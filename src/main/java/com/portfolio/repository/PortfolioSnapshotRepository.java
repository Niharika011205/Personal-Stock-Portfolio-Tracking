package com.portfolio.repository;

import com.portfolio.model.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {
    List<PortfolioSnapshot> findByPortfolioIdOrderBySnapshotDateDesc(Long portfolioId);
    
    @Query("SELECT ps FROM PortfolioSnapshot ps WHERE ps.portfolio.id = :portfolioId " +
           "AND ps.snapshotDate BETWEEN :startDate AND :endDate ORDER BY ps.snapshotDate ASC")
    List<PortfolioSnapshot> findSnapshotsByDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
