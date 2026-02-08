package com.moneymatters.calculators.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class YearlyRetirementProjection {
    private Integer year;
    private Integer age;
    private BigDecimal corpusAtStart;
    private BigDecimal sipContribution;       // Pre-retirement only
    private BigDecimal withdrawalAmount;      // Post-retirement only
    private BigDecimal investmentReturn;
    private BigDecimal corpusAtEnd;
}

