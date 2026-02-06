package com.moneymatters.calculators.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class RetirementPlanResponse {

    // Inflation-adjusted values
    private BigDecimal inflatedMonthlyExpenseAtRetirement;
    private BigDecimal inflatedAnnualExpenseAtRetirement;

    // Corpus calculations
    private BigDecimal requiredCorpusAtRetirement;
    private BigDecimal projectedExistingCorpusAtRetirement;
    private BigDecimal corpusShortfall;

    // Recommendations
    private BigDecimal recommendedMonthlySIP;
    private BigDecimal totalSIPInvestmentNeeded;

    // Timeline
    private Integer yearsToRetirement;
    private Integer yearsInRetirement;

    // Projections
    private List<YearlyRetirementProjection> preRetirementProjections;
    private List<YearlyRetirementProjection> postRetirementProjections;

    // Charts
    private List<ChartPoint> corpusGrowthChart;
}

