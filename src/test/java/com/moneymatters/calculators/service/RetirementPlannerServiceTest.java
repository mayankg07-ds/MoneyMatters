package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.RetirementPlanRequest;
import com.moneymatters.calculators.dto.RetirementPlanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Retirement Planner Service Tests")
public class RetirementPlannerServiceTest {

    private RetirementPlannerServiceImpl service;

    @BeforeEach
    void setUp() {
        FinancialMathService mathService = new FinancialMathService();
        service = new RetirementPlannerServiceImpl(mathService);
    }

    @Test
    @DisplayName("Base scenario: 30yr old, retire at 60, 50K expenses")
    void testBasicRetirementScenario() {
        RetirementPlanRequest request = new RetirementPlanRequest(
            30,                              // currentAge
            60,                              // retirementAge
            85,                              // lifeExpectancy
            new BigDecimal("50000"),         // currentMonthlyExpense
            new BigDecimal("6"),             // inflation 6%
            new BigDecimal("12"),            // pre-retirement return 12%
            new BigDecimal("8"),             // post-retirement return 8%
            new BigDecimal("1000000")        // existing corpus ₹10L
        );

        RetirementPlanResponse response = service.calculateRetirementPlan(request);

        // Years calculation
        assertEquals(30, response.getYearsToRetirement());
        assertEquals(25, response.getYearsInRetirement());

        // Inflated expense: 50000 × (1.06)^30 ≈ 287,000
        assertTrue(response.getInflatedMonthlyExpenseAtRetirement()
            .compareTo(new BigDecimal("280000")) > 0);
        assertTrue(response.getInflatedMonthlyExpenseAtRetirement()
            .compareTo(new BigDecimal("295000")) < 0);

        // Required corpus should be in crores (very large)
        assertTrue(response.getRequiredCorpusAtRetirement()
            .compareTo(new BigDecimal("30000000")) > 0); // > 3 crores

        // Projected existing corpus: 10L × (1.12)^30 ≈ 2.99 crores
        assertTrue(response.getProjectedExistingCorpusAtRetirement()
            .compareTo(new BigDecimal("25000000")) > 0);

        // Shortfall should exist
        assertTrue(response.getCorpusShortfall().compareTo(BigDecimal.ZERO) > 0);

        // Recommended SIP should be reasonable (few thousands per month)
        assertTrue(response.getRecommendedMonthlySIP()
            .compareTo(new BigDecimal("1000")) > 0);
        assertTrue(response.getRecommendedMonthlySIP()
            .compareTo(new BigDecimal("50000")) < 0);
    }

    @Test
    @DisplayName("Already sufficient corpus: no SIP needed")
    void testSufficientExistingCorpus() {
        RetirementPlanRequest request = new RetirementPlanRequest(
            55,                              // close to retirement
            60,
            75,
            new BigDecimal("30000"),
            new BigDecimal("5"),
            new BigDecimal("10"),
            new BigDecimal("7"),
            new BigDecimal("50000000")       // ₹5 crores existing
        );

        RetirementPlanResponse response = service.calculateRetirementPlan(request);

        // Should have no shortfall or minimal
        assertTrue(response.getCorpusShortfall()
            .compareTo(BigDecimal.ZERO) <= 0);

        // Recommended SIP should be 0 or very small
        assertTrue(response.getRecommendedMonthlySIP()
            .compareTo(new BigDecimal("100")) < 0);
    }

    @Test
    @DisplayName("Edge case: retirement age = life expectancy")
    void testZeroRetirementYears() {
        RetirementPlanRequest request = new RetirementPlanRequest(
            50,
            60,
            60,  // Same as retirement age!
            new BigDecimal("50000"),
            new BigDecimal("6"),
            new BigDecimal("12"),
            new BigDecimal("8"),
            new BigDecimal("1000000")
        );

        RetirementPlanResponse response = service.calculateRetirementPlan(request);

        // Years in retirement = 0
        assertEquals(0, response.getYearsInRetirement());

        // Required corpus should be minimal or zero
        assertTrue(response.getRequiredCorpusAtRetirement()
            .compareTo(new BigDecimal("1000")) < 0);
    }

    @Test
    @DisplayName("Edge case: retirement age < current age (invalid)")
    void testInvalidRetirementAge() {
        RetirementPlanRequest request = new RetirementPlanRequest(
            60,
            50,  // Retirement age before current age!
            80,
            new BigDecimal("50000"),
            new BigDecimal("6"),
            new BigDecimal("12"),
            new BigDecimal("8"),
            new BigDecimal("1000000")
        );

        RetirementPlanResponse response = service.calculateRetirementPlan(request);

        // Should return zeros or handle gracefully
        assertEquals(BigDecimal.ZERO, response.getRecommendedMonthlySIP());
        assertEquals(BigDecimal.ZERO, response.getCorpusShortfall());
    }
}

