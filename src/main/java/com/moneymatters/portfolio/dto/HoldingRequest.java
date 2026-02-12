package com.moneymatters.portfolio.dto;

import com.moneymatters.portfolio.entity.Holding;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldingRequest {

    @NotNull(message = "User ID required")
    private Long userId;

    @NotNull(message = "Asset type required")
    private Holding.AssetType assetType;

    @NotBlank(message = "Asset name required")
    private String assetName;

    @NotBlank(message = "Asset symbol required")
    private String assetSymbol;

    @NotBlank(message = "Exchange required")
    private String exchange;  // "NSE", "BSE"

    @NotNull(message = "Quantity required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Average buy price required")
    @Positive(message = "Average buy price must be positive")
    private BigDecimal avgBuyPrice;

    private LocalDate purchaseDate;
}
