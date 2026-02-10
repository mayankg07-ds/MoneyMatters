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
public class CashflowRequest {

    @NotEmpty(message = "At least one income source required")
    @Valid
    private List<CashflowItem> incomes;

    @NotEmpty(message = "At least one expense required")
    @Valid
    private List<CashflowItem> expenses;

    @NotNull(message = "Projection years required")
    @Min(value = 1, message = "Projection years must be at least 1")
    @Max(value = 30, message = "Projection years cannot exceed 30")
    private Integer projectionYears;

    @NotNull(message = "Income growth rate required")
    @DecimalMin(value = "0.0", message = "Income growth cannot be negative")
    @DecimalMax(value = "50.0", message = "Income growth rate too high (max 50%)")
    private BigDecimal expectedIncomeGrowthPercent;

    @NotNull(message = "Expense growth rate required")
    @DecimalMin(value = "0.0", message = "Expense growth cannot be negative")
    @DecimalMax(value = "20.0", message = "Expense growth rate too high (max 20%)")
    private BigDecimal expectedExpenseGrowthPercent;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashflowItem {
        
        @NotBlank(message = "Item name required")
        private String name;
        
        @NotNull(message = "Amount required")
        @Positive(message = "Amount must be positive")
        private BigDecimal monthlyAmount;
        
        private String category;  // Optional: "Fixed", "Variable", "Discretionary"
    }
}

