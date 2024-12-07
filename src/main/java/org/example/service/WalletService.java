package org.example.service;

import jakarta.transaction.Transactional;
import org.example.entity.Transaction;
import org.example.entity.User;
import org.example.entity.Wallet;
import org.example.enums.TransactionType;
import org.example.repository.TransactionRepository;
import org.example.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RedisCacheService redisCacheService;

    public Wallet createWallet(User user) {
        Wallet wallet = new Wallet(null, user, 0.0,  LocalDateTime.now());
        return walletRepository.save(wallet);
    }

    public Double retrieveBalance(Long walletId) {
        Double cachedBalance = redisCacheService.getSearchResultFromCache(walletId.toString(), LocalDateTime.now());
        if (cachedBalance != null) {
            return cachedBalance;
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        redisCacheService.cacheSearchResult(walletId.toString(), LocalDateTime.now(), wallet.getBalance());
        return wallet.getBalance();
    }

    public Wallet depositFunds(Long walletId, Double amount) {
        // Validate the deposit amount
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Update the balance
        Double updatedBalance = wallet.getBalance() + amount;
        wallet.setBalance(updatedBalance);

        Transaction transaction = new Transaction(null, walletId, amount,
                TransactionType.DEPOSIT, LocalDateTime.now());
        transactionRepository.save(transaction);

        redisCacheService.cacheSearchResult(walletId.toString(), LocalDateTime.now(), updatedBalance);
        return walletRepository.save(wallet);
    }

    public Wallet withdrawFunds(Long walletId, Double amount) {
        // Validate the withdrawal amount
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        }

        // Fetch the wallet
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Check if sufficient funds are available
        if (wallet.getBalance() != 0 && wallet.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds.");
        }

        // Deduct the amount from the wallet balance
        Double updatedBalance = wallet.getBalance() - amount;
        wallet.setBalance(updatedBalance);

        // Record the transaction
        Transaction transaction = new Transaction(null, walletId, -amount,
                TransactionType.WITHDRAWAL, LocalDateTime.now());
        transactionRepository.save(transaction);

        redisCacheService.cacheSearchResult(walletId.toString(), LocalDateTime.now(), updatedBalance);
        return walletRepository.save(wallet);
    }


    public Double retrieveHistoricalBalance(Long walletId, LocalDateTime timestamp) {
        Double cachedBalance = redisCacheService.getSearchResultFromCache(walletId.toString(), timestamp);
        if (cachedBalance != null) {
            return cachedBalance;
        }

        walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        List<Transaction> transactions = transactionRepository.findByWalletIdAndTimestampBefore(walletId, timestamp);
        return transactions.stream()
                .mapToDouble(t -> t.getType().equals(TransactionType.DEPOSIT) ? t.getAmount() : -t.getAmount())
                .sum();
    }

    public void transferFunds(Long fromWalletId, Long toWalletId, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }

        // Fetch wallets
        Wallet fromWallet = walletRepository.findById(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Source wallet not found"));
        Wallet toWallet = walletRepository.findById(toWalletId)
                .orElseThrow(() -> new RuntimeException("Destination wallet not found"));

        // Check if source wallet has sufficient balance
        if (fromWallet.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds in source wallet.");
        }

        // Update balances
        fromWallet.setBalance(fromWallet.getBalance() - amount);
        toWallet.setBalance(toWallet.getBalance() + amount);

        // Save updated wallets
        redisCacheService.cacheSearchResult(fromWallet.getId().toString(), LocalDateTime.now(), fromWallet.getBalance());
        redisCacheService.cacheSearchResult(toWallet.getId().toString(), LocalDateTime.now(), toWallet.getBalance());
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Record transactions
        Transaction withdrawalTransaction = new Transaction(
                null,
                fromWalletId,
                -amount,
                TransactionType.WITHDRAWAL,
                LocalDateTime.now()
        );
        Transaction depositTransaction = new Transaction(
                null,
                toWalletId,
                amount,
                TransactionType.DEPOSIT,
                LocalDateTime.now()
        );

        transactionRepository.save(withdrawalTransaction);
        transactionRepository.save(depositTransaction);
    }
}

