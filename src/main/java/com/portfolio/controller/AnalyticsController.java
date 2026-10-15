package com.portfolio.controller;

import com.portfolio.dto.AnalyticsReportDTO;
import com.portfolio.model.PortfolioSnapshot;
import com.portfolio.service.AnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@Slf4j
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/report/{portfolioId}")
    public ResponseEntity<AnalyticsReportDTO> generateReport(@PathVariable Long portfolioId) {
        log.info("Generating analytics report for portfolio: {}", portfolioId);
        AnalyticsReportDTO report = analyticsService.generatePortfolioReport(portfolioId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/history/{portfolioId}")
    public ResponseEntity<List<PortfolioSnapshot>> getPortfolioHistory(
            @PathVariable Long portfolioId,
            @RequestParam(defaultValue = "30") int days) {
        log.info("Fetching portfolio history for {} days", days);
        List<PortfolioSnapshot> history = analyticsService.getPortfolioHistory(portfolioId, days);
        return ResponseEntity.ok(history);
    }
}
