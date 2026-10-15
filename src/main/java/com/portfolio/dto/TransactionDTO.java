package com.portfolio.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private Long id;
    private String transactionType;
    private String ticker;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private String transactionDate;
    private String createdAt;
    private String notes;
}
