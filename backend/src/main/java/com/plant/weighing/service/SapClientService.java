package com.plant.weighing.service;

import com.plant.weighing.config.SapProperties;
import com.plant.weighing.model.WeighingTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Posts a completed weighing transaction directly to SAP S/4HANA Public
 * Cloud, via the OData API exposed through your Communication Arrangement.
 *
 * IMPORTANT — this class intentionally does NOT hardcode SAP's exact field
 * names, because they depend on which Communication Scenario/API you
 * activated (Production Order Confirmation vs. Goods Movement vs. Batch
 * update all have different entity/field names, and these can also change
 * across S/4HANA releases). Get the exact OData service metadata from:
 *   - SAP API Business Hub (api.sap.com) for the API name in your
 *     Communication Arrangement, or
 *   - the live $metadata endpoint of your own service, e.g.
 *     {baseUrl}{confirmationServicePath}/$metadata
 * then adjust buildPayload() below to match.
 */
@Service
public class SapClientService {

    private static final Logger log = LoggerFactory.getLogger(SapClientService.class);

    private final SapProperties sapProperties;
    private final RestTemplate restTemplate;

    public SapClientService(SapProperties sapProperties) {
        this.sapProperties = sapProperties;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Posts the transaction to SAP. Returns SAP's document/confirmation
     * reference number on success. Throws on failure (caller decides how to
     * handle retry).
     */
    public String postConfirmation(WeighingTransaction tx) {
        String url = sapProperties.getBaseUrl() + sapProperties.getConfirmationServicePath();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        // Most SAP OData V2 services require an X-CSRF-Token fetch step for
        // POST calls. Two-step flow: GET with X-CSRF-Token: Fetch, then POST
        // using the token + returned cookies. Implemented in fetchCsrfToken().
        String[] csrfTokenAndCookie = fetchCsrfToken(url);
        headers.set("X-CSRF-Token", csrfTokenAndCookie[0]);
        if (csrfTokenAndCookie[1] != null) {
            headers.set(HttpHeaders.COOKIE, csrfTokenAndCookie[1]);
        }
        applyAuth(headers);

        Map<String, Object> payload = buildPayload(tx);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            log.info("SAP confirmation posted for order {} batch {}: HTTP {}",
                    tx.getProductionOrderId(), tx.getBatchId(), response.getStatusCode());

            // TODO: adjust this to whatever unique key/reference field your
            // chosen API returns, e.g. ConfirmationNo, MaterialDocument, etc.
            Object body = response.getBody() != null ? response.getBody().get("d") : null;
            return body != null ? body.toString() : "OK";

        } catch (HttpClientErrorException e) {
            log.error("SAP rejected the confirmation for order {} batch {}: {} - {}",
                    tx.getProductionOrderId(), tx.getBatchId(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new RestClientException("SAP error " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        }
    }

    /**
     * Builds the JSON payload for the confirmation call.
     * TODO: replace these field names with the actual entity properties from
     * your API's $metadata (e.g. ProductionOrder, ConfirmationYieldQuantity,
     * ConfirmationUnit... for API_PRODUCTION_ORDER_CONFIRMATION_2_SRV, or the
     * equivalent fields for whatever API your arrangement uses).
     */
    private Map<String, Object> buildPayload(WeighingTransaction tx) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ProductionOrder", tx.getProductionOrderId());
        payload.put("Batch", tx.getBatchId());
        payload.put("YieldConfirmedQuantity", tx.getTotalWeight());
        payload.put("ConfirmationUnit", tx.getUnit());
        payload.put("EnteredByUser", tx.getUserId());
        return payload;
    }

    private void applyAuth(HttpHeaders headers) {
        if ("oauth2".equalsIgnoreCase(sapProperties.getAuthType())) {
            String token = fetchOAuthToken();
            headers.setBearerAuth(token);
        } else {
            headers.setBasicAuth(sapProperties.getUsername(), sapProperties.getPassword());
        }
    }

    /** OAuth2 client-credentials flow against the Communication Arrangement's token endpoint. */
    private String fetchOAuthToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        org.springframework.util.MultiValueMap<String, String> form = new org.springframework.util.LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", sapProperties.getClientId());
        form.add("client_secret", sapProperties.getClientSecret());

        HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(sapProperties.getTokenUrl(), request, Map.class);
        return (String) response.getBody().get("access_token");
    }

    /**
     * SAP OData V2 services require a CSRF token for write operations.
     * Fetch it with a lightweight GET before the actual POST.
     * Returns [token, setCookieHeader].
     */
    private String[] fetchCsrfToken(String serviceUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CSRF-Token", "Fetch");
        applyAuth(headers);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(serviceUrl, HttpMethod.GET, request, String.class);

        String token = response.getHeaders().getFirst("X-CSRF-Token");
        String cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        return new String[]{token, cookie};
    }
}
