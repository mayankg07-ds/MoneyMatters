package com.moneymatters.calculators.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class CashflowResponse {

    // Current month snapshot
    private BigDecimal currentMonthlyIncome;
    private BigDecimal currentMonthlyExpense;
    private BigDecimal currentNetCashflow;
    private BigDecimal currentSavingsRate;

    // Summary statistics
    private BigDecimal averageAnnualIncome;
    private BigDecimal averageAnnualExpense;
    private BigDecimal totalSavingsOverPeriod;
    private BigDecimal averageSavingsRate;

    // Yearly projections
    private List<YearlyCashflow> projections;

    // Income and expense breakdowns
    private List<ItemBreakdown> incomeBreakdown;
    private List<ItemBreakdown> expenseBreakdown;

    // Charts
    private List<ChartPoint> incomeVsExpenseChart;
    private List<ChartPoint> savingsChart;
    private List<ChartPoint> savingsRateChart;

    @Data
    @AllArgsConstructor
    public static class YearlyCashflow {
        private Integer year;
        private BigDecimal monthlyIncome;
        private BigDecimal monthlyExpense;
        private BigDecimal monthlyNetCashflow;
        private BigDecimal annualIncome;
        private BigDecimal annualExpense;
        private BigDecimal annualSavings;
        private BigDecimal savingsRate;
        private BigDecimal cumulativeSavings;
    }

    @Data
    @AllArgsConstructor
    public static class ItemBreakdown {
        private String name;
        private BigDecimal monthlyAmount;
        private BigDecimal percentage;
        private String category;
    }
}
