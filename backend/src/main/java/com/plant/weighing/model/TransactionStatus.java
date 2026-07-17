package com.plant.weighing.model;

public enum TransactionStatus {
    PENDING,   // captured locally, not yet sent to SAP
    SENT,      // successfully posted to SAP
    FAILED     // attempted, failed, will be retried
}
