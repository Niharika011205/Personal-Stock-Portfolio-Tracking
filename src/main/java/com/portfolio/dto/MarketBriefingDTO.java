package com.portfolio.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketBriefingDTO {
    private Long portfolioId;
    private String briefingSummary;
    private String portfolioRiskAssessment;
    private List<String> keyRisks;
    private List<String> opportunityRecommendations;
    private String generatedAt;
}
