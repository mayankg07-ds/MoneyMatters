package com.moneymatters.calculators.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

@DisplayName("Financial Math Service Tests")
public class FinancialMathServiceTest {

    private FinancialMathService service;

    @BeforeEach
    void setUp() {
        service = new FinancialMathService();
    }

    // ==================== FUTURE VALUE TESTS ====================

    @Test
    @DisplayName("FV: ₹100 at 10% for 5 years = ₹161.05")
    void testFutureValue() {
        BigDecimal result = service.calculateFutureValue(
            new BigDecimal("100"),
            new BigDecimal("10"),
            new BigDecimal("5")
        );

        assertTrue(result.compareTo(new BigDecimal("161")) > 0);
        assertTrue(result.compareTo(new BigDecimal("162")) < 0);
    }

    @Test
    @DisplayName("FV: Negative principal returns 0")
    void testFutureValueNegativePrincipal() {
        BigDecimal result = service.calculateFutureValue(
            new BigDecimal("-100"),
            new BigDecimal("10"),
            new BigDecimal("5")
        );

        assertEquals(BigDecimal.ZERO, result);
    }

    // ==================== PRESENT VALUE TESTS ====================

    @Test
    @DisplayName("PV: ₹1,00,000 in 5 years at 10% = invest ₹62,092 today")
    void testPresentValue() {
        BigDecimal result = service.calculatePresentValue(
            new BigDecimal("100000"),
            new BigDecimal("10"),
            new BigDecimal("5")
        );

        assertTrue(result.compareTo(new BigDecimal("62000")) > 0);
        assertTrue(result.compareTo(new BigDecimal("63000")) < 0);
    }

    // ==================== EMI TESTS ====================

    @Test
    @DisplayName("EMI: ₹50,00,000 at 8.5% for 20 years = ₹41,543/month")
    void testEMI() {
        BigDecimal result = service.calculateEMI(
            new BigDecimal("5000000"),
            new BigDecimal("8.5"),
            240
        );

        assertTrue(result.compareTo(new BigDecimal("43000")) > 0);
        assertTrue(result.compareTo(new BigDecimal("44000")) < 0);
    }

    @Test
    @DisplayName("EMI: 0% rate = Principal / Months")
    void testEMIZeroRate() {
        BigDecimal result = service.calculateEMI(
            new BigDecimal("600000"),
            new BigDecimal("0"),
            60
        );

        assertEquals(new BigDecimal("10000.00"), result);
    }

    // ==================== ANNUITY FUTURE VALUE TESTS ====================

    @Test
    @DisplayName("AFV: ₹10,000/month at 12% for 5 years ≈ ₹8,16,700")
    void testAnnuityFutureValue() {
        BigDecimal result = service.calculateAnnuityFutureValue(
            new BigDecimal("10000"),
            new BigDecimal("12"),
            60
        );

        assertTrue(result.compareTo(new BigDecimal("800000")) > 0);
        assertTrue(result.compareTo(new BigDecimal("850000")) < 0);
    }

    // ==================== INFLATION TESTS ====================

    @Test
    @DisplayName("Inflation: ₹50,000 today, 6% inflation for 30 years = ₹2,87,000")
    void testInflationAdjustment() {
        BigDecimal result = service.adjustForInflation(
            new BigDecimal("50000"),
            new BigDecimal("6"),
            30
        );

        assertTrue(result.compareTo(new BigDecimal("280000")) > 0);
        assertTrue(result.compareTo(new BigDecimal("295000")) < 0);
    }

    // ==================== PRESENT VALUE ANNUITY TESTS ====================

    @Test
    @DisplayName("PVA: Need ₹1,00,000/month for 25 years at 8% = corpus ₹10,67,000")
    void testPresentValueAnnuity() {
        BigDecimal result = service.calculatePresentValueAnnuity(
            new BigDecimal("100000"),
            new BigDecimal("8"),
            300
        );

        assertTrue(result.compareTo(new BigDecimal("12000000")) > 0);
        assertTrue(result.compareTo(new BigDecimal("14000000")) < 0);
    }
}
