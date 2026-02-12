package com.moneymatters.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioAnalyticsResponse {

    // Overall Portfolio Metrics
    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private BigDecimal totalGain;
    private BigDecimal totalGainPercent;
    
    // Realized vs Unrealized
    private BigDecimal realizedGain;
    private BigDecimal unrealizedGain;
    
    // Performance Metrics
    private BigDecimal xirr;  // Extended IRR
    private BigDecimal absoluteReturn;
    private BigDecimal cagr;  // Compound Annual Growth Rate
    
    // Time Metrics
    private LocalDate firstInvestmentDate;
    private LocalDate lastTransactionDate;
    private Integer investmentDurationDays;
    private Double investmentDurationYears;
    
    // Asset Breakdown
    private List<AssetWiseAnalytics> assetWiseAnalytics;
    
    // Top Performers
    private List<TopPerformer> topGainers;
    private List<TopPerformer> topLosers;
    
    // Dividend Income
    private BigDecimal totalDividendReceived;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetWiseAnalytics {
        private String assetType;
        private BigDecimal invested;
        private BigDecimal currentValue;
        private BigDecimal gain;
        private BigDecimal gainPercent;
        private BigDecimal allocation;  // % of portfolio
        private Integer count;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformer {
        private String assetSymbol;
        private String assetName;
        private BigDecimal invested;
        private BigDecimal currentValue;
        private BigDecimal gain;
        private BigDecimal gainPercent;
    }
}
