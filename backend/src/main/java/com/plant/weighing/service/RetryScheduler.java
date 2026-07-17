package com.plant.weighing.service;

import com.plant.weighing.model.WeighingTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryScheduler.class);

    private static final int MAX_RETRIES = 20;

    private final TransactionService transactionService;

    public RetryScheduler(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /** Every 2 minutes, retry any transaction that hasn't been sent to SAP yet. */
    @Scheduled(fixedDelayString = "${sap.retryIntervalMs:120000}")
    public void retryFailedTransactions() {
        for (WeighingTransaction tx : transactionService.pendingOrFailed()) {
            if (tx.getRetryCount() >= MAX_RETRIES) {
                log.error("Transaction {} exceeded max retries ({}); needs manual intervention.", tx.getId(), MAX_RETRIES);
                continue;
            }
            transactionService.attemptSend(tx);
        }
    }
}
