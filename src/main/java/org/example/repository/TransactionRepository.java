package org.example.repository;

import org.example.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWalletIdAndTimestampBefore(Long walletId, LocalDateTime timestamp);
}
