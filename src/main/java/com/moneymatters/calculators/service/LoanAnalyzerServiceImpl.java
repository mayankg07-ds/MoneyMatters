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
public class LoanAnalyzerServiceImpl implements LoanAnalyzerService {

    private final FinancialMathService financialMathService;

    // ============================================================
    // THIS IS WHERE YOUR CODE GOES - Main Method
    // ============================================================
    
    @Override
    public LoanAnalysisResponse analyzeLoan(LoanAnalysisRequest request) {
        log.info("Analyzing loan: {}", request);

        // Calculate EMI using FinancialMathService
        BigDecimal emi = financialMathService.calculateEMI(
            request.getPrincipal(),
            request.getAnnualInterestRatePercent(),
            request.getTenureMonths()
        );

        BigDecimal monthlyRate = CalculationUtils.percentToDecimal(
            request.getAnnualInterestRatePercent()
        ).divide(new BigDecimal(12), 10, RoundingMode.HALF_UP);

        // Generate amortization schedule WITHOUT prepayments first (for original values)
        List<MonthlyPaymentBreakdown> originalSchedule = generateAmortizationSchedule(
            request.getPrincipal(),
            emi,
            monthlyRate,
            request.getTenureMonths(),
            null  // No prepayments for original calculation
        );

        // Calculate original total interest (without prepayments)
        BigDecimal originalTotalInterest = originalSchedule.stream()
            .map(MonthlyPaymentBreakdown::getInterestPaid)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Generate amortization schedule WITH prepayments (for actual values)
        List<MonthlyPaymentBreakdown> schedule = generateAmortizationSchedule(
            request.getPrincipal(),
            emi,
            monthlyRate,
            request.getTenureMonths(),
            request.getPrepayments()
        );

        // Calculate totals from schedule (with prepayments if any)
        BigDecimal totalInterest = schedule.stream()
            .map(MonthlyPaymentBreakdown::getInterestPaid)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = request.getPrincipal().add(totalInterest);

        BigDecimal interestPercentage = CalculationUtils.safeDivide(
            totalInterest.multiply(new BigDecimal(100)),
            request.getPrincipal()
        );

        // Calculate prepayment impact if applicable
        PrepaymentImpact prepaymentImpact = calculatePrepaymentImpact(
            request,
            emi,
            originalTotalInterest,  // Use original interest (without prepayments)
            schedule
        );

        // Generate chart data
        List<ChartPoint> principalVsInterestChart = 
            generatePrincipalVsInterestChart(schedule);
        List<ChartPoint> balanceOverTimeChart = 
            generateBalanceOverTimeChart(schedule);

        // Return complete response
        return new LoanAnalysisResponse(
            CalculationUtils.format(emi),
            CalculationUtils.format(totalAmount),
            CalculationUtils.format(totalInterest),
            schedule.size(),
            request.getPrincipal(),
            CalculationUtils.format(interestPercentage),
            schedule,
            prepaymentImpact,
            principalVsInterestChart,
            balanceOverTimeChart
        );
    }

    // ============================================================
    // Helper Methods (Write these BELOW the main method)
    // ============================================================

    private List<MonthlyPaymentBreakdown> generateAmortizationSchedule(
            BigDecimal principal,
            BigDecimal emi,
            BigDecimal monthlyRate,
            int tenureMonths,
            List<PrepaymentScenario> prepayments) {

        List<MonthlyPaymentBreakdown> schedule = new ArrayList<>();
        
        BigDecimal balance = principal;
        BigDecimal cumulativeInterest = BigDecimal.ZERO;
        BigDecimal cumulativePrincipal = BigDecimal.ZERO;
        
        BigDecimal currentEMI = emi;

        for (int month = 1; month <= tenureMonths && balance.compareTo(new BigDecimal("1")) > 0; month++) {
            
            BigDecimal openingBalance = balance;
            
            // Interest for this month
            BigDecimal interestPaid = balance.multiply(monthlyRate);
            interestPaid = CalculationUtils.format(interestPaid);
            
            // Principal for this month
            BigDecimal principalPaid = currentEMI.subtract(interestPaid);
            
            // Don't pay more than remaining balance
            if (principalPaid.compareTo(balance) > 0) {
                principalPaid = balance;
                currentEMI = principalPaid.add(interestPaid);
            }
            
            principalPaid = CalculationUtils.format(principalPaid);
            
            // Update balance
            balance = balance.subtract(principalPaid);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                balance = BigDecimal.ZERO;
            }
            
            // Update cumulatives
            cumulativeInterest = cumulativeInterest.add(interestPaid);
            cumulativePrincipal = cumulativePrincipal.add(principalPaid);
            
            // Add to schedule
            schedule.add(new MonthlyPaymentBreakdown(
                month,
                (month - 1) / 12 + 1,
                CalculationUtils.format(openingBalance),
                CalculationUtils.format(currentEMI),
                interestPaid,
                principalPaid,
                CalculationUtils.format(balance),
                CalculationUtils.format(cumulativeInterest),
                CalculationUtils.format(cumulativePrincipal)
            ));
            
            // Handle prepayments at this month
            if (prepayments != null) {
                for (PrepaymentScenario prepayment : prepayments) {
                    if (prepayment.getAtMonth().equals(month)) {
                        balance = balance.subtract(prepayment.getAmount());
                        if (balance.compareTo(BigDecimal.ZERO) < 0) {
                            balance = BigDecimal.ZERO;
                        }
                        
                        if (prepayment.getOption() == PrepaymentScenario.PrepaymentOption.REDUCE_EMI) {
                            int remainingMonths = tenureMonths - month;
                            currentEMI = recalculateEMI(balance, monthlyRate, remainingMonths);
                        }
                    }
                }
            }
            
            if (balance.compareTo(new BigDecimal("1")) < 0) {
                break;
            }
        }
        
