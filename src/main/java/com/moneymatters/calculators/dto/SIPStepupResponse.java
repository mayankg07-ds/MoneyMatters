package com.moneymatters.calculators.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class SIPStepupResponse {

    private BigDecimal totalInvested;
    private BigDecimal maturityValue;
    private BigDecimal wealthGained;

    private BigDecimal firstYearMonthlySIP;
    private BigDecimal lastYearMonthlySIP;

    private List<YearlyBreakdown> yearlyBreakdown;
    private List<ChartPoint> maturityCurve;
}
