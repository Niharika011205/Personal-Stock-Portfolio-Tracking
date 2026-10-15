package com.portfolio.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "portfolio_snapshots", indexes = {
        @Index(name = "idx_portfolio_date", columnList = "portfolio_id,snapshot_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal totalValue;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal totalCost;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal unrealizedPnL;

    @Column(name = "asset_count")
    private Integer assetCount;

    @Column(name = "return_percentage", precision = 10, scale = 4)
    private BigDecimal returnPercentage;
}
