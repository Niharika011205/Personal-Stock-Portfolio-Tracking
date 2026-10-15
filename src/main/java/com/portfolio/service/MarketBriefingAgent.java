package com.portfolio.service;

import com.portfolio.dto.MarketBriefingDTO;
import com.portfolio.model.Portfolio;
import com.portfolio.model.PortfolioAsset;
import com.portfolio.repository.PortfolioRepository;
import com.portfolio.repository.PortfolioAssetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class MarketBriefingAgent {
    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository assetRepository;

    public MarketBriefingAgent(PortfolioRepository portfolioRepository,
                               PortfolioAssetRepository assetRepository) {
        this.portfolioRepository = portfolioRepository;
        this.assetRepository = assetRepository;
    }

    public MarketBriefingDTO generateMarketBriefing(Long portfolioId) {
        log.info("Generating market briefing for portfolio: {}", portfolioId);
        
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        List<PortfolioAsset> assets = assetRepository.findByPortfolioId(portfolioId);

        String briefingSummary = generateBriefingSummary(assets);
        String riskAssessment = assessPortfolioRisk(assets);
        List<String> keyRisks = identifyKeyRisks(assets);
        List<String> recommendations = generateRecommendations(assets);

        return MarketBriefingDTO.builder()
                .portfolioId(portfolioId)
                .briefingSummary(briefingSummary)
                .portfolioRiskAssessment(riskAssessment)
                .keyRisks(keyRisks)
                .opportunityRecommendations(recommendations)
                .generatedAt(LocalDateTime.now().toString())
                .build();
    }

    private String generateBriefingSummary(List<PortfolioAsset> assets) {
        if (assets.isEmpty()) {
            return "Portfolio is currently empty. Start by adding stock positions to begin tracking.";
        }

        BigDecimal totalValue = assets.stream()
                .map(PortfolioAsset::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = assets.stream()
                .map(PortfolioAsset::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unrealizedPnL = totalValue.subtract(totalCost);
        BigDecimal returnPercentage = totalCost.compareTo(BigDecimal.ZERO) > 0
                ? unrealizedPnL.divide(totalCost, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100))
                : BigDecimal.ZERO;

        List<PortfolioAsset> gainers = assets.stream()
                .filter(a -> a.getUnrealizedPnL().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getReturnPercentage().compareTo(a.getReturnPercentage()))
                .limit(3)
                .collect(Collectors.toList());

        List<PortfolioAsset> losers = assets.stream()
                .filter(a -> a.getUnrealizedPnL().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(PortfolioAsset::getReturnPercentage))
                .limit(3)
                .collect(Collectors.toList());

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Portfolio Overview: Your portfolio holds %d assets with a total value of $%.2f. ", 
                assets.size(), totalValue));
        summary.append(String.format("Overall return: %s%.2f%% ", returnPercentage.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "", returnPercentage));
        summary.append(String.format("($%.2f). ", unrealizedPnL));

        if (!gainers.isEmpty()) {
            summary.append("Top performers: ");
            summary.append(gainers.stream()
                    .map(a -> String.format("%s (+%.2f%%)", a.getTicker(), a.getReturnPercentage()))
                    .collect(Collectors.joining(", ")));
            summary.append(". ");
        }

        if (!losers.isEmpty()) {
            summary.append("Underperformers: ");
            summary.append(losers.stream()
                    .map(a -> String.format("%s (%.2f%%)", a.getTicker(), a.getReturnPercentage()))
                    .collect(Collectors.joining(", ")));
            summary.append(". ");
        }

        return summary.toString();
    }

    private String assessPortfolioRisk(List<PortfolioAsset> assets) {
        if (assets.isEmpty()) {
            return "MINIMAL - No assets in portfolio.";
        }

        BigDecimal totalValue = assets.stream()
                .map(PortfolioAsset::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate concentration risk
        Optional<BigDecimal> maxAllocation = assets.stream()
                .map(a -> a.getCurrentValue().divide(totalValue, 4, java.math.RoundingMode.HALF_UP))
                .max(BigDecimal::compareTo);

        // Count negative positions
        long negativePositions = assets.stream()
                .filter(a -> a.getUnrealizedPnL().compareTo(BigDecimal.ZERO) < 0)
                .count();

        long totalAssets = assets.size();

        String riskLevel = "MODERATE";
        String rationale = "";

        if (maxAllocation.isPresent() && maxAllocation.get().compareTo(new BigDecimal("0.50")) > 0) {
            riskLevel = "HIGH";
            rationale = "High concentration risk. ";
        }

        if ((double) negativePositions / totalAssets > 0.5) {
            riskLevel = "HIGH";
            rationale += "More than half of positions are underwater. ";
        }

        if (totalAssets < 3) {
            riskLevel = "HIGH";
            rationale += "Limited diversification with few assets. ";
        }

        if (totalAssets > 15 && maxAllocation.isPresent() && maxAllocation.get().compareTo(new BigDecimal("0.20")) < 0) {
            riskLevel = "LOW";
            rationale = "Well-diversified portfolio with no major concentration.";
        }

        return riskLevel + " - " + (rationale.isEmpty() ? "Balanced risk profile." : rationale);
    }

    private List<String> identifyKeyRisks(List<PortfolioAsset> assets) {
        List<String> risks = new ArrayList<>();

        BigDecimal totalValue = assets.stream()
                .map(PortfolioAsset::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Concentration risk
        assets.stream()
                .filter(a -> a.getCurrentValue().divide(totalValue, 4, java.math.RoundingMode.HALF_UP)
                        .compareTo(new BigDecimal("0.30")) > 0)
                .forEach(a -> risks.add(String.format("High concentration: %s represents >30%% of portfolio", a.getTicker())));

        // Underwater positions
        assets.stream()
                .filter(a -> a.getUnrealizedPnL().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(PortfolioAsset::getReturnPercentage))
                .limit(3)
                .forEach(a -> risks.add(String.format("Underwater position: %s down %.2f%%", a.getTicker(), a.getReturnPercentage())));

        // Limited diversification
        if (assets.size() < 5) {
            risks.add(String.format("Limited diversification: Only %d unique positions", assets.size()));
        }

        return risks;
    }

    private List<String> generateRecommendations(List<PortfolioAsset> assets) {
        List<String> recommendations = new ArrayList<>();

        if (assets.isEmpty()) {
            recommendations.add("Start building your portfolio by adding diverse stock positions");
            return recommendations;
        }

        BigDecimal totalValue = assets.stream()
                .map(PortfolioAsset::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Diversification recommendation
        if (assets.size() < 5) {
            recommendations.add("Increase portfolio diversification by adding 2-3 more positions across different sectors");
        }

        // Rebalancing recommendation
        Optional<BigDecimal> maxAllocation = assets.stream()
                .map(a -> a.getCurrentValue().divide(totalValue, 4, java.math.RoundingMode.HALF_UP))
                .max(BigDecimal::compareTo);

        if (maxAllocation.isPresent() && maxAllocation.get().compareTo(new BigDecimal("0.40")) > 0) {
            recommendations.add("Consider rebalancing: Top position exceeds 40% allocation");
        }

        // Profit taking recommendation
        List<PortfolioAsset> strongPerformers = assets.stream()
                .filter(a -> a.getReturnPercentage().compareTo(new BigDecimal("50")) > 0)
                .collect(Collectors.toList());

        if (!strongPerformers.isEmpty()) {
            recommendations.add(String.format("Consider taking profits on strong performers: %s",
                    strongPerformers.stream().map(PortfolioAsset::getTicker).collect(Collectors.joining(", "))));
        }

        // Loss mitigation
        List<PortfolioAsset> significantLosers = assets.stream()
                .filter(a -> a.getReturnPercentage().compareTo(new BigDecimal("-30")) < 0)
                .collect(Collectors.toList());

        if (!significantLosers.isEmpty()) {
            recommendations.add("Review underperforming positions for potential loss mitigation strategies");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Portfolio appears well-balanced. Continue monitoring market conditions");
        }

        return recommendations;
    }
}
