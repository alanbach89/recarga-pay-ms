package org.example.entity;

import jakarta.persistence.*;
import org.example.enums.TransactionType;

import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long walletId;
    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private LocalDateTime timestamp;

    // Default constructor
    public Transaction() {
    }

    // Parameterized constructor
    public Transaction(Long id, Long walletId, Double amount, TransactionType type, LocalDateTime timestamp) {
        this.id = id;
        this.walletId = walletId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}