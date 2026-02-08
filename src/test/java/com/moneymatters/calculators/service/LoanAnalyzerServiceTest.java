package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Loan Analyzer Service Tests")
public class LoanAnalyzerServiceTest {

    private LoanAnalyzerServiceImpl service;

    @BeforeEach
    void setUp() {
        FinancialMathService mathService = new FinancialMathService();
        service = new LoanAnalyzerServiceImpl(mathService);
    }

    @Test
    @DisplayName("Basic loan: 5L at 10% for 5 years")
    void testBasicLoan() {
        LoanAnalysisRequest request = new LoanAnalysisRequest(
            new BigDecimal("500000"),    // Principal
            new BigDecimal("10"),        // 10% annual
            60,                          // 5 years
            null                         // No prepayments
        );

        LoanAnalysisResponse response = service.analyzeLoan(request);

        // EMI should be around ₹10,624
        assertTrue(response.getEmi().compareTo(new BigDecimal("10600")) > 0);
        assertTrue(response.getEmi().compareTo(new BigDecimal("10650")) < 0);

        // Total payable = EMI × 60
        BigDecimal expectedTotal = response.getEmi().multiply(new BigDecimal(60));
        assertEquals(0, response.getTotalAmountPayable()
            .compareTo(expectedTotal.setScale(2, java.math.RoundingMode.HALF_UP)));

        // Total interest = Total - Principal
        BigDecimal expectedInterest = expectedTotal.subtract(new BigDecimal("500000"));
        assertTrue(response.getTotalInterestPayable()
            .compareTo(expectedInterest.setScale(2, java.math.RoundingMode.HALF_UP)) == 0);

        // Should have 60 rows in schedule
        assertEquals(60, response.getAmortizationSchedule().size());

        // First month: more interest than principal
        MonthlyPaymentBreakdown firstMonth = response.getAmortizationSchedule().get(0);
        assertEquals(1, firstMonth.getMonth());
        assertTrue(firstMonth.getInterestPaid()
            .compareTo(firstMonth.getPrincipalPaid()) > 0);

        // Last month: more principal than interest
        MonthlyPaymentBreakdown lastMonth = response.getAmortizationSchedule().get(59);
        assertEquals(60, lastMonth.getMonth());
        assertTrue(lastMonth.getPrincipalPaid()
            .compareTo(lastMonth.getInterestPaid()) > 0);

        // Last month closing balance should be ~0
        assertTrue(lastMonth.getClosingBalance()
            .compareTo(new BigDecimal("10")) < 0);
    }

    @Test
    @DisplayName("Amortization schedule accuracy")
    void testAmortizationScheduleAccuracy() {
        LoanAnalysisRequest request = new LoanAnalysisRequest(
            new BigDecimal("500000"),
            new BigDecimal("10"),
            60,
            null
        );

        LoanAnalysisResponse response = service.analyzeLoan(request);
        List<MonthlyPaymentBreakdown> schedule = response.getAmortizationSchedule();

        // Verify first month manually
        MonthlyPaymentBreakdown month1 = schedule.get(0);
        
        // Opening balance should be principal
        assertEquals(0, month1.getOpeningBalance()
            .compareTo(new BigDecimal("500000.00")));

        // Interest = 500000 × (10/100/12) = 4166.67
        BigDecimal expectedInterest = new BigDecimal("500000")
            .multiply(new BigDecimal("10"))
            .divide(new BigDecimal("100"), 10, java.math.RoundingMode.HALF_UP)
            .divide(new BigDecimal("12"), 10, java.math.RoundingMode.HALF_UP);
        
        assertTrue(month1.getInterestPaid()
            .subtract(expectedInterest.setScale(2, java.math.RoundingMode.HALF_UP))
            .abs()
            .compareTo(new BigDecimal("10")) < 0); // Within ₹10

        // Cumulative checks
        BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
        BigDecimal totalInterestPaid = BigDecimal.ZERO;

        for (MonthlyPaymentBreakdown month : schedule) {
            totalPrincipalPaid = totalPrincipalPaid.add(month.getPrincipalPaid());
            totalInterestPaid = totalInterestPaid.add(month.getInterestPaid());
        }

        // Total principal paid should equal original principal
        assertTrue(totalPrincipalPaid.subtract(new BigDecimal("500000"))
            .abs().compareTo(new BigDecimal("100")) < 0);

        // Total interest should match response
        assertTrue(totalInterestPaid.subtract(response.getTotalInterestPayable())
            .abs().compareTo(new BigDecimal("10")) < 0);
    }

    @Test
    @DisplayName("Prepayment: Reduce tenure")
    void testPrepaymentReduceTenure() {
        PrepaymentScenario prepayment = new PrepaymentScenario(
            12,                          // After 1 year
            new BigDecimal("50000"),     // ₹50K prepayment
            PrepaymentScenario.PrepaymentOption.REDUCE_TENURE
        );

        LoanAnalysisRequest request = new LoanAnalysisRequest(
            new BigDecimal("500000"),
            new BigDecimal("10"),
            60,
            List.of(prepayment)
        );

        LoanAnalysisResponse response = service.analyzeLoan(request);

        // Tenure should reduce
        assertTrue(response.getEffectiveTenureMonths() < 60);

        // Interest should be saved
        PrepaymentImpact impact = response.getPrepaymentImpact();
        assertNotNull(impact);
        assertTrue(impact.getInterestSaved().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(impact.getMonthsSaved() > 0);

        // New total cost should be less
        assertTrue(impact.getNewTotalCost()
            .compareTo(impact.getOriginalTotalCost()) < 0);
    }

    @Test
    @DisplayName("Prepayment: Reduce EMI")
    void testPrepaymentReduceEMI() {
        PrepaymentScenario prepayment = new PrepaymentScenario(
            12,
            new BigDecimal("50000"),
            PrepaymentScenario.PrepaymentOption.REDUCE_EMI
        );

        LoanAnalysisRequest request = new LoanAnalysisRequest(
            new BigDecimal("500000"),
            new BigDecimal("10"),
            60,
            List.of(prepayment)
        );

        LoanAnalysisResponse response = service.analyzeLoan(request);

        // Tenure should stay same
        assertEquals(60, response.getEffectiveTenureMonths());

        // EMI should reduce
        PrepaymentImpact impact = response.getPrepaymentImpact();
        assertNotNull(impact);
        assertTrue(impact.getNewEMI().compareTo(response.getEmi()) < 0);
        assertTrue(impact.getInterestSaved().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Multiple prepayments")
    void testMultiplePrepayments() {
        List<PrepaymentScenario> prepayments = List.of(
            new PrepaymentScenario(12, new BigDecimal("25000"), 
                PrepaymentScenario.PrepaymentOption.REDUCE_TENURE),
            new PrepaymentScenario(24, new BigDecimal("25000"), 
                PrepaymentScenario.PrepaymentOption.REDUCE_TENURE)
        );

        LoanAnalysisRequest request = new LoanAnalysisRequest(
            new BigDecimal("500000"),
            new BigDecimal("10"),
            60,
            prepayments
        );

        LoanAnalysisResponse response = service.analyzeLoan(request);

        // Should handle multiple prepayments
        assertEquals(new BigDecimal("50000.00"), 
            response.getPrepaymentImpact().getTotalPrepaymentAmount());
        
        // Significant interest savings
        assertTrue(response.getPrepaymentImpact().getInterestSaved()
            .compareTo(new BigDecimal("30000")) > 0);
    }
}
