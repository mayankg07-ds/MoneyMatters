package com.moneymatters.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummaryResponse {

    private Long userId;
    private BigDecimal totalInvested;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalUnrealizedGain;
    private BigDecimal totalUnrealizedGainPercent;
    private Integer totalHoldings;
    private List<HoldingResponse> holdings;
    private List<AssetTypeBreakdown> assetTypeBreakdown;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetTypeBreakdown {
        private String assetType;
        private BigDecimal totalInvested;
        private BigDecimal currentValue;
        private BigDecimal allocation;  // Percentage
        private Integer count;
    }
}
