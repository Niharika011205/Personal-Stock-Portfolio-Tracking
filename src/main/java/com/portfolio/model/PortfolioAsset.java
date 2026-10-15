package com.portfolio.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_assets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"portfolio_id", "ticker"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false)
    private String ticker;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(name = "average_purchase_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal averagePurchasePrice;

    @Column(name = "current_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "current_value", nullable = false, precision = 20, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "total_cost", nullable = false, precision = 20, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "unrealized_pnl", nullable = false, precision = 20, scale = 2)
    private BigDecimal unrealizedPnL;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePriceAndValue(BigDecimal newPrice) {
        this.currentPrice = newPrice;
        this.currentValue = quantity.multiply(newPrice);
        this.unrealizedPnL = currentValue.subtract(totalCost);
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getReturnPercentage() {
        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return unrealizedPnL.divide(totalCost, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
    }
}
