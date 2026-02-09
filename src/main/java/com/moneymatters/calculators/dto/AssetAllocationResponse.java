package com.moneymatters.calculators.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class AssetAllocationResponse {

    private BigDecimal totalPortfolioValue;
    private List<AssetAnalysis> assetAnalyses;
    private List<RebalancingAction> rebalancingActions;
    private BigDecimal totalBuyAmount;
    private BigDecimal totalSellAmount;
    private boolean isBalanced;
    private List<ChartPoint> allocationChart;

    @Data
    @AllArgsConstructor
    public static class AssetAnalysis {
        private String assetName;
        private BigDecimal currentValue;
        private BigDecimal currentPercentage;
        private BigDecimal targetPercentage;
        private BigDecimal drift;  // Difference from target
        private BigDecimal targetValue;
        private BigDecimal adjustmentNeeded;
    }

    @Data
    @AllArgsConstructor
    public static class RebalancingAction {
        private String assetName;
        private String action;  // "BUY", "SELL", "HOLD"
        private BigDecimal amount;
        private String recommendation;
    }
}
