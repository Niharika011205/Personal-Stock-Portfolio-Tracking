package com.portfolio.service;

import com.portfolio.dto.PortfolioDTO;
import com.portfolio.dto.PortfolioAssetDTO;
import com.portfolio.model.Portfolio;
import com.portfolio.model.PortfolioAsset;
import com.portfolio.model.User;
import com.portfolio.repository.PortfolioRepository;
import com.portfolio.repository.PortfolioAssetRepository;
import com.portfolio.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository assetRepository;
    private final UserRepository userRepository;
    private final MarketDataService marketDataService;

    public PortfolioService(PortfolioRepository portfolioRepository,
                          PortfolioAssetRepository assetRepository,
                          UserRepository userRepository,
                          MarketDataService marketDataService) {
        this.portfolioRepository = portfolioRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.marketDataService = marketDataService;
    }

    public PortfolioDTO createPortfolio(Long userId, String name, String description) {
        log.info("Creating portfolio for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .name(name)
                .description(description)
                .build();

        Portfolio saved = portfolioRepository.save(portfolio);
        log.info("Portfolio created with ID: {}", saved.getId());
        return mapToDTO(saved);
    }

    public PortfolioDTO getPortfolioById(Long portfolioId, Long userId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));
        return mapToDTO(portfolio);
    }

    public List<PortfolioDTO> getUserPortfolios(Long userId) {
        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);
        return portfolios.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public PortfolioDTO updatePortfolioAssetPrices(Long portfolioId, Long userId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        List<PortfolioAsset> assets = assetRepository.findByPortfolioId(portfolioId);
        
        for (PortfolioAsset asset : assets) {
            BigDecimal currentPrice = marketDataService.getStockPrice(asset.getTicker());
            asset.updatePriceAndValue(currentPrice);
            assetRepository.save(asset);
        }

        log.info("Updated prices for {} assets in portfolio: {}", assets.size(), portfolioId);
        return mapToDTO(portfolio);
    }

    private PortfolioDTO mapToDTO(Portfolio portfolio) {
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        
        List<PortfolioAssetDTO> assetDTOs = portfolio.getAssets().stream()
                .map(this::mapAssetToDTO)
                .collect(Collectors.toList());

        for (PortfolioAsset asset : portfolio.getAssets()) {
            totalValue = totalValue.add(asset.getCurrentValue());
            totalCost = totalCost.add(asset.getTotalCost());
        }

        BigDecimal unrealizedPnL = totalValue.subtract(totalCost);
        BigDecimal returnPercentage = totalCost.compareTo(BigDecimal.ZERO) > 0 
                ? unrealizedPnL.divide(totalCost, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100))
                : BigDecimal.ZERO;

        return PortfolioDTO.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .description(portfolio.getDescription())
                .createdAt(portfolio.getCreatedAt().toString())
                .updatedAt(portfolio.getUpdatedAt().toString())
                .totalValue(totalValue)
                .totalCost(totalCost)
                .unrealizedPnL(unrealizedPnL)
                .returnPercentage(returnPercentage)
                .assetCount(portfolio.getAssets().size())
                .assets(assetDTOs)
                .build();
    }

    private PortfolioAssetDTO mapAssetToDTO(PortfolioAsset asset) {
        return PortfolioAssetDTO.builder()
                .id(asset.getId())
                .ticker(asset.getTicker())
                .quantity(asset.getQuantity())
                .averagePurchasePrice(asset.getAveragePurchasePrice())
                .currentPrice(asset.getCurrentPrice())
                .currentValue(asset.getCurrentValue())
                .totalCost(asset.getTotalCost())
                .unrealizedPnL(asset.getUnrealizedPnL())
                .returnPercentage(asset.getReturnPercentage())
                .updatedAt(asset.getUpdatedAt() != null ? asset.getUpdatedAt().toString() : "")
                .build();
    }
}
