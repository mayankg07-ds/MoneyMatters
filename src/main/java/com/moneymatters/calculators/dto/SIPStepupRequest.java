package com.moneymatters.calculators.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SIPStepupRequest {

    @NotNull
    @Positive(message = "Monthly SIP must be > 0")
    private java.math.BigDecimal monthlySIP;

    @NotNull
    @DecimalMin(value = "0.01", message = "Expected return must be > 0")
    @DecimalMax(value = "50.00", message = "Expected return must be <= 50%")
    private java.math.BigDecimal expectedAnnualReturnPercent;

    @NotNull
    @Positive(message = "Years must be > 0")
    @Max(value = 50, message = "Years cannot exceed 50")
    private Integer years;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "Step-up cannot be negative")
    @DecimalMax(value = "50.0", message = "Step-up cannot exceed 50%")
    private java.math.BigDecimal annualStepupPercent;
}
