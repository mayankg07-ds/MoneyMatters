package com.moneymatters.portfolio.dto;

import com.moneymatters.portfolio.entity.Holding;
import com.moneymatters.portfolio.entity.Transaction;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "User ID required")
    private Long userId;

    @NotNull(message = "Transaction type required")
    private Transaction.TransactionType transactionType;

    @NotNull(message = "Asset type required")
    private Holding.AssetType assetType;

    @NotBlank(message = "Asset name required")
    private String assetName;

    @NotBlank(message = "Asset symbol required")
    private String assetSymbol;

    @NotBlank(message = "Exchange required")
    private String exchange;

    @NotNull(message = "Quantity required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Price per unit required")
    @Positive(message = "Price per unit must be positive")
    private BigDecimal pricePerUnit;

    @PositiveOrZero(message = "Charges cannot be negative")
    private BigDecimal charges;

    @NotNull(message = "Transaction date required")
    private LocalDate transactionDate;

    private String notes;
}
