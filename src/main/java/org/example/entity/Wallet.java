package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private Double balance;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default constructor
    public Wallet() {
    }

    // Parameterized constructor
    public Wallet(Long id, User user, Double balance, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}