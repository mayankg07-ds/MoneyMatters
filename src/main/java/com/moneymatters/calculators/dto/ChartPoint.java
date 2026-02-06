package com.moneymatters.calculators.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ChartPoint {
    private String label;      // "Year 1", "Year 2", ...
    private BigDecimal value;  // cumulative maturity value
}
