package com.portfolio.controller;

import com.portfolio.dto.CreateTransactionRequest;
import com.portfolio.dto.TransactionDTO;
import com.portfolio.service.TransactionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(
            @RequestParam Long portfolioId,
            @Valid @RequestBody CreateTransactionRequest request) {
        log.info("Creating transaction for portfolio: {}", portfolioId);
        TransactionDTO transaction = transactionService.createTransaction(portfolioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getTransactions(@RequestParam Long portfolioId) {
        log.info("Fetching transactions for portfolio: {}", portfolioId);
        List<TransactionDTO> transactions = transactionService.getPortfolioTransactions(portfolioId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/by-date-range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @RequestParam Long portfolioId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Fetching transactions for portfolio: {} from {} to {}", portfolioId, startDate, endDate);
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        List<TransactionDTO> transactions = transactionService.getTransactionsByDateRange(portfolioId, start, end);
        return ResponseEntity.ok(transactions);
    }
}
