package com.portfolio.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioDTO {
    private Long id;
    private String name;
    private String description;
    private String createdAt;
    private String updatedAt;
    private BigDecimal totalValue;
    private BigDecimal totalCost;
    private BigDecimal unrealizedPnL;
    private BigDecimal returnPercentage;
    private Integer assetCount;
    private List<PortfolioAssetDTO> assets;
}
