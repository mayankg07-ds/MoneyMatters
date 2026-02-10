package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.SWPRequest;
import com.moneymatters.calculators.dto.SWPResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SWP Calculator Service Tests")
public class SWPCalculatorServiceTest {

    private SWPCalculatorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SWPCalculatorServiceImpl();
    }

    @Test
    @DisplayName("Sustainable SWP: Return > Withdrawal")
    void testSustainableSWP() {
        SWPRequest request = new SWPRequest(
            new BigDecimal("5000000"),   // 50L corpus
            new BigDecimal("30000"),     // 30K/month withdrawal
            new BigDecimal("8"),         // 8% return
            25,                          // 25 years
            BigDecimal.ZERO,             // No inflation
            false                        // Not inflation-adjusted
        );

        SWPResponse response = service.calculateSWP(request);

        // Final corpus should be greater than starting (sustainable)
        assertTrue(response.getFinalCorpusValue()
            .compareTo(response.getStartingCorpus()) > 0);

        // Should be marked as sustainable
        assertTrue(response.getIsSustainable());

        // Should have full 25 years (300 months)
        assertEquals(300, response.getEffectiveDurationMonths());

        // Total returns should exceed total withdrawals
        assertTrue(response.getTotalReturnsEarned()
            .compareTo(response.getTotalWithdrawn()) > 0);
    }

    @Test
    @DisplayName("Depleting SWP: Withdrawal > Return")
    void testDepletingSWP() {
        SWPRequest request = new SWPRequest(
            new BigDecimal("5000000"),   // 50L corpus
            new BigDecimal("50000"),     // 50K/month withdrawal (high)
            new BigDecimal("6"),         // 6% return (low)
            20,                          // 20 years
            BigDecimal.ZERO,
            false
        );

        SWPResponse response = service.calculateSWP(request);

        // Final corpus should be less than starting (depleting)
        assertTrue(response.getFinalCorpusValue()
            .compareTo(response.getStartingCorpus()) < 0);

        // Corpus might exhaust before 20 years
        assertTrue(response.getEffectiveDurationMonths() <= 240);

        // Total withdrawals should exceed total returns
        assertTrue(response.getTotalWithdrawn()
            .compareTo(response.getTotalReturnsEarned()) > 0);
    }

    @Test
    @DisplayName("Inflation-adjusted withdrawals")
    void testInflationAdjustedSWP() {
        SWPRequest request = new SWPRequest(
            new BigDecimal("10000000"),  // 1 crore
            new BigDecimal("40000"),     // 40K initial
            new BigDecimal("10"),        // 10% return
            25,
            new BigDecimal("5"),         // 5% inflation
            true                         // Inflation-adjusted
        );

        SWPResponse response = service.calculateSWP(request);

        // Monthly breakdown should show increasing withdrawals
        SWPResponse.MonthlyWithdrawalBreakdown month1 = 
            response.getMonthlyBreakdown().get(0);
        SWPResponse.MonthlyWithdrawalBreakdown month12 = 
            response.getMonthlyBreakdown().get(11);
        SWPResponse.MonthlyWithdrawalBreakdown month24 = 
            response.getMonthlyBreakdown().get(23);

        // Month 1 withdrawal
        assertEquals(0, month1.getWithdrawalAmount()
            .compareTo(new BigDecimal("40000.00")));

        // Month 13 withdrawal should be higher (5% increase)
        assertTrue(month12.getWithdrawalAmount()
            .compareTo(month1.getWithdrawalAmount()) > 0);

        // Month 25 withdrawal should be even higher
        assertTrue(month24.getWithdrawalAmount()
            .compareTo(month12.getWithdrawalAmount()) > 0);

        // Should still be sustainable
        assertTrue(response.getIsSustainable());
    }

    @Test
    @DisplayName("Complete corpus exhaustion")
    void testCorpusExhaustion() {
        SWPRequest request = new SWPRequest(
            new BigDecimal("1000000"),   // 10L corpus (small)
            new BigDecimal("50000"),     // 50K/month (high withdrawal)
            new BigDecimal("4"),         // 4% return (low)
            10,                          // 10 years
            BigDecimal.ZERO,
            false
        );

        SWPResponse response = service.calculateSWP(request);

        // Corpus should be exhausted
        assertTrue(response.getFinalCorpusValue()
            .compareTo(new BigDecimal("1000")) < 0);

        // Duration should be less than requested
        assertTrue(response.getEffectiveDurationMonths() < 120);

        // Should be marked as unsustainable
        assertFalse(response.getIsSustainable());

        // Sustainability message should indicate exhaustion
        assertTrue(response.getSustainabilityMessage().contains("UNSUSTAINABLE"));
    }

    @Test
    @DisplayName("Withdrawal rate analysis")
    void testWithdrawalRateAnalysis() {
        SWPRequest request = new SWPRequest(
            new BigDecimal("10000000"),  // 1 crore
            new BigDecimal("40000"),     // 40K/month = 4.8% annually
            new BigDecimal("9"),         // 9% return
            20,
            new BigDecimal("4"),         // 4% inflation
            false
        );

        SWPResponse response = service.calculateSWP(request);

        // Withdrawal rate should be ~4.8%
        assertTrue(response.getWithdrawalRate()
            .compareTo(new BigDecimal("4.5")) > 0);
        assertTrue(response.getWithdrawalRate()
            .compareTo(new BigDecimal("5.0")) < 0);

        // Safe withdrawal rate should be 5% (9% - 4%)
        assertTrue(response.getSafeWithdrawalRate()
            .compareTo(new BigDecimal("4.5")) > 0);
        assertTrue(response.getSafeWithdrawalRate()
            .compareTo(new BigDecimal("5.5")) < 0);

        // Should be sustainable
        assertTrue(response.getIsSustainable());
    }

    @Test
    @DisplayName("Yearly summary accuracy")
    void testYearlySummary() {
        SWPRequest request = new SWPRequest(
            new BigDecimal("5000000"),
            new BigDecimal("30000"),
            new BigDecimal("8"),
            5,
            BigDecimal.ZERO,
            false
        );

        SWPResponse response = service.calculateSWP(request);

        // Should have 5 yearly summaries
        assertEquals(5, response.getYearlySummary().size());

        // First year
        SWPResponse.YearlyWithdrawalSummary year1 = response.getYearlySummary().get(0);
        assertEquals(1, year1.getYear());
        
        // Total withdrawals for year 1 should be 30K × 12 = 3.6L
        assertTrue(year1.getTotalWithdrawals()
            .compareTo(new BigDecimal("360000")) == 0);

        // Corpus should be growing
        assertTrue(year1.getCorpusGrowing());
    }
}
