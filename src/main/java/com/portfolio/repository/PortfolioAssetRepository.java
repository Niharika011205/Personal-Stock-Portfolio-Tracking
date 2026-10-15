package com.portfolio.repository;

import com.portfolio.model.PortfolioAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioAssetRepository extends JpaRepository<PortfolioAsset, Long> {
    List<PortfolioAsset> findByPortfolioId(Long portfolioId);
    Optional<PortfolioAsset> findByPortfolioIdAndTicker(Long portfolioId, String ticker);
    
    @Query("SELECT DISTINCT pa.ticker FROM PortfolioAsset pa WHERE pa.portfolio.id = :portfolioId")
    List<String> findDistinctTickersByPortfolioId(@Param("portfolioId") Long portfolioId);
}
