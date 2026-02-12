package com.moneymatters.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    public enum TransactionType {
        BUY,
        SELL,
        DIVIDEND,
        BONUS,
        SPLIT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long holdingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Holding.AssetType assetType;

    @Column(length = 100)
    private String assetName;

    @Column(nullable = false, length = 50)
    private String assetSymbol;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal charges;

    @Column(precision = 18, scale = 2)
    private BigDecimal netAmount;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(length = 500)
    private String notes;

    @Column
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
