package com.moneymatters.calculators.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class LoanAnalysisResponse {

    // Basic EMI Info
    private BigDecimal emi;
    private BigDecimal totalAmountPayable;
    private BigDecimal totalInterestPayable;
    private Integer effectiveTenureMonths;

    // Breakdown
    private BigDecimal principalAmount;
    private BigDecimal interestPercentage;

    // Amortization Schedule
    private List<MonthlyPaymentBreakdown> amortizationSchedule;

    // Prepayment Analysis
    private PrepaymentImpact prepaymentImpact;

    // Charts
    private List<ChartPoint> principalVsInterestChart;
    private List<ChartPoint> balanceOverTimeChart;
}
