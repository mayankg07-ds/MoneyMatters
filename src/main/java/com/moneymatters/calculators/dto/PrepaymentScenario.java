package com.moneymatters.calculators.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrepaymentScenario {

    @NotNull
    @Positive(message = "Prepayment month must be positive")
    private Integer atMonth;

    @NotNull
    @Positive(message = "Prepayment amount must be positive")
    private BigDecimal amount;

    @NotNull
    private PrepaymentOption option;

    public enum PrepaymentOption {
        REDUCE_TENURE,    // Keep EMI same, reduce months
        REDUCE_EMI        // Keep tenure same, reduce EMI
    }
}
