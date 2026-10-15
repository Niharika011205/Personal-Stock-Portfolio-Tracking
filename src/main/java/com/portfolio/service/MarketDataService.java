package com.portfolio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MarketDataService {
    private final RestTemplate restTemplate;
    private final Map<String, CachedPrice> priceCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 15 * 60 * 1000; // 15 minutes

    public MarketDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BigDecimal getStockPrice(String ticker) {
        log.debug("Fetching price for ticker: {}", ticker);
        
        CachedPrice cached = priceCache.get(ticker);
        if (cached != null && !cached.isExpired()) {
            log.debug("Returning cached price for: {}", ticker);
            return cached.price;
        }

        // Mock implementation - in production, integrate with real API (Alpha Vantage, Yahoo Finance, etc.)
        BigDecimal price = generateMockPrice(ticker);
        
        priceCache.put(ticker, new CachedPrice(price, System.currentTimeMillis()));
        return price;
    }

    private BigDecimal generateMockPrice(String ticker) {
        // Generate deterministic mock prices based on ticker hash
        int hash = ticker.hashCode();
        double basePrice = 50 + (hash % 200);
        double variation = (hash % 1000) / 100.0;
        return BigDecimal.valueOf(basePrice + variation);
    }

    public Map<String, BigDecimal> getStockPrices(java.util.List<String> tickers) {
        Map<String, BigDecimal> prices = new HashMap<>();
        for (String ticker : tickers) {
            prices.put(ticker, getStockPrice(ticker));
        }
        return prices;
    }

    public void clearCache() {
        priceCache.clear();
        log.info("Market data cache cleared");
    }

    private static class CachedPrice {
        BigDecimal price;
        long cachedAt;

        CachedPrice(BigDecimal price, long cachedAt) {
            this.price = price;
            this.cachedAt = cachedAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - cachedAt > CACHE_DURATION_MS;
        }
    }
}
