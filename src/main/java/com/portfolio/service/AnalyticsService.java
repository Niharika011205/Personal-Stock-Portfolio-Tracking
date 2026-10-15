package com.portfolio.service;

import com.portfolio.dto.AnalyticsReportDTO;
import com.portfolio.model.Portfolio;
import com.portfolio.model.PortfolioAsset;
import com.portfolio.model.PortfolioSnapshot;
import com.portfolio.model.Transaction;
import com.portfolio.repository.PortfolioRepository;
import com.portfolio.repository.PortfolioAssetRepository;
import com.portfolio.repository.PortfolioSnapshotRepository;
import com.portfolio.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class AnalyticsService {
    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository assetRepository;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final TransactionRepository transactionRepository;

    public AnalyticsService(PortfolioRepository portfolioRepository,
                           PortfolioAssetRepository assetRepository,
                           PortfolioSnapshotRepository snapshotRepository,
                           TransactionRepository transactionRepository) {
        this.portfolioRepository = portfolioRepository;
        this.assetRepository = assetRepository;
        this.snapshotRepository = snapshotRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public AnalyticsReportDTO generatePortfolioReport(Long portfolioId) {
        log.info("Generating analytics report for portfolio: {}", portfolioId);
        
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        List<PortfolioAsset> assets = assetRepository.findByPortfolioId(portfolioId);
        List<Transaction> transactions = transactionRepository.findByPortfolioId(portfolioId);

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (PortfolioAsset asset : assets) {
            totalValue = totalValue.add(asset.getCurrentValue());
            totalCost = totalCost.add(asset.getTotalCost());
        }

        BigDecimal unrealizedPnL = totalValue.subtract(totalCost);
        BigDecimal returnPercentage = totalCost.compareTo(BigDecimal.ZERO) > 0
                ? unrealizedPnL.divide(totalCost, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100))
                : BigDecimal.ZERO;

        Map<String, BigDecimal> allocation = calculateAssetAllocation(assets, totalValue);
        List<AnalyticsReportDTO.AssetPerformance> topPerformers = getTopPerformers(assets, totalValue, 5);
        List<AnalyticsReportDTO.AssetPerformance> worstPerformers = getWorstPerformers(assets, totalValue, 5);

        savePortfolioSnapshot(portfolio, totalValue, totalCost, unrealizedPnL, assets.size(), returnPercentage);

        return AnalyticsReportDTO.builder()
                .portfolioId(portfolioId)
                .totalValue(totalValue)
                .totalCost(totalCost)
                .unrealizedPnL(unrealizedPnL)
                .returnPercentage(returnPercentage)
                .totalAssets(assets.size())
                .assetAllocation(allocation)
                .topPerformers(topPerformers)
                .worstPerformers(worstPerformers)
                .reportDate(LocalDate.now().toString())
                .totalTransactions(transactions.size())
                .averageCostPerAsset(calculateAverageCost(assets))
                .build();
    }

    private Map<String, BigDecimal> calculateAssetAllocation(List<PortfolioAsset> assets, BigDecimal totalValue) {
        Map<String, BigDecimal> allocation = new LinkedHashMap<>();
        
        if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
            return allocation;
        }

        for (PortfolioAsset asset : assets) {
            BigDecimal percentage = asset.getCurrentValue()
                    .divide(totalValue, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            allocation.put(asset.getTicker(), percentage);
        }

        return allocation;
    }

    private List<AnalyticsReportDTO.AssetPerformance> getTopPerformers(List<PortfolioAsset> assets, BigDecimal totalValue, int limit) {
        return assets.stream()
                .sorted((a, b) -> b.getReturnPercentage().compareTo(a.getReturnPercentage()))
                .limit(limit)
                .map(asset -> AnalyticsReportDTO.AssetPerformance.builder()
                        .ticker(asset.getTicker())
                        .returnPercentage(asset.getReturnPercentage())
                        .unrealizedPnL(asset.getUnrealizedPnL())
                        .allocationPercentage(totalValue.compareTo(BigDecimal.ZERO) > 0
                                ? asset.getCurrentValue().divide(totalValue, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100))
                                : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }

    private List<AnalyticsReportDTO.AssetPerformance> getWorstPerformers(List<PortfolioAsset> assets, BigDecimal totalValue, int limit) {
        return assets.stream()
                .sorted(Comparator.comparing(PortfolioAsset::getReturnPercentage))
                .limit(limit)
                .map(asset -> AnalyticsReportDTO.AssetPerformance.builder()
                        .ticker(asset.getTicker())
                        .returnPercentage(asset.getReturnPercentage())
                        .unrealizedPnL(asset.getUnrealizedPnL())
                        .allocationPercentage(totalValue.compareTo(BigDecimal.ZERO) > 0
                                ? asset.getCurrentValue().divide(totalValue, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100))
                                : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }

    private BigDecimal calculateAverageCost(List<PortfolioAsset> assets) {
        if (assets.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalCost = assets.stream()
                .map(PortfolioAsset::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalCost.divide(new BigDecimal(assets.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    private void savePortfolioSnapshot(Portfolio portfolio, BigDecimal totalValue, BigDecimal totalCost,
                                       BigDecimal unrealizedPnL, Integer assetCount, BigDecimal returnPercentage) {
        PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                .portfolio(portfolio)
                .snapshotDate(LocalDate.now())
                .totalValue(totalValue)
                .totalCost(totalCost)
                .unrealizedPnL(unrealizedPnL)
                .assetCount(assetCount)
                .returnPercentage(returnPercentage)
                .build();
        snapshotRepository.save(snapshot);
        log.info("Portfolio snapshot saved for portfolio: {}", portfolio.getId());
    }

    public List<PortfolioSnapshot> getPortfolioHistory(Long portfolioId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        LocalDate endDate = LocalDate.now();
        return snapshotRepository.findSnapshotsByDateRange(portfolioId, startDate, endDate);
    }
}
