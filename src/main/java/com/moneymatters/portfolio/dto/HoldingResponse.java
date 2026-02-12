package com.moneymatters.portfolio.dto;

import com.moneymatters.portfolio.entity.Holding;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldingResponse {

    private Long id;
    private Long userId;
    private Holding.AssetType assetType;
    private String assetName;
    private String assetSymbol;
    private String exchange;
    private BigDecimal quantity;
    private BigDecimal avgBuyPrice;
    private BigDecimal totalInvested;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal unrealizedGain;
    private BigDecimal unrealizedGainPercent;
    private LocalDate purchaseDate;
    private LocalDateTime lastUpdated;

    // Helper method to convert entity to DTO
    public static HoldingResponse fromEntity(Holding holding) {
        return new HoldingResponse(
            holding.getId(),
            holding.getUserId(),
            holding.getAssetType(),
            holding.getAssetName(),
            holding.getAssetSymbol(),
            holding.getExchange(),
            holding.getQuantity(),
            holding.getAvgBuyPrice(),
            holding.getTotalInvested(),
            holding.getCurrentPrice(),
            holding.getCurrentValue(),
            holding.getUnrealizedGain(),
            holding.getUnrealizedGainPercent(),
            holding.getPurchaseDate(),
            holding.getLastUpdated()
        );
    }
}
