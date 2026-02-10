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
public class CashflowPlannerServiceImpl implements CashflowPlannerService {

    @Override
    public CashflowResponse projectCashflow(CashflowRequest request) {
        log.info("Projecting cashflow for {} years", request.getProjectionYears());

        // Calculate current month totals
        BigDecimal currentMonthlyIncome = calculateTotal(request.getIncomes());
        BigDecimal currentMonthlyExpense = calculateTotal(request.getExpenses());
        BigDecimal currentNetCashflow = currentMonthlyIncome.subtract(currentMonthlyExpense);
        BigDecimal currentSavingsRate = calculateSavingsRate(
            currentNetCashflow, 
            currentMonthlyIncome
        );

        // Generate projections
        List<CashflowResponse.YearlyCashflow> projections = generateProjections(
            currentMonthlyIncome,
            currentMonthlyExpense,
            request.getProjectionYears(),
            request.getExpectedIncomeGrowthPercent(),
            request.getExpectedExpenseGrowthPercent()
        );

        // Calculate summary statistics
        BigDecimal totalSavings = projections.stream()
            .map(CashflowResponse.YearlyCashflow::getAnnualSavings)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgAnnualIncome = projections.stream()
            .map(CashflowResponse.YearlyCashflow::getAnnualIncome)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(projections.size()), 2, RoundingMode.HALF_UP);

        BigDecimal avgAnnualExpense = projections.stream()
            .map(CashflowResponse.YearlyCashflow::getAnnualExpense)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(projections.size()), 2, RoundingMode.HALF_UP);

        BigDecimal avgSavingsRate = projections.stream()
            .map(CashflowResponse.YearlyCashflow::getSavingsRate)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(projections.size()), 2, RoundingMode.HALF_UP);

        // Generate breakdowns
        List<CashflowResponse.ItemBreakdown> incomeBreakdown = 
            generateBreakdown(request.getIncomes(), currentMonthlyIncome);
        List<CashflowResponse.ItemBreakdown> expenseBreakdown = 
            generateBreakdown(request.getExpenses(), currentMonthlyExpense);

        // Generate charts
        List<ChartPoint> incomeVsExpenseChart = generateIncomeVsExpenseChart(projections);
        List<ChartPoint> savingsChart = generateSavingsChart(projections);
        List<ChartPoint> savingsRateChart = generateSavingsRateChart(projections);

        return new CashflowResponse(
            CalculationUtils.format(currentMonthlyIncome),
            CalculationUtils.format(currentMonthlyExpense),
            CalculationUtils.format(currentNetCashflow),
            CalculationUtils.format(currentSavingsRate),
            CalculationUtils.format(avgAnnualIncome),
            CalculationUtils.format(avgAnnualExpense),
            CalculationUtils.format(totalSavings),
            CalculationUtils.format(avgSavingsRate),
            projections,
            incomeBreakdown,
            expenseBreakdown,
            incomeVsExpenseChart,
            savingsChart,
            savingsRateChart
        );
    }

    private BigDecimal calculateTotal(List<CashflowRequest.CashflowItem> items) {
        return items.stream()
            .map(CashflowRequest.CashflowItem::getMonthlyAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateSavingsRate(BigDecimal netCashflow, BigDecimal income) {
        if (income.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return netCashflow
            .multiply(new BigDecimal(100))
            .divide(income, 2, RoundingMode.HALF_UP);
    }

    private List<CashflowResponse.YearlyCashflow> generateProjections(
            BigDecimal baseMonthlyIncome,
            BigDecimal baseMonthlyExpense,
            int years,
            BigDecimal incomeGrowthPercent,
            BigDecimal expenseGrowthPercent) {

        List<CashflowResponse.YearlyCashflow> projections = new ArrayList<>();
        
        BigDecimal incomeGrowthFactor = BigDecimal.ONE.add(
            CalculationUtils.percentToDecimal(incomeGrowthPercent)
        );
        BigDecimal expenseGrowthFactor = BigDecimal.ONE.add(
            CalculationUtils.percentToDecimal(expenseGrowthPercent)
        );

        BigDecimal cumulativeSavings = BigDecimal.ZERO;
        BigDecimal currentMonthlyIncome = baseMonthlyIncome;
        BigDecimal currentMonthlyExpense = baseMonthlyExpense;

        for (int year = 1; year <= years; year++) {
            // Calculate for this year
            BigDecimal annualIncome = currentMonthlyIncome.multiply(new BigDecimal(12));
            BigDecimal annualExpense = currentMonthlyExpense.multiply(new BigDecimal(12));
            BigDecimal annualSavings = annualIncome.subtract(annualExpense);
            BigDecimal monthlyNetCashflow = currentMonthlyIncome.subtract(currentMonthlyExpense);
            
            BigDecimal savingsRate = calculateSavingsRate(
                monthlyNetCashflow,
                currentMonthlyIncome
            );

            cumulativeSavings = cumulativeSavings.add(annualSavings);

            projections.add(new CashflowResponse.YearlyCashflow(
                year,
                CalculationUtils.format(currentMonthlyIncome),
                CalculationUtils.format(currentMonthlyExpense),
                CalculationUtils.format(monthlyNetCashflow),
                CalculationUtils.format(annualIncome),
                CalculationUtils.format(annualExpense),
                CalculationUtils.format(annualSavings),
                CalculationUtils.format(savingsRate),
                CalculationUtils.format(cumulativeSavings)
            ));

            // Apply growth for next year
            currentMonthlyIncome = currentMonthlyIncome.multiply(incomeGrowthFactor);
            currentMonthlyExpense = currentMonthlyExpense.multiply(expenseGrowthFactor);
        }

        return projections;
    }

    private List<CashflowResponse.ItemBreakdown> generateBreakdown(
            List<CashflowRequest.CashflowItem> items,
            BigDecimal total) {

        return items.stream()
            .map(item -> {
                BigDecimal percentage = CalculationUtils.safeDivide(
                    item.getMonthlyAmount().multiply(new BigDecimal(100)),
                    total
                );
                return new CashflowResponse.ItemBreakdown(
                    item.getName(),
                    CalculationUtils.format(item.getMonthlyAmount()),
                    CalculationUtils.format(percentage),
                    item.getCategory()
                );
            })
            .toList();
    }

    private List<ChartPoint> generateIncomeVsExpenseChart(
            List<CashflowResponse.YearlyCashflow> projections) {

        List<ChartPoint> chart = new ArrayList<>();
        
        for (CashflowResponse.YearlyCashflow projection : projections) {
            chart.add(new ChartPoint(
                "Year " + projection.getYear() + " Income",
                projection.getAnnualIncome()
            ));
            chart.add(new ChartPoint(
                "Year " + projection.getYear() + " Expense",
                projection.getAnnualExpense()
            ));
        }

        return chart;
    }

    private List<ChartPoint> generateSavingsChart(
            List<CashflowResponse.YearlyCashflow> projections) {

        return projections.stream()
            .map(p -> new ChartPoint(
                "Year " + p.getYear(),
                p.getCumulativeSavings()
            ))
            .toList();
    }

    private List<ChartPoint> generateSavingsRateChart(
            List<CashflowResponse.YearlyCashflow> projections) {

        return projections.stream()
            .map(p -> new ChartPoint(
                "Year " + p.getYear(),
                p.getSavingsRate()
            ))
            .toList();
    }
}