        return schedule;
    }

    private BigDecimal recalculateEMI(
            BigDecimal principal,
            BigDecimal monthlyRate,
            int remainingMonths) {

        if (remainingMonths <= 0 || principal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return CalculationUtils.safeDivide(principal, new BigDecimal(remainingMonths));
        }

        BigDecimal base = BigDecimal.ONE.add(monthlyRate);
        BigDecimal powerN = CalculationUtils.power(base, remainingMonths);
        
        BigDecimal numerator = monthlyRate.multiply(powerN);
        BigDecimal denominator = powerN.subtract(BigDecimal.ONE);
        
        BigDecimal factor = CalculationUtils.safeDivide(numerator, denominator);
        return CalculationUtils.format(principal.multiply(factor));
    }

    private PrepaymentImpact calculatePrepaymentImpact(
            LoanAnalysisRequest request,
            BigDecimal originalEMI,
            BigDecimal originalTotalInterest,
            List<MonthlyPaymentBreakdown> scheduleWithPrepayment) {

        if (request.getPrepayments() == null || request.getPrepayments().isEmpty()) {
            return null;
        }

        BigDecimal totalPrepayment = request.getPrepayments().stream()
            .map(PrepaymentScenario::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal originalTotalCost = originalEMI
            .multiply(new BigDecimal(request.getTenureMonths()));

        BigDecimal newTotalInterest = scheduleWithPrepayment.stream()
            .map(MonthlyPaymentBreakdown::getInterestPaid)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotalPrincipal = scheduleWithPrepayment.stream()
            .map(MonthlyPaymentBreakdown::getPrincipalPaid)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotalCost = newTotalInterest.add(newTotalPrincipal).add(totalPrepayment);

        BigDecimal interestSaved = originalTotalInterest.subtract(newTotalInterest);
        int monthsSaved = request.getTenureMonths() - scheduleWithPrepayment.size();

        BigDecimal newEMI = scheduleWithPrepayment.isEmpty() 
            ? BigDecimal.ZERO 
            : scheduleWithPrepayment.get(scheduleWithPrepayment.size() - 1).getEmi();

        return new PrepaymentImpact(
            CalculationUtils.format(totalPrepayment),
            CalculationUtils.format(interestSaved),
            monthsSaved,
            CalculationUtils.format(newEMI),
            CalculationUtils.format(originalTotalCost),
            CalculationUtils.format(newTotalCost)
        );
    }

    private List<ChartPoint> generatePrincipalVsInterestChart(
            List<MonthlyPaymentBreakdown> schedule) {

        List<ChartPoint> chart = new ArrayList<>();
        
        for (int i = 0; i < schedule.size(); i += 12) {
            MonthlyPaymentBreakdown month = schedule.get(i);
            chart.add(new ChartPoint(
                "Month " + month.getMonth(),
                month.getCumulativePrincipal()
            ));
        }
        
        if (!schedule.isEmpty()) {
            MonthlyPaymentBreakdown lastMonth = schedule.get(schedule.size() - 1);
            chart.add(new ChartPoint(
                "Month " + lastMonth.getMonth(),
                lastMonth.getCumulativePrincipal()
            ));
        }
        
        return chart;
    }

    private List<ChartPoint> generateBalanceOverTimeChart(
            List<MonthlyPaymentBreakdown> schedule) {

        List<ChartPoint> chart = new ArrayList<>();
        
        for (int i = 0; i < schedule.size(); i += 12) {
            MonthlyPaymentBreakdown month = schedule.get(i);
            chart.add(new ChartPoint(
                "Month " + month.getMonth(),
                month.getClosingBalance()
            ));
        }
        
        if (!schedule.isEmpty()) {
            MonthlyPaymentBreakdown lastMonth = schedule.get(schedule.size() - 1);
            chart.add(new ChartPoint(
                "Month " + lastMonth.getMonth(),
                lastMonth.getClosingBalance()
            ));
        }
        
        return chart;
    }
}
