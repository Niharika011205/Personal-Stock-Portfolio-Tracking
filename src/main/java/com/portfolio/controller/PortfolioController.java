package com.portfolio.controller;

import com.portfolio.dto.PortfolioDTO;
import com.portfolio.service.PortfolioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portfolios")
@Slf4j
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public ResponseEntity<PortfolioDTO> createPortfolio(
            @RequestParam Long userId,
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        log.info("Creating portfolio for user: {}", userId);
        PortfolioDTO portfolio = portfolioService.createPortfolio(userId, name, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolio);
    }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioDTO> getPortfolio(
            @PathVariable Long portfolioId,
            @RequestParam Long userId) {
        log.info("Fetching portfolio: {}", portfolioId);
        PortfolioDTO portfolio = portfolioService.getPortfolioById(portfolioId, userId);
        return ResponseEntity.ok(portfolio);
    }

    @GetMapping
    public ResponseEntity<List<PortfolioDTO>> getUserPortfolios(@RequestParam Long userId) {
        log.info("Fetching portfolios for user: {}", userId);
        List<PortfolioDTO> portfolios = portfolioService.getUserPortfolios(userId);
        return ResponseEntity.ok(portfolios);
    }

    @PutMapping("/{portfolioId}/refresh-prices")
    public ResponseEntity<PortfolioDTO> refreshPortfolioPrices(
            @PathVariable Long portfolioId,
            @RequestParam Long userId) {
        log.info("Refreshing prices for portfolio: {}", portfolioId);
        PortfolioDTO portfolio = portfolioService.updatePortfolioAssetPrices(portfolioId, userId);
        return ResponseEntity.ok(portfolio);
    }
}
