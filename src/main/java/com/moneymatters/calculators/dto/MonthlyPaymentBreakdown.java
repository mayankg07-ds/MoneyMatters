package com.moneymatters.calculators.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlyPaymentBreakdown {
    private Integer month;
    private Integer year;
    private BigDecimal openingBalance;
    private BigDecimal emi;
    private BigDecimal interestPaid;
    private BigDecimal principalPaid;
    private BigDecimal closingBalance;
    private BigDecimal cumulativeInterest;
    private BigDecimal cumulativePrincipal;
}

