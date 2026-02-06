package com.moneymatters.calculators.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PrepaymentImpact {
    private BigDecimal totalPrepaymentAmount;
    private BigDecimal interestSaved;
    private Integer monthsSaved;           // For REDUCE_TENURE
    private BigDecimal newEMI;             // For REDUCE_EMI
    private BigDecimal originalTotalCost;
    private BigDecimal newTotalCost;
}
