package com.moneymatters.calculators.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class YearlyBreakdown {
    private Integer year;
    private BigDecimal monthlySIP;
    private BigDecimal yearlyContribution;
    private BigDecimal valueAtYearEnd;
    private BigDecimal valueAtMaturity; // valueAtYearEnd grown till final year
}

