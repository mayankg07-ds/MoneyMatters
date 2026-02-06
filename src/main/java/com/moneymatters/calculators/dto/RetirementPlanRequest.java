package com.moneymatters.calculators.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetirementPlanRequest {

    @NotNull
    @Min(value = 18, message = "Current age must be at least 18")
    @Max(value = 100, message = "Current age cannot exceed 100")
    private Integer currentAge;

    @NotNull
    @Min(value = 30, message = "Retirement age must be at least 30")
    @Max(value = 100, message = "Retirement age cannot exceed 100")
    private Integer retirementAge;

    @NotNull
    @Min(value = 40, message = "Life expectancy must be at least 40")
    @Max(value = 120, message = "Life expectancy cannot exceed 120")
    private Integer lifeExpectancy;

    @NotNull
    @Positive(message = "Current monthly expense must be positive")
    private BigDecimal currentMonthlyExpense;

    @NotNull
    @DecimalMin(value = "0.0", message = "Inflation cannot be negative")
    @DecimalMax(value = "20.0", message = "Inflation rate too high (max 20%)")
    private BigDecimal expectedInflationPercent;

    @NotNull
    @DecimalMin(value = "0.01", message = "Pre-retirement return must be positive")
    @DecimalMax(value = "30.0", message = "Pre-retirement return too high (max 30%)")
    private BigDecimal expectedReturnPreRetirementPercent;

    @NotNull
    @DecimalMin(value = "0.01", message = "Post-retirement return must be positive")
    @DecimalMax(value = "20.0", message = "Post-retirement return too high (max 20%)")
    private BigDecimal expectedReturnPostRetirementPercent;

    @NotNull
    @PositiveOrZero(message = "Existing corpus cannot be negative")
    private BigDecimal existingCorpus;
}

