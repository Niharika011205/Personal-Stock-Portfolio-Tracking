package com.portfolio.service;

import com.portfolio.dto.CreateTransactionRequest;
import com.portfolio.dto.TransactionDTO;
import com.portfolio.model.Portfolio;
import com.portfolio.model.PortfolioAsset;
import com.portfolio.model.Transaction;
import com.portfolio.repository.PortfolioRepository;
import com.portfolio.repository.PortfolioAssetRepository;
import com.portfolio.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository assetRepository;
    private final MarketDataService marketDataService;

    public TransactionService(TransactionRepository transactionRepository,
                             PortfolioRepository portfolioRepository,
                             PortfolioAssetRepository assetRepository,
                             MarketDataService marketDataService) {
        this.transactionRepository = transactionRepository;
        this.portfolioRepository = portfolioRepository;
        this.assetRepository = assetRepository;
        this.marketDataService = marketDataService;
    }

    public TransactionDTO createTransaction(Long portfolioId, CreateTransactionRequest request) {
        log.info("Creating transaction for portfolio: {}", portfolioId);
        
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        Transaction transaction = Transaction.builder()
                .portfolio(portfolio)
                .transactionType(Transaction.TransactionType.valueOf(request.getTransactionType()))
                .ticker(request.getTicker().toUpperCase())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .transactionDate(request.getTransactionDate())
                .notes(request.getNotes())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        updatePortfolioAsset(portfolio, request);
        
        log.info("Transaction created with ID: {}", saved.getId());
        return mapToDTO(saved);
    }

    private void updatePortfolioAsset(Portfolio portfolio, CreateTransactionRequest request) {
        String ticker = request.getTicker().toUpperCase();
        Optional<PortfolioAsset> existingAsset = assetRepository.findByPortfolioIdAndTicker(portfolio.getId(), ticker);

        BigDecimal currentPrice = marketDataService.getStockPrice(ticker);

        if (existingAsset.isPresent()) {
            PortfolioAsset asset = existingAsset.get();
            if (request.getTransactionType().equals("BUY")) {
                asset = updateAssetOnBuy(asset, request, currentPrice);
            } else if (request.getTransactionType().equals("SELL")) {
                asset = updateAssetOnSell(asset, request, currentPrice);
            }
            assetRepository.save(asset);
        } else {
            if (request.getTransactionType().equals("BUY")) {
                PortfolioAsset newAsset = createNewAsset(portfolio, ticker, request, currentPrice);
                assetRepository.save(newAsset);
            }
        }
    }

    private PortfolioAsset updateAssetOnBuy(PortfolioAsset asset, CreateTransactionRequest request, BigDecimal currentPrice) {
        BigDecimal totalValue = asset.getQuantity().multiply(asset.getAveragePurchasePrice())
                .add(request.getQuantity().multiply(request.getPrice()));
        BigDecimal newQuantity = asset.getQuantity().add(request.getQuantity());
        BigDecimal newAveragePrice = totalValue.divide(newQuantity, 4, java.math.RoundingMode.HALF_UP);

        asset.setQuantity(newQuantity);
        asset.setAveragePurchasePrice(newAveragePrice);
        asset.setTotalCost(newQuantity.multiply(newAveragePrice));
        asset.updatePriceAndValue(currentPrice);
        
        return asset;
    }

    private PortfolioAsset updateAssetOnSell(PortfolioAsset asset, CreateTransactionRequest request, BigDecimal currentPrice) {
        BigDecimal newQuantity = asset.getQuantity().subtract(request.getQuantity());
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient quantity to sell");
        }

        asset.setQuantity(newQuantity);
        if (newQuantity.compareTo(BigDecimal.ZERO) > 0) {
            asset.setTotalCost(newQuantity.multiply(asset.getAveragePurchasePrice()));
            asset.updatePriceAndValue(currentPrice);
        } else {
            asset.setTotalCost(BigDecimal.ZERO);
            asset.setCurrentValue(BigDecimal.ZERO);
            asset.setUnrealizedPnL(BigDecimal.ZERO);
        }
        
        return asset;
    }

    private PortfolioAsset createNewAsset(Portfolio portfolio, String ticker, CreateTransactionRequest request, BigDecimal currentPrice) {
        BigDecimal totalCost = request.getQuantity().multiply(request.getPrice());
        
        return PortfolioAsset.builder()
                .portfolio(portfolio)
                .ticker(ticker)
                .quantity(request.getQuantity())
                .averagePurchasePrice(request.getPrice())
                .currentPrice(currentPrice)
                .currentValue(request.getQuantity().multiply(currentPrice))
                .totalCost(totalCost)
                .unrealizedPnL(request.getQuantity().multiply(currentPrice).subtract(totalCost))
                .build();
    }

    public List<TransactionDTO> getPortfolioTransactions(Long portfolioId) {
        List<Transaction> transactions = transactionRepository.findByPortfolioId(portfolioId);
        return transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getTransactionsByDateRange(Long portfolioId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository.findByPortfolioIdAndDateRange(portfolioId, startDate, endDate);
        return transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TransactionDTO mapToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType().toString())
                .ticker(transaction.getTicker())
                .quantity(transaction.getQuantity())
                .price(transaction.getPrice())
                .totalAmount(transaction.getTotalAmount())
                .transactionDate(transaction.getTransactionDate().toString())
                .createdAt(transaction.getCreatedAt().toString())
                .notes(transaction.getNotes())
                .build();
    }
}
