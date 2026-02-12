package com.moneymatters.portfolio.dto;

import com.moneymatters.portfolio.entity.Holding;
import com.moneymatters.portfolio.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private Long userId;
    private Long holdingId;
    private Transaction.TransactionType transactionType;
    private Holding.AssetType assetType;
    private String assetName;
    private String assetSymbol;
    private String exchange;
    private BigDecimal quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalAmount;
    private BigDecimal charges;
    private BigDecimal netAmount;
    private LocalDate transactionDate;
    private String notes;

    // For SELL transactions - realized gain/loss
    private BigDecimal realizedGain;
    private BigDecimal realizedGainPercent;

    public static TransactionResponse fromEntity(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getUserId(),
            transaction.getHoldingId(),
            transaction.getTransactionType(),
            transaction.getAssetType(),
            transaction.getAssetName(),
            transaction.getAssetSymbol(),
            null, // exchange not in Transaction entity
            transaction.getQuantity(),
            transaction.getPricePerUnit(),
            transaction.getTotalAmount(),
            transaction.getCharges(),
            transaction.getNetAmount(),
            transaction.getTransactionDate(),
            transaction.getNotes(),
            null, // calculated separately for SELL
            null
        );
    }
}
