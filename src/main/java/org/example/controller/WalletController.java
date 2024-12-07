package org.example.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dto.FundRequestDto;
import org.example.entity.User;
import org.example.entity.Wallet;
import org.example.service.UserService;
import org.example.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private static final Logger logger = LogManager.getLogger(WalletController.class);

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;

    @PostMapping("/{userId}")
    public ResponseEntity<Wallet> createWallet(@PathVariable Long userId) {
        logger.info("Creating wallet for user with ID: {}", userId);
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = walletService.createWallet(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<Double> retrieveBalance(@PathVariable Long walletId) {
        logger.info("Retrieving balance for wallet with ID: {}", walletId);
        return ResponseEntity.ok(walletService.retrieveBalance(walletId));
    }

    @GetMapping("/{walletId}/historical-balance")
    public ResponseEntity<Double> retrieveHistoricalBalance(
            @PathVariable Long walletId,
            @RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date).plusDays(1);
            LocalDateTime dateTime = localDate.atStartOfDay();
            Double historicalBalance = walletService.retrieveHistoricalBalance(walletId, dateTime);
            return ResponseEntity.ok(historicalBalance);
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format: {}", date);
            return ResponseEntity.badRequest().body(null); // Invalid timestamp format
        } catch (RuntimeException e) {
            logger.error("Error retrieving historical balance for wallet with ID: {}", walletId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Wallet not found or other errors
        }
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<Wallet> depositFunds(
            @PathVariable Long walletId,
            @RequestBody FundRequestDto dto) {
        logger.info("Depositing funds to wallet with ID: {}", walletId);
        return ResponseEntity.ok(walletService.depositFunds(walletId, dto.getAmount()));
    }

    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<Wallet> withdrawFunds(
            @PathVariable Long walletId,
            @RequestBody FundRequestDto dto) {
        logger.info("Withdrawing funds from wallet with ID: {}", walletId);
        Wallet updatedWallet = walletService.withdrawFunds(walletId, dto.getAmount());
        return ResponseEntity.ok(updatedWallet);
    }

    @PostMapping("/{fromWalletId}/transfer/{toWalletId}")
    public ResponseEntity<String> transferFunds(
            @PathVariable Long fromWalletId,
            @PathVariable Long toWalletId,
            @RequestBody FundRequestDto dto) {
        logger.info("Transferring funds from wallet with ID: {} to wallet with ID: {}", fromWalletId, toWalletId);
        walletService.transferFunds(fromWalletId, toWalletId, dto.getAmount());
        return ResponseEntity.ok("Transfer successful");
    }
}

