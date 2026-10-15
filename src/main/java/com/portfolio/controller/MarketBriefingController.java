package com.portfolio.controller;

import com.portfolio.dto.MarketBriefingDTO;
import com.portfolio.service.MarketBriefingAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/briefings")
@Slf4j
public class MarketBriefingController {
    private final MarketBriefingAgent marketBriefingAgent;

    public MarketBriefingController(MarketBriefingAgent marketBriefingAgent) {
        this.marketBriefingAgent = marketBriefingAgent;
    }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<MarketBriefingDTO> getMarketBriefing(@PathVariable Long portfolioId) {
        log.info("Generating market briefing for portfolio: {}", portfolioId);
        MarketBriefingDTO briefing = marketBriefingAgent.generateMarketBriefing(portfolioId);
        return ResponseEntity.ok(briefing);
    }
}
