package com.moneymatters.portfolio.service;

import com.moneymatters.portfolio.dto.PortfolioAnalyticsResponse;
import com.moneymatters.portfolio.entity.Holding;
import com.moneymatters.portfolio.entity.Transaction;
import com.moneymatters.portfolio.repository.HoldingRepository;
import com.moneymatters.portfolio.repository.TransactionRepository;
import com.moneymatters.portfolio.util.XIRRCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioAnalyticsServiceImpl implements PortfolioAnalyticsService {

    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public PortfolioAnalyticsResponse getPortfolioAnalytics(Long userId) {
        log.info("Generating portfolio analytics for user: {}", userId);

        // Get all holdings
        List<Holding> holdings = holdingRepository.findActiveHoldingsByUserId(userId);

        // Get all transactions
        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        if (holdings.isEmpty() && transactions.isEmpty()) {
            return createEmptyAnalytics(userId);
        }

        // Calculate overall metrics
        BigDecimal totalInvested = holdings.stream()
            .map(Holding::getTotalInvested)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentValue = holdings.stream()
            .map(Holding::getCurrentValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unrealizedGain = holdings.stream()
            .map(Holding::getUnrealizedGain)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate realized gains from SELL transactions
        BigDecimal realizedGain = calculateRealizedGain(transactions);

        // Calculate total dividend received
        BigDecimal totalDividend = transactions.stream()
            .filter(t -> t.getTransactionType() == Transaction.TransactionType.DIVIDEND)
            .map(Transaction::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGain = realizedGain.add(unrealizedGain);
        
        BigDecimal totalGainPercent = totalInvested.compareTo(BigDecimal.ZERO) > 0 ?
            totalGain.multiply(new BigDecimal(100))
                .divide(totalInvested, 4, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        // Time metrics
        LocalDate firstInvestmentDate = transactions.stream()
            .filter(t -> t.getTransactionType() == Transaction.TransactionType.BUY)
            .map(Transaction::getTransactionDate)
            .min(LocalDate::compareTo)
            .orElse(LocalDate.now());

        LocalDate lastTransactionDate = transactions.stream()
            .map(Transaction::getTransactionDate)
            .max(LocalDate::compareTo)
            .orElse(LocalDate.now());

        long durationDays = ChronoUnit.DAYS.between(firstInvestmentDate, LocalDate.now());
        double durationYears = durationDays / 365.0;

        // Calculate XIRR
        BigDecimal xirr = calculateXIRR(userId, transactions, holdings, currentValue);

        // Calculate CAGR
        BigDecimal cagr = durationYears > 0 ?
            XIRRCalculator.calculateCAGR(totalInvested, currentValue, durationYears) :
            BigDecimal.ZERO;

        // Calculate absolute return
        BigDecimal absoluteReturn = XIRRCalculator.calculateAbsoluteReturn(
            totalInvested, currentValue);

        // Asset-wise analytics
        List<PortfolioAnalyticsResponse.AssetWiseAnalytics> assetWiseAnalytics = 
            calculateAssetWiseAnalytics(holdings, currentValue);

        // Top performers
        List<PortfolioAnalyticsResponse.TopPerformer> topGainers = 
            getTopPerformers(holdings, true, 5);
        List<PortfolioAnalyticsResponse.TopPerformer> topLosers = 
            getTopPerformers(holdings, false, 5);

        return new PortfolioAnalyticsResponse(
            totalInvested,
            currentValue,
            totalGain,
            totalGainPercent,
            realizedGain,
            unrealizedGain,
            xirr,
            absoluteReturn,
            cagr,
            firstInvestmentDate,
            lastTransactionDate,
            (int) durationDays,
            durationYears,
            assetWiseAnalytics,
            topGainers,
            topLosers,
            totalDividend
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioAnalyticsResponse getPortfolioAnalyticsForDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {

        // Filter transactions by date range
        List<Transaction> transactions = transactionRepository
            .findByUserIdAndDateRange(userId, startDate, endDate);

        // Get holdings (current state)
        List<Holding> holdings = holdingRepository.findActiveHoldingsByUserId(userId);

        // Similar calculation as above, but filtered by date range
        // Implementation similar to getPortfolioAnalytics()
        // For brevity, returning full analytics
        return getPortfolioAnalytics(userId);
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private BigDecimal calculateXIRR(Long userId, List<Transaction> transactions, 
                                     List<Holding> holdings, BigDecimal currentValue) {
        try {
            List<LocalDate> dates = new ArrayList<>();
            List<BigDecimal> amounts = new ArrayList<>();

            // Add all BUY transactions (negative cash flow)
            transactions.stream()
                .filter(t -> t.getTransactionType() == Transaction.TransactionType.BUY)
                .forEach(t -> {
                    dates.add(t.getTransactionDate());
                    amounts.add(t.getNetAmount().negate()); // Negative for outflow
                });

            // Add all SELL transactions (positive cash flow)
            transactions.stream()
                .filter(t -> t.getTransactionType() == Transaction.TransactionType.SELL)
                .forEach(t -> {
                    dates.add(t.getTransactionDate());
                    amounts.add(t.getNetAmount()); // Positive for inflow
                });

            // Add current portfolio value as final cash flow (if holdings exist)
            if (!holdings.isEmpty() && currentValue.compareTo(BigDecimal.ZERO) > 0) {
                dates.add(LocalDate.now());
                amounts.add(currentValue); // Current value as inflow
            }

            if (dates.size() < 2) {
                return BigDecimal.ZERO;
            }

            return XIRRCalculator.calculateXIRR(dates, amounts);

        } catch (Exception e) {
            log.error("Error calculating XIRR for user {}: {}", userId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateRealizedGain(List<Transaction> transactions) {
        // For now, simplified calculation
        // In real implementation, you'd track FIFO cost basis for each SELL
        
        BigDecimal totalSaleValue = transactions.stream()
            .filter(t -> t.getTransactionType() == Transaction.TransactionType.SELL)
            .map(Transaction::getNetAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // This is simplified - real calculation needs FIFO cost basis
        // For accurate realized gain, integrate with TransactionService.calculateFIFOGain()
        
        return BigDecimal.ZERO; // Placeholder
    }

    private List<PortfolioAnalyticsResponse.AssetWiseAnalytics> calculateAssetWiseAnalytics(
            List<Holding> holdings, BigDecimal totalValue) {

        Map<Holding.AssetType, List<Holding>> holdingsByType = holdings.stream()
            .collect(Collectors.groupingBy(Holding::getAssetType));

        List<PortfolioAnalyticsResponse.AssetWiseAnalytics> analytics = new ArrayList<>();

        for (Map.Entry<Holding.AssetType, List<Holding>> entry : holdingsByType.entrySet()) {
            Holding.AssetType assetType = entry.getKey();
            List<Holding> typeHoldings = entry.getValue();

            BigDecimal invested = typeHoldings.stream()
                .map(Holding::getTotalInvested)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal currentValue = typeHoldings.stream()
                .map(Holding::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal gain = currentValue.subtract(invested);

            BigDecimal gainPercent = invested.compareTo(BigDecimal.ZERO) > 0 ?
                gain.multiply(new BigDecimal(100))
                    .divide(invested, 4, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

            BigDecimal allocation = totalValue.compareTo(BigDecimal.ZERO) > 0 ?
                currentValue.multiply(new BigDecimal(100))
                    .divide(totalValue, 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

            analytics.add(new PortfolioAnalyticsResponse.AssetWiseAnalytics(
                assetType.name(),
                invested,
                currentValue,
                gain,
                gainPercent,
                allocation,
                typeHoldings.size()
            ));
        }

        return analytics;
    }

    private List<PortfolioAnalyticsResponse.TopPerformer> getTopPerformers(
            List<Holding> holdings, boolean gainers, int limit) {

        return holdings.stream()
            .filter(h -> h.getQuantity().compareTo(BigDecimal.ZERO) > 0)
            .sorted((h1, h2) -> {
                int comparison = h1.getUnrealizedGainPercent()
                    .compareTo(h2.getUnrealizedGainPercent());
                return gainers ? -comparison : comparison; // Descending for gainers
            })
            .limit(limit)
            .map(h -> new PortfolioAnalyticsResponse.TopPerformer(
                h.getAssetSymbol(),
                h.getAssetName(),
                h.getTotalInvested(),
                h.getCurrentValue(),
                h.getUnrealizedGain(),
                h.getUnrealizedGainPercent()
            ))
            .collect(Collectors.toList());
    }

    private PortfolioAnalyticsResponse createEmptyAnalytics(Long userId) {
        return new PortfolioAnalyticsResponse(
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, LocalDate.now(), LocalDate.now(), 0, 0.0,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), BigDecimal.ZERO
        );
    }
}
