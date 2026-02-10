package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.*;
import com.moneymatters.calculators.util.CalculationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SWPCalculatorServiceImpl implements SWPCalculatorService {

    private static final int SCALE = 10;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Override
    public SWPResponse calculateSWP(SWPRequest request) {
        log.info("Calculating SWP: Corpus={}, Withdrawal={}, Duration={} years",
            request.getStartingCorpus(),
            request.getMonthlyWithdrawal(),
            request.getDurationYears());

        // Calculate monthly return rate
        BigDecimal monthlyReturnRate = CalculationUtils.percentToDecimal(
            request.getExpectedAnnualReturnPercent()
        ).divide(new BigDecimal(12), SCALE, ROUNDING);

        // Calculate monthly inflation factor (if enabled)
        BigDecimal monthlyInflationFactor = BigDecimal.ONE;
        if (request.getInflationAdjusted()) {
            // Monthly compounding: (1 + annual_rate/12)
            monthlyInflationFactor = BigDecimal.ONE.add(
                CalculationUtils.percentToDecimal(request.getInflationPercent())
                    .divide(new BigDecimal(12), SCALE, ROUNDING)
            );
        }

        // Generate month-by-month breakdown
        List<SWPResponse.MonthlyWithdrawalBreakdown> monthlyBreakdown = 
            generateMonthlyBreakdown(
                request.getStartingCorpus(),
                request.getMonthlyWithdrawal(),
                monthlyReturnRate,
                request.getDurationYears() * 12,
                monthlyInflationFactor
            );

        // Generate yearly summary
        List<SWPResponse.YearlyWithdrawalSummary> yearlySummary = 
            generateYearlySummary(monthlyBreakdown, request.getDurationYears());

        // Calculate totals
        BigDecimal totalWithdrawn = monthlyBreakdown.stream()
            .map(SWPResponse.MonthlyWithdrawalBreakdown::getWithdrawalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReturns = monthlyBreakdown.stream()
            .map(SWPResponse.MonthlyWithdrawalBreakdown::getInvestmentReturn)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalCorpus = monthlyBreakdown.isEmpty()
            ? request.getStartingCorpus()
            : monthlyBreakdown.get(monthlyBreakdown.size() - 1).getClosingBalance();

        // Sustainability analysis
        int requestedMonths = request.getDurationYears() * 12;
        int effectiveMonths = monthlyBreakdown.size();
        
        
        BigDecimal withdrawalRate = CalculationUtils.safeDivide(
            request.getMonthlyWithdrawal().multiply(new BigDecimal(12)).multiply(new BigDecimal(100)),
            request.getStartingCorpus()
        );

        BigDecimal safeWithdrawalRate = calculateSafeWithdrawalRate(
            request.getExpectedAnnualReturnPercent(),
            request.getInflationPercent()
        );

        // Sustainable if: corpus lasted full duration AND final corpus > 0
        boolean isSustainable = effectiveMonths >= requestedMonths && 
            finalCorpus.compareTo(BigDecimal.ZERO) > 0;

        String sustainabilityMessage = generateSustainabilityMessage(
            isSustainable,
            finalCorpus,
            request.getStartingCorpus(),
            withdrawalRate,
            safeWithdrawalRate
        );

        // Generate charts
        List<ChartPoint> corpusChart = generateCorpusChart(monthlyBreakdown);
        List<ChartPoint> withdrawalChart = generateWithdrawalChart(monthlyBreakdown);

        return new SWPResponse(
            CalculationUtils.format(request.getStartingCorpus()),
            CalculationUtils.format(request.getMonthlyWithdrawal()),
            CalculationUtils.format(finalCorpus),
            CalculationUtils.format(totalWithdrawn),
            CalculationUtils.format(totalReturns),
            monthlyBreakdown.size(),
            isSustainable,
            sustainabilityMessage,
            CalculationUtils.format(withdrawalRate),
            CalculationUtils.format(safeWithdrawalRate),
            monthlyBreakdown,
            yearlySummary,
            corpusChart,
            withdrawalChart
        );
    }

    private List<SWPResponse.MonthlyWithdrawalBreakdown> generateMonthlyBreakdown(
            BigDecimal startingCorpus,
            BigDecimal initialWithdrawal,
            BigDecimal monthlyReturnRate,
            int totalMonths,
            BigDecimal monthlyInflationFactor) {

        List<SWPResponse.MonthlyWithdrawalBreakdown> breakdown = new ArrayList<>();
        
        BigDecimal corpus = startingCorpus;
        BigDecimal currentWithdrawal = initialWithdrawal;

        for (int month = 1; month <= totalMonths; month++) {
            
            // Check if corpus is exhausted
            if (corpus.compareTo(new BigDecimal("1")) < 0) {
                log.warn("Corpus exhausted at month {}", month);
                break;
            }

            BigDecimal openingBalance = corpus;

            // Calculate investment return for this month
            BigDecimal investmentReturn = corpus.multiply(monthlyReturnRate);

            // Apply monthly inflation adjustment
            if (month > 1) {
                currentWithdrawal = currentWithdrawal.multiply(monthlyInflationFactor)
                    .setScale(SCALE, ROUNDING);
            }

            // Ensure withdrawal doesn't exceed corpus
            BigDecimal actualWithdrawal = currentWithdrawal;
            if (actualWithdrawal.compareTo(corpus) > 0) {
                actualWithdrawal = corpus;
            }

            // Calculate closing balance
            corpus = corpus.add(investmentReturn).subtract(actualWithdrawal);
            if (corpus.compareTo(BigDecimal.ZERO) < 0) {
                corpus = BigDecimal.ZERO;
            }

            BigDecimal netChange = investmentReturn.subtract(actualWithdrawal);

            breakdown.add(new SWPResponse.MonthlyWithdrawalBreakdown(
                month,
                (month - 1) / 12 + 1,
                CalculationUtils.format(openingBalance),
                CalculationUtils.format(investmentReturn),
                CalculationUtils.format(actualWithdrawal),
                CalculationUtils.format(corpus),
                CalculationUtils.format(netChange)
            ));

            // Stop if corpus depleted
            if (corpus.compareTo(new BigDecimal("1")) < 0) {
                break;
            }
        }

        return breakdown;
    }

    private List<SWPResponse.YearlyWithdrawalSummary> generateYearlySummary(
            List<SWPResponse.MonthlyWithdrawalBreakdown> monthlyBreakdown,
            int requestedYears) {

        List<SWPResponse.YearlyWithdrawalSummary> summary = new ArrayList<>();

        int actualYears = (monthlyBreakdown.size() + 11) / 12;

        for (int year = 1; year <= actualYears; year++) {
            int startMonth = (year - 1) * 12;
            int endMonth = Math.min(year * 12, monthlyBreakdown.size());

            if (startMonth >= monthlyBreakdown.size()) {
                break;
            }

            List<SWPResponse.MonthlyWithdrawalBreakdown> yearMonths = 
                monthlyBreakdown.subList(startMonth, endMonth);

            BigDecimal startingCorpus = yearMonths.get(0).getOpeningBalance();
            
            BigDecimal totalReturns = yearMonths.stream()
                .map(SWPResponse.MonthlyWithdrawalBreakdown::getInvestmentReturn)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalWithdrawals = yearMonths.stream()
                .map(SWPResponse.MonthlyWithdrawalBreakdown::getWithdrawalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal endingCorpus = yearMonths.get(yearMonths.size() - 1)
                .getClosingBalance();

            BigDecimal avgWithdrawal = CalculationUtils.safeDivide(
                totalWithdrawals,
                new BigDecimal(yearMonths.size())
            );

            boolean corpusGrowing = endingCorpus.compareTo(startingCorpus) > 0;

            summary.add(new SWPResponse.YearlyWithdrawalSummary(
                year,
                CalculationUtils.format(startingCorpus),
                CalculationUtils.format(totalReturns),
                CalculationUtils.format(totalWithdrawals),
                CalculationUtils.format(endingCorpus),
                CalculationUtils.format(avgWithdrawal),
                corpusGrowing
            ));
        }

        return summary;
    }

    private BigDecimal calculateSafeWithdrawalRate(
            BigDecimal expectedReturn,
            BigDecimal inflation) {

        // Safe withdrawal rate = Expected return - Inflation
        // Minimum 3%, maximum 6% (standard retirement planning guidelines)
        BigDecimal safeRate = expectedReturn.subtract(inflation);

        if (safeRate.compareTo(new BigDecimal("3")) < 0) {
            safeRate = new BigDecimal("3");
        }
        if (safeRate.compareTo(new BigDecimal("6")) > 0) {
            safeRate = new BigDecimal("6");
        }

        return safeRate;
    }

    private String generateSustainabilityMessage(
            boolean isSustainable,
            BigDecimal finalCorpus,
            BigDecimal startingCorpus,
            BigDecimal withdrawalRate,
            BigDecimal safeWithdrawalRate) {

        if (!isSustainable || finalCorpus.compareTo(new BigDecimal("1")) < 0) {
            return "⚠️ UNSUSTAINABLE: Corpus will be fully exhausted within the specified duration.";
        }

        if (finalCorpus.compareTo(startingCorpus) > 0) {
            return "✅ HIGHLY SUSTAINABLE: Your corpus is growing even with withdrawals. " +
                   "Consider increasing withdrawals or reduce risk exposure.";
        }

        if (isSustainable && withdrawalRate.compareTo(safeWithdrawalRate) <= 0) {
            return String.format(
                "✅ SUSTAINABLE: Your withdrawal rate (%.2f%%) is within safe limits (%.2f%%). " +
                "Corpus should last throughout retirement.",
                withdrawalRate, safeWithdrawalRate
            );
        }

        if (withdrawalRate.compareTo(safeWithdrawalRate) > 0) {
            return String.format(
                "⚠️ RISKY: Your withdrawal rate (%.2f%%) exceeds safe limits (%.2f%%). " +
                "Consider reducing withdrawals or increasing corpus.",
                withdrawalRate, safeWithdrawalRate
            );
        }

        return "⚠️ CAUTION: Corpus is declining. Monitor regularly and adjust withdrawals if needed.";
    }

    private List<ChartPoint> generateCorpusChart(
            List<SWPResponse.MonthlyWithdrawalBreakdown> breakdown) {

        List<ChartPoint> chart = new ArrayList<>();

        // Sample every 12 months for readability
        for (int i = 0; i < breakdown.size(); i += 12) {
            SWPResponse.MonthlyWithdrawalBreakdown month = breakdown.get(i);
            chart.add(new ChartPoint(
                "Month " + month.getMonth(),
                month.getClosingBalance()
            ));
        }

        // Always add last month
        if (!breakdown.isEmpty()) {
            SWPResponse.MonthlyWithdrawalBreakdown lastMonth = 
                breakdown.get(breakdown.size() - 1);
            chart.add(new ChartPoint(
                "Month " + lastMonth.getMonth(),
                lastMonth.getClosingBalance()
            ));
        }

        return chart;
    }

    private List<ChartPoint> generateWithdrawalChart(
            List<SWPResponse.MonthlyWithdrawalBreakdown> breakdown) {

        List<ChartPoint> chart = new ArrayList<>();

        // Sample every 12 months
        for (int i = 0; i < breakdown.size(); i += 12) {
            SWPResponse.MonthlyWithdrawalBreakdown month = breakdown.get(i);
            chart.add(new ChartPoint(
                "Year " + month.getYear(),
                month.getWithdrawalAmount()
            ));
        }

        return chart;
    }
}
