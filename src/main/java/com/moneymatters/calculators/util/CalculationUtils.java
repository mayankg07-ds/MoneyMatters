package com.moneymatters.calculators.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for financial calculations.
 * Provides reusable methods for all calculators.
 * 
 * KEY PRINCIPLE: Always use BigDecimal, never float/double
 */

public class CalculationUtils {

    private static final int SCALE = 10;  // Precision: 10 decimal places
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Calculate (base)^exponent
     * 
     * Used in: compound interest, annuity, EMI formulas
     * Example: power(1.01, 12) = 1.01^12 = 1.1268
     * 
     * @param base The base number (usually 1 + rate)
     * @param exponent The power
     * @return base^exponent with high precision
     */
    public static BigDecimal power(BigDecimal base, int exponent) {
        if (exponent == 0) {
            return BigDecimal.ONE;
        }
        
        if (exponent < 0) {
            return BigDecimal.ONE.divide(
                power(base, Math.abs(exponent)), 
                SCALE, 
                ROUNDING
            );
        }

        double result = Math.pow(base.doubleValue(), exponent);
        return new BigDecimal(result).setScale(SCALE, ROUNDING);
    }

    /**
     * Format to 2 decimal places (for currency display)
     * 
     * @param value The value to format
     * @return Formatted value (2 decimal places)
     */
    public static BigDecimal format(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, ROUNDING);
    }

    /**
     * Convert percentage to decimal
     * 
     * Example: percentToDecimal(12) = 0.12
     * 
     * @param percent The percentage (12 for 12%)
     * @return The decimal (0.12)
     */
    
    public static BigDecimal percentToDecimal(BigDecimal percent) {
        if (percent == null) {
            return BigDecimal.ZERO;
        }
        return percent.divide(new BigDecimal(100), SCALE, ROUNDING);
    }

    /**
     * Check if value is positive (> 0)
     * 
     * @param value The value to check
     * @return true if value > 0
     */
        public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if value is non-negative (>= 0)
     * 
     * @param value The value to check
     * @return true if value >= 0
     */
    public static boolean isNonNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Safe division (handles zero divisor)
     * 
     * @param dividend The numerator
     * @param divisor The denominator
     * @return dividend / divisor, or 0 if divisor is 0
     */
      public static BigDecimal safeDivide(BigDecimal dividend, BigDecimal divisor) {
        if (dividend == null || divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return dividend.divide(divisor, SCALE, ROUNDING);
    }
}
    