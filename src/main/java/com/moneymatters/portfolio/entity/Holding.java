package com.moneymatters.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "holdings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {

    public enum AssetType {
        STOCK,
        MUTUAL_FUND,
        ETF,
        BOND,
        GOLD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String assetSymbol;

    @Column(length = 100)
    private String assetName;

    @Column(length = 10)
    private String exchange;  // "NSE", "BSE", "NASDAQ", etc.

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal avgBuyPrice;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalInvested;

    @Column(precision = 18, scale = 2)
    private BigDecimal currentPrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal currentValue;

    @Column(precision = 18, scale = 2)
    private BigDecimal unrealizedGain;

    @Column(precision = 10, scale = 4)
    private BigDecimal unrealizedGainPercent;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AssetType assetType;

    @Column
    private LocalDate purchaseDate;

    @Column
    @Builder.Default
    private boolean active = true;

    @Column
    private LocalDateTime lastUpdated;

    @Column
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }
}
