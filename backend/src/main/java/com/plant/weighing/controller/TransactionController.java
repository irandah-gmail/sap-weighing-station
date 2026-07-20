package com.plant.weighing.controller;

import com.plant.weighing.dto.WeighingSubmitRequest;
import com.plant.weighing.model.WeighingTransaction;
import com.plant.weighing.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /** Submit a completed weighing (Production Order, Batch, User, weight readings). */
    @PostMapping
    public WeighingTransaction submit(@Valid @RequestBody WeighingSubmitRequest request) {
        return transactionService.submit(request);
    }

    /** Recent transactions, for the "pending sync" panel in the UI. */
    @GetMapping("/recent")
    public List<WeighingTransaction> recent() {
        return transactionService.recent();
    }

    /** Clear the FAILED status on a transaction so it can be retried. */
    @PostMapping("/{id}/clear-failed")
    public WeighingTransaction clearFailed(@PathVariable Long id) {
        WeighingTransaction tx = transactionService.clearFailed(id);
        if (tx == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
        return tx;
    }

    /** Resend immediately: reset state and attempt to send right away. */
    @PostMapping("/{id}/resend")
    public WeighingTransaction resendFailed(@PathVariable Long id) {
        WeighingTransaction tx = transactionService.resendFailed(id);
        if (tx == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
        return tx;
    }

    /** Delete a FAILED transaction from the DB. */
    @DeleteMapping("/{id}")
    public void deleteIfFailed(@PathVariable Long id) {
        boolean deleted = transactionService.deleteIfFailed(id);
        if (!deleted) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only delete transactions in FAILED state or transaction not found");
    }
}
