package com.moneymatters.calculators.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SWPRequest {

    @NotNull(message = "Starting corpus required")
    @Positive(message = "Starting corpus must be positive")
    private BigDecimal startingCorpus;

    @NotNull(message = "Monthly withdrawal amount required")
    @Positive(message = "Monthly withdrawal must be positive")
    private BigDecimal monthlyWithdrawal;

    @NotNull(message = "Expected return required")
    @DecimalMin(value = "0.01", message = "Expected return must be positive")
    @DecimalMax(value = "30.0", message = "Expected return too high (max 30%)")
    private BigDecimal expectedAnnualReturnPercent;

    @NotNull(message = "Duration required")
    @Min(value = 1, message = "Duration must be at least 1 year")
    @Max(value = 50, message = "Duration cannot exceed 50 years")
    private Integer durationYears;

    // Optional: Inflation adjustment on withdrawals
    @DecimalMin(value = "0.0", message = "Inflation cannot be negative")
    @DecimalMax(value = "20.0", message = "Inflation rate too high")
    private BigDecimal inflationPercent;

    // Optional: Determine if withdrawals should increase with inflation
    private Boolean inflationAdjusted;

    public Boolean getInflationAdjusted() {
        return inflationAdjusted != null ? inflationAdjusted : false;
    }

    public BigDecimal getInflationPercent() {
        return inflationPercent != null ? inflationPercent : BigDecimal.ZERO;
    }
}
