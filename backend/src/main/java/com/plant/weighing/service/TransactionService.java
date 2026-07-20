package com.plant.weighing.service;

import com.plant.weighing.dto.WeighingSubmitRequest;
import com.plant.weighing.model.TransactionStatus;
import com.plant.weighing.model.WeighingTransaction;
import com.plant.weighing.repository.WeighingTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final WeighingTransactionRepository repository;
    private final SapClientService sapClientService;

    public TransactionService(WeighingTransactionRepository repository, SapClientService sapClientService) {
        this.repository = repository;
        this.sapClientService = sapClientService;
    }

    /**
     * Saves the transaction locally first (so it's never lost even if SAP is
     * unreachable), then tries to post it immediately. If that fails, it's
     * left as FAILED and picked up by the retry scheduler.
     */
    public WeighingTransaction submit(WeighingSubmitRequest request) {
        double total = request.getWeightReadings().stream().mapToDouble(Double::doubleValue).sum();

        WeighingTransaction tx = new WeighingTransaction();
        tx.setProductionOrderId(request.getProductionOrderId());
        tx.setBatchId(request.getBatchId());
        tx.setUserId(request.getUserId());
        tx.setTotalWeight(Math.round(total * 100.0) / 100.0);
        tx.setUnit(request.getUnit());
        tx.setStatus(TransactionStatus.PENDING);
        tx.setCapturedAt(LocalDateTime.now());
        tx = repository.save(tx);

        attemptSend(tx);
        return tx;
    }

    public void attemptSend(WeighingTransaction tx) {
        try {
            String sapRef = sapClientService.postConfirmation(tx);
            tx.setStatus(TransactionStatus.SENT);
            tx.setSapReference(sapRef);
            tx.setSentAt(LocalDateTime.now());
            tx.setLastError(null);
            log.info("Transaction {} sent to SAP successfully (ref {})", tx.getId(), sapRef);
        } catch (Exception e) {
            tx.setStatus(TransactionStatus.FAILED);
            tx.setRetryCount(tx.getRetryCount() + 1);
            tx.setLastError(e.getMessage());
            log.warn("Transaction {} failed to send to SAP (attempt {}): {}", tx.getId(), tx.getRetryCount(), e.getMessage());
        }
        repository.save(tx);
    }

    public List<WeighingTransaction> recent() {
        return repository.findTop50ByOrderByCapturedAtDesc();
    }

    public List<WeighingTransaction> pendingOrFailed() {
        return repository.findByStatusIn(List.of(TransactionStatus.PENDING, TransactionStatus.FAILED));
    }

    /**
     * Clear a transaction that previously failed so it can be retried.
     * This sets the status back to PENDING, clears the last error and resets retry count.
     */
    public WeighingTransaction clearFailed(Long id) {
        return repository.findById(id).map(tx -> {
            if (tx.getStatus() == TransactionStatus.FAILED) {
                tx.setStatus(TransactionStatus.PENDING);
                tx.setLastError(null);
                tx.setRetryCount(0);
                repository.save(tx);
            }
            return tx;
        }).orElse(null);
    }
}
