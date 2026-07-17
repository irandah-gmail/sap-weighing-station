package com.plant.weighing.repository;

import com.plant.weighing.model.TransactionStatus;
import com.plant.weighing.model.WeighingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeighingTransactionRepository extends JpaRepository<WeighingTransaction, Long> {
    List<WeighingTransaction> findByStatusIn(List<TransactionStatus> statuses);
    List<WeighingTransaction> findTop50ByOrderByCapturedAtDesc();
}
