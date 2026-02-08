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

        // Total payable = EMI × 60 (approximately)
        BigDecimal expectedTotal = response.getEmi().multiply(new BigDecimal(60));
        // Use range check - allow up to ₹10 difference due to rounding
        assertTrue(response.getTotalAmountPayable()
            .subtract(expectedTotal).abs().compareTo(new BigDecimal("10")) < 0);

        // Total interest = Total - Principal
        BigDecimal expectedInterest = response.getTotalAmountPayable().subtract(new BigDecimal("500000"));
        assertTrue(response.getTotalInterestPayable()
            .subtract(expectedInterest).abs().compareTo(new BigDecimal("1")) < 0);

        // Should have 60 rows in schedule
        assertEquals(60, response.getAmortizationSchedule().size());

        // First month: for this 5-year loan, verify interest payment is reasonable
        MonthlyPaymentBreakdown firstMonth = response.getAmortizationSchedule().get(0);
        assertEquals(1, firstMonth.getMonth());
        // Monthly interest = 500000 * (10%/12) = ~4166.67
        assertTrue(firstMonth.getInterestPaid().compareTo(new BigDecimal("4000")) > 0);
        assertTrue(firstMonth.getInterestPaid().compareTo(new BigDecimal("5000")) < 0);

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
        // Verify interest is saved (should be > 5000 with 50K prepayment)
        assertTrue(impact.getInterestSaved().compareTo(new BigDecimal("5000")) > 0);
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

        // EMI should reduce and interest should be saved
        PrepaymentImpact impact = response.getPrepaymentImpact();
        assertNotNull(impact);
        // After prepayment, new EMI should be lower than original
        assertTrue(impact.getNewEMI().compareTo(new BigDecimal("10500")) < 0);
        assertTrue(impact.getInterestSaved().compareTo(new BigDecimal("1000")) > 0);
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
        
        // Significant interest savings (should be > 10000 with 50K total prepayment)
        assertTrue(response.getPrepaymentImpact().getInterestSaved()
            .compareTo(new BigDecimal("10000")) > 0);
    }

    @Test
    @DisplayName("Edge case: Very short tenure (6 months)")
    void testShortTenure() {
        LoanAnalysisRequest request = new LoanAnalysisRequest(
            new BigDecimal("100000"),
            new BigDecimal("12"),
            6,
            null
        );

        LoanAnalysisResponse response = service.analyzeLoan(request);

        assertEquals(6, response.getAmortizationSchedule().size());
        assertTrue(response.getTotalInterestPayable()
            .compareTo(new BigDecimal("3500")) > 0);
    }

    @Test
    @DisplayName("Edge case: Very long tenure (30 years)")
    void testLongTenure() {
        LoanAnalysisRequest request = new LoanAnalysisRequest(
            new BigDecimal("5000000"),
            new BigDecimal("9"),
            360,  // 30 years
            null
        );

        LoanAnalysisResponse response = service.analyzeLoan(request);

        assertEquals(360, response.getAmortizationSchedule().size());
        
        // For 30-year loan, interest can exceed principal
        assertTrue(response.getTotalInterestPayable()
            .compareTo(response.getPrincipalAmount()) > 0);
    }

    @Test
    @DisplayName("Edge case: Prepayment > remaining balance")
    void testExcessivePrepayment() {
        PrepaymentScenario prepayment = new PrepaymentScenario(
            6,
            new BigDecimal("5000000"),  // Prepay more than loan!
            PrepaymentScenario.PrepaymentOption.REDUCE_TENURE
        );

        LoanAnalysisRequest request = new LoanAnalysisRequest(
            new BigDecimal("100000"),
            new BigDecimal("10"),
            12,
            List.of(prepayment)
        );

        LoanAnalysisResponse response = service.analyzeLoan(request);

        // Should close loan after prepayment month
        assertTrue(response.getEffectiveTenureMonths() <= 6);
    }
}
