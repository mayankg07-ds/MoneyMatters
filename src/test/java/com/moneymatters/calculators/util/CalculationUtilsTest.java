package com.moneymatters.calculators.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class CalculationUtilsTest {

    // ==================== POWER TESTS ====================

    @Test
    void testPowerBasic() {
        // (1.01)^12 ≈ 1.1268
        BigDecimal result = CalculationUtils.power(new BigDecimal("1.01"), 12);
        
        assertTrue(result.compareTo(new BigDecimal("1.1267")) > 0);
        assertTrue(result.compareTo(new BigDecimal("1.1269")) < 0);
    }

    @Test
    void testPowerZero() {
        // Any number to power 0 = 1
        BigDecimal result = CalculationUtils.power(new BigDecimal("2.5"), 0);
        assertEquals(BigDecimal.ONE, result);
    }

    @Test
    void testPowerOne() {
        // Any number to power 1 = itself
        BigDecimal base = new BigDecimal("3.5");
        BigDecimal result = CalculationUtils.power(base, 1);
        assertEquals(base.setScale(10, java.math.RoundingMode.HALF_UP), result);
    }

    @Test
    void testPowerNegative() {
        // (1.10)^-5 = 1/(1.10)^5
        BigDecimal result = CalculationUtils.power(new BigDecimal("1.10"), -5);
        
        // Should be approximately 0.6209
        assertTrue(result.compareTo(new BigDecimal("0.6208")) > 0);
        assertTrue(result.compareTo(new BigDecimal("0.6210")) < 0);
    }

    // ==================== PERCENT TESTS ====================

    @Test
    void testPercentToDecimal() {
        BigDecimal result = CalculationUtils.percentToDecimal(new BigDecimal("12"));
        assertEquals(new BigDecimal("0.12"), result.setScale(2, java.math.RoundingMode.HALF_UP));
    }

    @Test
    void testPercentToDecimalFraction() {
        BigDecimal result = CalculationUtils.percentToDecimal(new BigDecimal("0.5"));
        assertEquals(new BigDecimal("0.005"), result.setScale(3, java.math.RoundingMode.HALF_UP));
    }

    // ==================== FORMAT TESTS ====================

    @Test
    void testFormatTwoDecimals() {
        BigDecimal result = CalculationUtils.format(new BigDecimal("10000.123456"));
        
        assertEquals(2, result.scale());
        assertEquals(new BigDecimal("10000.12"), result);
    }

    @Test
    void testFormatRounding() {
        // 0.125 rounds up to 0.13
        BigDecimal result = CalculationUtils.format(new BigDecimal("10000.125"));
        assertEquals(new BigDecimal("10000.13"), result);
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    void testIsPositive() {
        assertTrue(CalculationUtils.isPositive(new BigDecimal("100")));
        assertTrue(CalculationUtils.isPositive(new BigDecimal("0.01")));
        assertFalse(CalculationUtils.isPositive(new BigDecimal("0")));
        assertFalse(CalculationUtils.isPositive(new BigDecimal("-10")));
        assertFalse(CalculationUtils.isPositive(null));
    }

    @Test
    void testIsNonNegative() {
        assertTrue(CalculationUtils.isNonNegative(new BigDecimal("100")));
        assertTrue(CalculationUtils.isNonNegative(new BigDecimal("0")));
        assertFalse(CalculationUtils.isNonNegative(new BigDecimal("-1")));
        assertFalse(CalculationUtils.isNonNegative(null));
    }

    // ==================== DIVISION TESTS ====================

    @Test
    void testSafeDivideNormal() {
        BigDecimal result = CalculationUtils.safeDivide(
            new BigDecimal("100"),
            new BigDecimal("4")
        );
        assertEquals(new BigDecimal("25"), result.setScale(0, java.math.RoundingMode.HALF_UP));
    }

    @Test
    void testSafeDivideByZero() {
        BigDecimal result = CalculationUtils.safeDivide(
            new BigDecimal("100"),
            new BigDecimal("0")
        );
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testSafeDivideNull() {
        assertEquals(BigDecimal.ZERO, CalculationUtils.safeDivide(null, new BigDecimal("5")));
        assertEquals(BigDecimal.ZERO, CalculationUtils.safeDivide(new BigDecimal("5"), null));
    }
}
