package com.moneymatters.portfolio.service;

import com.moneymatters.portfolio.dto.*;
import com.moneymatters.portfolio.entity.Holding;
import com.moneymatters.portfolio.entity.Transaction;
import com.moneymatters.portfolio.repository.HoldingRepository;
import com.moneymatters.portfolio.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final HoldingRepository holdingRepository;
    private final HoldingService holdingService;

    @Override
    @Transactional
    public TransactionResponse recordTransaction(TransactionRequest request) {
        log.info("Recording {} transaction for user {}: {}",
            request.getTransactionType(), request.getUserId(), request.getAssetSymbol());

        // Create transaction entity
        Transaction transaction = new Transaction();
        transaction.setUserId(request.getUserId());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setAssetType(request.getAssetType());
        transaction.setAssetName(request.getAssetName());
        transaction.setAssetSymbol(request.getAssetSymbol());
        transaction.setQuantity(request.getQuantity());
        transaction.setPricePerUnit(request.getPricePerUnit());
        
        BigDecimal totalAmount = request.getQuantity()
            .multiply(request.getPricePerUnit())
            .setScale(2, RoundingMode.HALF_UP);
        transaction.setTotalAmount(totalAmount);
        
        BigDecimal charges = request.getCharges() != null ? 
            request.getCharges() : BigDecimal.ZERO;
        transaction.setCharges(charges);
        
        BigDecimal netAmount = totalAmount.add(charges);
        transaction.setNetAmount(netAmount);
        
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNotes(request.getNotes());

        // Save transaction
        Transaction saved = transactionRepository.save(transaction);

        // Update holding based on transaction type
        switch (request.getTransactionType()) {
            case BUY:
                handleBuyTransaction(request);
                break;
            case SELL:
                handleSellTransaction(request, saved);
                break;
            case DIVIDEND:
                // No holding update needed
                break;
            case BONUS:
            case SPLIT:
                handleBonusOrSplit(request);
                break;
        }

        log.info("Transaction recorded with ID: {}", saved.getId());
        return TransactionResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactions(Long userId) {
        List<Transaction> transactions = transactionRepository
            .findByUserIdOrderByTransactionDateDesc(userId);

        return transactions.stream()
            .map(TransactionResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsBySymbol(Long userId, String assetSymbol) {
        List<Transaction> transactions = transactionRepository
            .findByUserIdAndAssetSymbol(userId, assetSymbol);

        return transactions.stream()
            .map(TransactionResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {

        List<Transaction> transactions = transactionRepository
            .findByUserIdAndDateRange(userId, startDate, endDate);

        return transactions.stream()
            .map(TransactionResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FIFOCalculationResult calculateFIFOGain(
            Long userId, String assetSymbol, BigDecimal quantityToSell, BigDecimal salePrice) {

        log.info("Calculating FIFO gain for {} units of {}", quantityToSell, assetSymbol);

        // Get all BUY transactions for this asset, ordered by date (FIFO)
        List<Transaction> buyTransactions = transactionRepository
            .findBuyTransactionsForAsset(userId, assetSymbol);

        if (buyTransactions.isEmpty()) {
            throw new RuntimeException("No purchase history found for " + assetSymbol);
        }

        List<FIFOCalculationResult.FIFOBatch> batches = new ArrayList<>();
        BigDecimal remainingToSell = quantityToSell;
        BigDecimal totalCostBasis = BigDecimal.ZERO;
        BigDecimal totalSaleValue = BigDecimal.ZERO;

        for (Transaction buyTxn : buyTransactions) {
            if (remainingToSell.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal availableQty = buyTxn.getQuantity();
            BigDecimal qtyFromThisBatch = remainingToSell.min(availableQty);

            BigDecimal costBasis = qtyFromThisBatch.multiply(buyTxn.getPricePerUnit());
            BigDecimal saleValue = qtyFromThisBatch.multiply(salePrice);
            BigDecimal gain = saleValue.subtract(costBasis);
            BigDecimal gainPercent = costBasis.compareTo(BigDecimal.ZERO) > 0 ?
                gain.multiply(new BigDecimal(100)).divide(costBasis, 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

            batches.add(new FIFOCalculationResult.FIFOBatch(
                buyTxn.getId(),
                buyTxn.getTransactionDate(),
                qtyFromThisBatch,
                buyTxn.getPricePerUnit(),
                salePrice,
                gain,
                gainPercent
            ));

            totalCostBasis = totalCostBasis.add(costBasis);
            totalSaleValue = totalSaleValue.add(saleValue);
            remainingToSell = remainingToSell.subtract(qtyFromThisBatch);
        }

        if (remainingToSell.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException(
                "Insufficient holdings. Trying to sell " + quantityToSell + 
                " but only have purchase history for " + 
                quantityToSell.subtract(remainingToSell));
        }

        BigDecimal totalGain = totalSaleValue.subtract(totalCostBasis);
        BigDecimal totalGainPercent = totalCostBasis.compareTo(BigDecimal.ZERO) > 0 ?
            totalGain.multiply(new BigDecimal(100))
                .divide(totalCostBasis, 2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        return new FIFOCalculationResult(
            totalGain,
            totalGainPercent,
            totalSaleValue,
            totalCostBasis,
            batches
        );
    }

    @Override
    @Transactional
    public void deleteTransaction(Long transactionId) {
        log.info("Deleting transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        // Reverse the holding changes
        reverseTransactionEffect(transaction);

        transactionRepository.deleteById(transactionId);
        log.info("Transaction deleted: {}", transactionId);
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private void handleBuyTransaction(TransactionRequest request) {
        // Find or create holding
        Holding holding = holdingRepository
            .findByUserIdAndAssetSymbol(request.getUserId(), request.getAssetSymbol())
            .orElse(null);

        if (holding == null) {
            // Create new holding
            HoldingRequest holdingRequest = new HoldingRequest(
                request.getUserId(),
                request.getAssetType(),
                request.getAssetName(),
                request.getAssetSymbol(),
                request.getExchange(),
                request.getQuantity(),
                request.getPricePerUnit(),
                request.getTransactionDate()
            );
            holdingService.createHolding(holdingRequest);
        } else {
            // Update existing holding - recalculate average price
            BigDecimal totalCost = holding.getTotalInvested()
                .add(request.getQuantity().multiply(request.getPricePerUnit()));
            
            BigDecimal totalQty = holding.getQuantity().add(request.getQuantity());
            
            BigDecimal newAvgPrice = totalCost.divide(totalQty, 2, RoundingMode.HALF_UP);

            holding.setQuantity(totalQty);
            holding.setAvgBuyPrice(newAvgPrice);
            holding.setTotalInvested(totalCost);

            // Recalculate current value
            BigDecimal currentValue = holding.getCurrentPrice().multiply(totalQty);
            holding.setCurrentValue(currentValue);
            
            BigDecimal unrealizedGain = currentValue.subtract(totalCost);
            holding.setUnrealizedGain(unrealizedGain);
            
            BigDecimal unrealizedGainPercent = totalCost.compareTo(BigDecimal.ZERO) > 0 ?
                unrealizedGain.multiply(new BigDecimal(100))
                    .divide(totalCost, 4, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
            holding.setUnrealizedGainPercent(unrealizedGainPercent);

            holdingRepository.save(holding);
        }
    }

    private void handleSellTransaction(TransactionRequest request, Transaction transaction) {
        Holding holding = holdingRepository
            .findByUserIdAndAssetSymbol(request.getUserId(), request.getAssetSymbol())
            .orElseThrow(() -> new RuntimeException("No holding found for " + request.getAssetSymbol()));

        if (holding.getQuantity().compareTo(request.getQuantity()) < 0) {
            throw new RuntimeException("Insufficient quantity to sell");
        }

        // Calculate FIFO gain
        FIFOCalculationResult fifoResult = calculateFIFOGain(
            request.getUserId(),
            request.getAssetSymbol(),
            request.getQuantity(),
            request.getPricePerUnit()
        );

        // Update holding quantity
        BigDecimal newQty = holding.getQuantity().subtract(request.getQuantity());
        holding.setQuantity(newQty);

        if (newQty.compareTo(BigDecimal.ZERO) == 0) {
            // Sold everything
            holding.setTotalInvested(BigDecimal.ZERO);
            holding.setCurrentValue(BigDecimal.ZERO);
            holding.setUnrealizedGain(BigDecimal.ZERO);
            holding.setUnrealizedGainPercent(BigDecimal.ZERO);
        } else {
            // Recalculate proportionally
            BigDecimal costBasisRemoved = fifoResult.getTotalCostBasis();
            BigDecimal newTotalInvested = holding.getTotalInvested().subtract(costBasisRemoved);
            holding.setTotalInvested(newTotalInvested);

            BigDecimal newCurrentValue = holding.getCurrentPrice().multiply(newQty);
            holding.setCurrentValue(newCurrentValue);

            BigDecimal newUnrealizedGain = newCurrentValue.subtract(newTotalInvested);
            holding.setUnrealizedGain(newUnrealizedGain);

            BigDecimal newUnrealizedGainPercent = newTotalInvested.compareTo(BigDecimal.ZERO) > 0 ?
                newUnrealizedGain.multiply(new BigDecimal(100))
                    .divide(newTotalInvested, 4, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
            holding.setUnrealizedGainPercent(newUnrealizedGainPercent);
        }

        holdingRepository.save(holding);

        // Store realized gain in transaction (optional - add field to Transaction entity if needed)
        log.info("SELL completed. Realized gain: {}", fifoResult.getTotalRealizedGain());
    }

    private void handleBonusOrSplit(TransactionRequest request) {
        Holding holding = holdingRepository
            .findByUserIdAndAssetSymbol(request.getUserId(), request.getAssetSymbol())
            .orElseThrow(() -> new RuntimeException("No holding found for " + request.getAssetSymbol()));

        // For bonus/split, increase quantity without changing total invested
        BigDecimal newQty = holding.getQuantity().add(request.getQuantity());
        holding.setQuantity(newQty);

        // Recalculate average price (total invested stays same, quantity increased)
        BigDecimal newAvgPrice = holding.getTotalInvested()
            .divide(newQty, 2, RoundingMode.HALF_UP);
        holding.setAvgBuyPrice(newAvgPrice);

        // Recalculate current value
        BigDecimal currentValue = holding.getCurrentPrice().multiply(newQty);
        holding.setCurrentValue(currentValue);

        BigDecimal unrealizedGain = currentValue.subtract(holding.getTotalInvested());
        holding.setUnrealizedGain(unrealizedGain);

        BigDecimal unrealizedGainPercent = holding.getTotalInvested().compareTo(BigDecimal.ZERO) > 0 ?
            unrealizedGain.multiply(new BigDecimal(100))
                .divide(holding.getTotalInvested(), 4, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;
        holding.setUnrealizedGainPercent(unrealizedGainPercent);

        holdingRepository.save(holding);
    }

    private void reverseTransactionEffect(Transaction transaction) {
        // Implementation depends on transaction type
        // For BUY: reduce quantity from holding
        // For SELL: add back quantity to holding
        // This is complex - implement based on your requirements
        log.warn("Transaction reversal not fully implemented yet");
    }
}
