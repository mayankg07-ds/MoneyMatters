package com.moneymatters.calculators.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanAnalysisRequest {

    @NotNull
    @Positive(message = "Principal must be positive")
    private BigDecimal principal;

    @NotNull
    @DecimalMin(value = "0.01", message = "Interest rate must be > 0")
    @DecimalMax(value = "50.0", message = "Interest rate cannot exceed 50%")
    private BigDecimal annualInterestRatePercent;

    @NotNull
    @Min(value = 1, message = "Tenure must be at least 1 month")
    @Max(value = 600, message = "Tenure cannot exceed 600 months (50 years)")
    private Integer tenureMonths;

    // Optional: Prepayment scenarios
    private List<PrepaymentScenario> prepayments;
}
