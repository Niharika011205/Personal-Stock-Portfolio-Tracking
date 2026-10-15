package com.portfolio.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsReportDTO {
    private Long portfolioId;
    private BigDecimal totalValue;
    private BigDecimal totalCost;
    private BigDecimal unrealizedPnL;
    private BigDecimal returnPercentage;
    private Integer totalAssets;
    private Map<String, BigDecimal> assetAllocation;
    private List<AssetPerformance> topPerformers;
    private List<AssetPerformance> worstPerformers;
    private String reportDate;
    private int totalTransactions;
    private BigDecimal averageCostPerAsset;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssetPerformance {
        private String ticker;
        private BigDecimal returnPercentage;
        private BigDecimal unrealizedPnL;
        private BigDecimal allocationPercentage;
    }
}
