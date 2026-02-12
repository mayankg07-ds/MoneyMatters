package com.moneymatters.portfolio.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("XIRR Calculator Tests")
public class XIRRCalculatorTest {

    @Test
    @DisplayName("Test XIRR: Simple investment scenario")
    void testSimpleXIRR() {
        // Invested ₹1,00,000 on Jan 1, 2024
        // Portfolio worth ₹1,20,000 on Dec 31, 2024
        // Expected XIRR ≈ 20%

        List<LocalDate> dates = Arrays.asList(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        );

        List<BigDecimal> amounts = Arrays.asList(
            new BigDecimal("-100000"),  // Investment (outflow)
            new BigDecimal("120000")    // Current value (inflow)
        );

        BigDecimal xirr = XIRRCalculator.calculateXIRR(dates, amounts);

        // Should be around 20%
        assertTrue(xirr.compareTo(new BigDecimal("19")) > 0);
        assertTrue(xirr.compareTo(new BigDecimal("21")) < 0);

        System.out.println("Simple XIRR: " + xirr + "%");
    }

    @Test
    @DisplayName("Test XIRR: Multiple investments (SIP-like)")
    void testMultipleInvestmentsXIRR() {
        // Monthly SIP of ₹10,000 for 12 months
        // Final value: ₹1,50,000

        List<LocalDate> dates = Arrays.asList(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 2, 1),
            LocalDate.of(2024, 3, 1),
            LocalDate.of(2024, 4, 1),
            LocalDate.of(2024, 5, 1),
            LocalDate.of(2024, 6, 1),
            LocalDate.of(2024, 7, 1),
            LocalDate.of(2024, 8, 1),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            LocalDate.of(2024, 11, 1),
            LocalDate.of(2024, 12, 1),
            LocalDate.of(2024, 12, 31)
        );

        List<BigDecimal> amounts = Arrays.asList(
            new BigDecimal("-10000"),
            new BigDecimal("-10000"),
            new BigDecimal("-10000"),
            new BigDecimal("-10000"),
            new BigDecimal("-10000"),
            new BigDecimal("-10000"),
            new BigDecimal("-10000"),
            new BigDecimal("-10000"),
            new BigDecimal("-10000"),
            new BigDecimal("-10000"),
            new BigDecimal("-10000"),
            new BigDecimal("-10000"),
            new BigDecimal("150000")  // Total invested: 120000, gain: 30000
        );

        BigDecimal xirr = XIRRCalculator.calculateXIRR(dates, amounts);

        // With 25% gain on ₹1.2L invested over a year, XIRR should be higher
        assertTrue(xirr.compareTo(new BigDecimal("30")) > 0);

        System.out.println("SIP-like XIRR: " + xirr + "%");
    }

    @Test
    @DisplayName("Test XIRR: Buy and Sell scenario")
    void testBuyAndSellXIRR() {
        // Jan 1: Invested ₹1,00,000
        // Jun 1: Sold for ₹1,10,000
        // Dec 31: Current value ₹0

        List<LocalDate> dates = Arrays.asList(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 6, 1)
        );

        List<BigDecimal> amounts = Arrays.asList(
            new BigDecimal("-100000"),
            new BigDecimal("110000")
        );

        BigDecimal xirr = XIRRCalculator.calculateXIRR(dates, amounts);

        // 10% gain in 5 months ≈ 24% annualized
        assertTrue(xirr.compareTo(new BigDecimal("20")) > 0);
        assertTrue(xirr.compareTo(new BigDecimal("28")) < 0);

        System.out.println("Buy-Sell XIRR: " + xirr + "%");
    }

    @Test
    @DisplayName("Test CAGR calculation")
    void testCAGR() {
        BigDecimal beginningValue = new BigDecimal("100000");
        BigDecimal endingValue = new BigDecimal("150000");
        double years = 3.0;

        BigDecimal cagr = XIRRCalculator.calculateCAGR(beginningValue, endingValue, years);

        // (150000/100000)^(1/3) - 1 = 0.1447 = 14.47%
        assertTrue(cagr.compareTo(new BigDecimal("14")) > 0);
        assertTrue(cagr.compareTo(new BigDecimal("15")) < 0);

        System.out.println("CAGR: " + cagr + "%");
    }

    @Test
    @DisplayName("Test Absolute Return")
    void testAbsoluteReturn() {
        BigDecimal invested = new BigDecimal("100000");
        BigDecimal currentValue = new BigDecimal("125000");

        BigDecimal absoluteReturn = XIRRCalculator.calculateAbsoluteReturn(
            invested, currentValue);

        assertEquals(0, absoluteReturn.compareTo(new BigDecimal("25.0000")));

        System.out.println("Absolute Return: " + absoluteReturn + "%");
    }

    @Test
    @DisplayName("Test XIRR: Real-world example (Reliance stock)")
    void testRealWorldXIRR() {
        // Bought 100 shares @ ₹2,500 on Jan 1, 2023
        // Bought 50 shares @ ₹2,700 on Jul 1, 2023
        // Current: 150 shares @ ₹2,900 on Jan 1, 2024

        List<LocalDate> dates = Arrays.asList(
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 7, 1),
            LocalDate.of(2024, 1, 1)
        );

        List<BigDecimal> amounts = Arrays.asList(
            new BigDecimal("-250000"),  // 100 × 2500
            new BigDecimal("-135000"),  // 50 × 2700
            new BigDecimal("435000")    // 150 × 2900
        );

        BigDecimal xirr = XIRRCalculator.calculateXIRR(dates, amounts);

        // Total invested: 385000, Current: 435000, Gain: 50000 (13%)
        // Over 1 year, XIRR should be around 15-20%
        assertTrue(xirr.compareTo(new BigDecimal("10")) > 0);
        assertTrue(xirr.compareTo(new BigDecimal("25")) < 0);

        System.out.println("Real-world XIRR: " + xirr + "%");
    }
}
