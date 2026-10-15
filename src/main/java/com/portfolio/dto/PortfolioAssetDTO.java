package com.portfolio.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioAssetDTO {
    private Long id;
    private String ticker;
    private BigDecimal quantity;
    private BigDecimal averagePurchasePrice;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal totalCost;
    private BigDecimal unrealizedPnL;
    private BigDecimal returnPercentage;
    private String updatedAt;
}
