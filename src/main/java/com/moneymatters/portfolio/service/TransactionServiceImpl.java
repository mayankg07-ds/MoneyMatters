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
    private final PortfolioAnalyticsService portfolioAnalyticsService;

    @Override
    @Transactional
    public TransactionResponse recordTransaction(String clerkUserId, TransactionRequest request) {
        log.info("Recording {} transaction for user {}: {}",
            request.getTransactionType(), clerkUserId, request.getAssetSymbol());

        Transaction transaction = new Transaction();
        transaction.setClerkUserId(clerkUserId);
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

        Transaction saved = transactionRepository.save(transaction);

        switch (request.getTransactionType()) {
            case BUY:
                handleBuyTransaction(clerkUserId, request);
                break;
            case SELL:
                handleSellTransaction(clerkUserId, request, saved);
                break;
            case DIVIDEND:
                break;
            case BONUS:
            case SPLIT:
                handleBonusOrSplit(clerkUserId, request);
                break;
        }

        log.info("Transaction recorded with ID: {}", saved.getId());

        portfolioAnalyticsService.clearAnalyticsCache(clerkUserId);

        return TransactionResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactions(String clerkUserId) {
        List<Transaction> transactions = transactionRepository
            .findByClerkUserIdOrderByTransactionDateDesc(clerkUserId);

        return transactions.stream()
            .map(TransactionResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsBySymbol(String clerkUserId, String assetSymbol) {
        List<Transaction> transactions = transactionRepository
            .findByClerkUserIdAndAssetSymbol(clerkUserId, assetSymbol);

        return transactions.stream()
            .map(TransactionResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByDateRange(
            String clerkUserId, LocalDate startDate, LocalDate endDate) {

        List<Transaction> transactions = transactionRepository
            .findByClerkUserIdAndDateRange(clerkUserId, startDate, endDate);

        return transactions.stream()
            .map(TransactionResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FIFOCalculationResult calculateFIFOGain(
            String clerkUserId, String assetSymbol, BigDecimal quantityToSell, BigDecimal salePrice) {

        log.info("Calculating FIFO gain for {} units of {}", quantityToSell, assetSymbol);

        List<Transaction> buyTransactions = transactionRepository
            .findBuyTransactionsForAsset(clerkUserId, assetSymbol);

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

        reverseTransactionEffect(transaction);

        transactionRepository.deleteById(transactionId);
        log.info("Transaction deleted: {}", transactionId);
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private void handleBuyTransaction(String clerkUserId, TransactionRequest request) {
        Holding holding = holdingRepository
            .findByClerkUserIdAndAssetSymbol(clerkUserId, request.getAssetSymbol())
            .orElse(null);

        if (holding == null) {
            HoldingRequest holdingRequest = new HoldingRequest(
                request.getAssetType(),
                request.getAssetName(),
                request.getAssetSymbol(),
                request.getExchange(),
                request.getQuantity(),
                request.getPricePerUnit(),
                request.getTransactionDate()
            );
            holdingService.createHolding(clerkUserId, holdingRequest);
        } else {
            BigDecimal transactionCost = request.getQuantity().multiply(request.getPricePerUnit());
            BigDecimal charges = request.getCharges() != null ? request.getCharges() : BigDecimal.ZERO;

            BigDecimal totalQty = holding.getQuantity().add(request.getQuantity());

            BigDecimal totalStockCost = holding.getAvgBuyPrice().multiply(holding.getQuantity())
                .add(transactionCost);
            BigDecimal newAvgPrice = totalStockCost.divide(totalQty, 2, RoundingMode.HALF_UP);

            BigDecimal totalCost = holding.getTotalInvested()
                .add(transactionCost)
                .add(charges);

            holding.setQuantity(totalQty);
            holding.setAvgBuyPrice(newAvgPrice);
            holding.setTotalInvested(totalCost);

            BigDecimal currentPrice = holding.getCurrentPrice() != null ?
                holding.getCurrentPrice() : newAvgPrice;
            BigDecimal currentValue = currentPrice.multiply(totalQty);
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

    private void handleSellTransaction(String clerkUserId, TransactionRequest request, Transaction transaction) {
        Holding holding = holdingRepository
            .findByClerkUserIdAndAssetSymbol(clerkUserId, request.getAssetSymbol())
            .orElseThrow(() -> new RuntimeException("No holding found for " + request.getAssetSymbol()));

        if (holding.getQuantity().compareTo(request.getQuantity()) < 0) {
            throw new RuntimeException("Insufficient quantity to sell");
        }

        FIFOCalculationResult fifoResult = calculateFIFOGain(
            clerkUserId,
            request.getAssetSymbol(),
            request.getQuantity(),
            request.getPricePerUnit()
        );

        BigDecimal newQty = holding.getQuantity().subtract(request.getQuantity());
        holding.setQuantity(newQty);

        if (newQty.compareTo(BigDecimal.ZERO) == 0) {
            holding.setTotalInvested(BigDecimal.ZERO);
            holding.setCurrentValue(BigDecimal.ZERO);
            holding.setUnrealizedGain(BigDecimal.ZERO);
            holding.setUnrealizedGainPercent(BigDecimal.ZERO);
        } else {
            BigDecimal costBasisRemoved = fifoResult.getTotalCostBasis();
            BigDecimal newTotalInvested = holding.getTotalInvested().subtract(costBasisRemoved);
            holding.setTotalInvested(newTotalInvested);

            BigDecimal currentPrice = holding.getCurrentPrice() != null ?
                holding.getCurrentPrice() : holding.getAvgBuyPrice();
            BigDecimal newCurrentValue = currentPrice.multiply(newQty);
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
        log.info("SELL completed. Realized gain: {}", fifoResult.getTotalRealizedGain());
    }

    private void handleBonusOrSplit(String clerkUserId, TransactionRequest request) {
        Holding holding = holdingRepository
            .findByClerkUserIdAndAssetSymbol(clerkUserId, request.getAssetSymbol())
            .orElseThrow(() -> new RuntimeException("No holding found for " + request.getAssetSymbol()));

        BigDecimal newQty = holding.getQuantity().add(request.getQuantity());
        holding.setQuantity(newQty);

        BigDecimal newAvgPrice = holding.getTotalInvested()
            .divide(newQty, 2, RoundingMode.HALF_UP);
        holding.setAvgBuyPrice(newAvgPrice);

        BigDecimal currentPrice = holding.getCurrentPrice() != null ?
            holding.getCurrentPrice() : newAvgPrice;
        BigDecimal currentValue = currentPrice.multiply(newQty);
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
        log.warn("Transaction reversal not fully implemented yet");
    }
}
