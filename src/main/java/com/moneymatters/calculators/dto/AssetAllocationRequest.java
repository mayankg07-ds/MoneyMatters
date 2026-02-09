package com.moneymatters.calculators.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetAllocationRequest {

    @NotEmpty(message = "At least one asset class required")
    @Valid
    private List<AssetClass> currentHoldings;

    @NotEmpty(message = "Target allocation required")
    @Valid
    private List<TargetAllocation> targetAllocations;

    @PositiveOrZero(message = "Fresh investment cannot be negative")
    private BigDecimal freshInvestment;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetClass {
        
        @NotBlank(message = "Asset name required")
        private String assetName;  // "Equity", "Debt", "Gold", "Cash"
        
        @NotNull
        @PositiveOrZero(message = "Current value cannot be negative")
        private BigDecimal currentValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetAllocation {
        
        @NotBlank(message = "Asset name required")
        private String assetName;
        
        @NotNull
        @DecimalMin(value = "0.0", message = "Target % cannot be negative")
        @DecimalMax(value = "100.0", message = "Target % cannot exceed 100")
        private BigDecimal targetPercentage;
    }
}
