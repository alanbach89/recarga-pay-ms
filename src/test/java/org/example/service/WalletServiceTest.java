package org.example.service;

import org.example.entity.Transaction;
import org.example.entity.User;
import org.example.entity.Wallet;
import org.example.enums.TransactionType;
import org.example.repository.TransactionRepository;
import org.example.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private WalletService walletService;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User(1L, "testuser", "email@user.com", "Argentina");
        wallet = new Wallet(1L, user, 100.0, LocalDateTime.now());
    }

    @Test
    void testCreateWallet() {
        Wallet newWallet = new Wallet(1L, user, 0.0, LocalDateTime.now());
        when(walletRepository.save(any(Wallet.class))).thenReturn(newWallet);

        Wallet createdWallet = walletService.createWallet(user);

        assertNotNull(createdWallet);
        assertEquals(user, createdWallet.getUser());
        assertEquals(0.0, createdWallet.getBalance());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void testRetrieveBalance() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(redisCacheService.getSearchResultFromCache(any(), any())).thenReturn(null);

        Double balance = walletService.retrieveBalance(1L);

        assertEquals(100.0, balance);
        verify(walletRepository, times(1)).findById(1L);
    }

    @Test
    void testDepositFunds() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(redisCacheService.getSearchResultFromCache(any(), any())).thenReturn(null);

        Wallet newWallet = new Wallet(wallet.getId(), wallet.getUser(), 150.0, wallet.getCreatedAt());
        when(walletRepository.save(any(Wallet.class))).thenReturn(newWallet);

        Wallet updatedWallet = walletService.depositFunds(1L, 50.0);

        assertNotNull(updatedWallet);
        assertEquals(150.0, updatedWallet.getBalance());
        verify(walletRepository, times(1)).findById(1L);
        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testWithdrawFunds() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(redisCacheService.getSearchResultFromCache(any(), any())).thenReturn(null);

        Wallet newWallet = new Wallet(wallet.getId(), wallet.getUser(), 50.0, wallet.getCreatedAt());
        when(walletRepository.save(any(Wallet.class))).thenReturn(newWallet);

        Wallet updatedWallet = walletService.withdrawFunds(1L, 50.0);

        assertNotNull(updatedWallet);
        assertEquals(50.0, updatedWallet.getBalance());
        verify(walletRepository, times(1)).findById(1L);
        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testRetrieveHistoricalBalance() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(redisCacheService.getSearchResultFromCache(any(), any())).thenReturn(null);
        when(transactionRepository.findByWalletIdAndTimestampBefore(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(
                        new Transaction(1L, 1L, 50.0, TransactionType.DEPOSIT, LocalDateTime.now().minusDays(1)),
                        new Transaction(2L, 1L, 20.0, TransactionType.WITHDRAWAL, LocalDateTime.now().minusDays(1))
                ));

        Double historicalBalance = walletService.retrieveHistoricalBalance(1L, LocalDateTime.now());

        assertEquals(30.0, historicalBalance);
        verify(walletRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).findByWalletIdAndTimestampBefore(anyLong(), any(LocalDateTime.class));
    }

    @Test
    void testTransferFunds() {
        Wallet toWallet = new Wallet(2L, user, 50.0, LocalDateTime.now());
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(toWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(redisCacheService.getSearchResultFromCache(any(), any())).thenReturn(null);

        walletService.transferFunds(1L, 2L, 50.0);

        verify(walletRepository, times(2)).findById(anyLong());
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }
}