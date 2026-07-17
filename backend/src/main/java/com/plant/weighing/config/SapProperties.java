package com.plant.weighing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bound from the `sap:` section of application.yml.
 * Fill these in from the Communication Arrangement you create in
 * SAP S/4HANA Public Cloud (Communication Systems / Communication User /
 * Communication Arrangement Fiori apps).
 */
@Component
@ConfigurationProperties(prefix = "sap")
public class SapProperties {

    /** Base host of the S/4HANA Public Cloud tenant, e.g. https://myXXXXXX.s4hana.cloud.sap */
    private String baseUrl;

    /** OData service path for the API bundled in your Communication Arrangement,
     *  e.g. /sap/opu/odata/sap/API_PRODUCTION_ORDER_CONFIRMATION_2_SRV */
    private String confirmationServicePath;

    /** Authentication: "basic" or "oauth2" depending on how the Communication
     *  Arrangement / Communication User was set up */
    private String authType = "basic";

    // Basic auth (Communication User)
    private String username;
    private String password;

    // OAuth2 client credentials (if the arrangement uses OAuth2)
    private String tokenUrl;
    private String clientId;
    private String clientSecret;

    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 15000;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getConfirmationServicePath() { return confirmationServicePath; }
    public void setConfirmationServicePath(String confirmationServicePath) { this.confirmationServicePath = confirmationServicePath; }

    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTokenUrl() { return tokenUrl; }
    public void setTokenUrl(String tokenUrl) { this.tokenUrl = tokenUrl; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
}
