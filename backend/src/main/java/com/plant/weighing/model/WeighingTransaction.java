package com.plant.weighing.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weighing_transaction")
public class WeighingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productionOrderId;

    @Column(nullable = false)
    private String batchId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private double totalWeight;

    @Column(nullable = false)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(length = 2000)
    private String lastError;

    private int retryCount = 0;

    @Column(nullable = false)
    private LocalDateTime capturedAt;

    private LocalDateTime sentAt;

    /** SAP's confirmation/document number returned once posted successfully */
    private String sapReference;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(String productionOrderId) { this.productionOrderId = productionOrderId; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getTotalWeight() { return totalWeight; }
    public void setTotalWeight(double totalWeight) { this.totalWeight = totalWeight; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public LocalDateTime getCapturedAt() { return capturedAt; }
    public void setCapturedAt(LocalDateTime capturedAt) { this.capturedAt = capturedAt; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getSapReference() { return sapReference; }
    public void setSapReference(String sapReference) { this.sapReference = sapReference; }
}
