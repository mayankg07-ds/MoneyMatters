package com.moneymatters.calculators.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class SWPResponse {

    // Summary
    private BigDecimal startingCorpus;
    private BigDecimal initialMonthlyWithdrawal;
    private BigDecimal finalCorpusValue;
    private BigDecimal totalWithdrawn;
    private BigDecimal totalReturnsEarned;
    private Integer effectiveDurationMonths;

    // Sustainability analysis
    private Boolean isSustainable;
    private String sustainabilityMessage;
    private BigDecimal withdrawalRate;  // Annual withdrawal as % of corpus
    private BigDecimal safeWithdrawalRate;  // 4% rule or calculated safe rate

    // Detailed breakdown
    private List<MonthlyWithdrawalBreakdown> monthlyBreakdown;
    private List<YearlyWithdrawalSummary> yearlySummary;

    // Charts
    private List<ChartPoint> corpusOverTimeChart;
    private List<ChartPoint> withdrawalOverTimeChart;

    @Data
    @AllArgsConstructor
    public static class MonthlyWithdrawalBreakdown {
        private Integer month;
        private Integer year;
        private BigDecimal openingBalance;
        private BigDecimal investmentReturn;
        private BigDecimal withdrawalAmount;
        private BigDecimal closingBalance;
        private BigDecimal netChange;
    }

    @Data
    @AllArgsConstructor
    public static class YearlyWithdrawalSummary {
        private Integer year;
        private BigDecimal startingCorpus;
        private BigDecimal totalReturns;
        private BigDecimal totalWithdrawals;
        private BigDecimal endingCorpus;
        private BigDecimal avgMonthlyWithdrawal;
        private Boolean corpusGrowing;
    }
}
