package com.moneymatters.portfolio.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
public class XIRRCalculator {

    private static final int MAX_ITERATIONS = 1000;
    private static final double PRECISION = 0.000001;
    private static final int SCALE = 10;

    /**
     * Calculate XIRR (Extended Internal Rate of Return)
     * 
     * @param dates List of transaction dates
     * @param amounts List of amounts (negative for investments, positive for redemptions)
     * @return XIRR as a percentage (e.g., 15.5 for 15.5%)
     */
    public static BigDecimal calculateXIRR(List<LocalDate> dates, List<BigDecimal> amounts) {
        if (dates == null || amounts == null || dates.size() != amounts.size()) {
            throw new IllegalArgumentException("Dates and amounts must have same size");
        }

        if (dates.size() < 2) {
            throw new IllegalArgumentException("At least 2 cash flows required for XIRR");
        }

        // Convert to double arrays for calculation
        double[] doubleAmounts = amounts.stream()
            .mapToDouble(BigDecimal::doubleValue)
            .toArray();

        long[] daysDifference = new long[dates.size()];
        LocalDate firstDate = dates.get(0);
        
        for (int i = 0; i < dates.size(); i++) {
            daysDifference[i] = ChronoUnit.DAYS.between(firstDate, dates.get(i));
        }

        // Use Newton-Raphson method to find rate
        double rate = newtonRaphson(doubleAmounts, daysDifference);

        // Convert to percentage
        BigDecimal xirr = BigDecimal.valueOf(rate * 100)
            .setScale(4, RoundingMode.HALF_UP);

        log.debug("Calculated XIRR: {}%", xirr);
        return xirr;
    }

    /**
     * Newton-Raphson method to solve for rate
     */
    private static double newtonRaphson(double[] amounts, long[] days) {
        double rate = 0.1; // Initial guess: 10%

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double f = calculateNPV(amounts, days, rate);
            double df = calculateNPVDerivative(amounts, days, rate);

            if (Math.abs(df) < PRECISION) {
                break;
            }

            double newRate = rate - (f / df);

            if (Math.abs(newRate - rate) < PRECISION) {
                return newRate;
            }

            rate = newRate;
        }

        return rate;
    }

    /**
     * Calculate Net Present Value for given rate
     */
    private static double calculateNPV(double[] amounts, long[] days, double rate) {
        double npv = 0.0;

        for (int i = 0; i < amounts.length; i++) {
            double exponent = days[i] / 365.0;
            npv += amounts[i] / Math.pow(1.0 + rate, exponent);
        }

        return npv;
    }

    /**
     * Calculate derivative of NPV (for Newton-Raphson)
     */
    private static double calculateNPVDerivative(double[] amounts, long[] days, double rate) {
        double derivative = 0.0;

        for (int i = 0; i < amounts.length; i++) {
            double exponent = days[i] / 365.0;
            derivative -= (amounts[i] * exponent) / Math.pow(1.0 + rate, exponent + 1);
        }

        return derivative;
    }

    /**
     * Calculate CAGR (Compound Annual Growth Rate)
     * 
     * @param beginningValue Starting value
     * @param endingValue Ending value
     * @param years Number of years
     * @return CAGR as percentage
     */
    public static BigDecimal calculateCAGR(BigDecimal beginningValue, 
                                           BigDecimal endingValue, 
                                           double years) {
        if (beginningValue.compareTo(BigDecimal.ZERO) <= 0 || years <= 0) {
            return BigDecimal.ZERO;
        }

        double beginning = beginningValue.doubleValue();
        double ending = endingValue.doubleValue();

        double cagr = (Math.pow(ending / beginning, 1.0 / years) - 1) * 100;

        return BigDecimal.valueOf(cagr).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Calculate absolute return
     */
    public static BigDecimal calculateAbsoluteReturn(BigDecimal invested, 
                                                     BigDecimal currentValue) {
        if (invested.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal gain = currentValue.subtract(invested);
        return gain.multiply(new BigDecimal(100))
            .divide(invested, 4, RoundingMode.HALF_UP);
    }
}
