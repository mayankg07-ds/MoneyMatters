package com.moneymatters.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FIFOCalculationResult {

    private BigDecimal totalRealizedGain;
    private BigDecimal totalRealizedGainPercent;
    private BigDecimal totalSaleValue;
    private BigDecimal totalCostBasis;
    private List<FIFOBatch> batches;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FIFOBatch {
        private Long transactionId;
        private LocalDate purchaseDate;
        private BigDecimal quantitySold;
        private BigDecimal purchasePrice;
        private BigDecimal salePrice;
        private BigDecimal gain;
        private BigDecimal gainPercent;
    }
}
