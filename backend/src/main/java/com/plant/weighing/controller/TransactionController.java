package com.plant.weighing.controller;

import com.plant.weighing.dto.WeighingSubmitRequest;
import com.plant.weighing.model.WeighingTransaction;
import com.plant.weighing.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
