package com.moneymatters.calculators.service;

import org.springframework.stereotype.Service;
import com.moneymatters.calculators.util.CalculationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Core financial mathematics service.
 * Implements all standard financial formulas.
 * 
 * Used by ALL calculators: SIP, Retirement, Loan, etc.
 * 
 * PRINCIPLE: All rates are annual percentages
 * Example: 12% = input 12, not 0.12
 */

@Service
public class FinancialMathService {

    private static final Logger logger = LoggerFactory.getLogger(FinancialMathService.class);
    private static final int SCALE = 10;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    // ==================== FUTURE VALUE ====================

    /**
     * Calculate Future Value: FV = PV × (1 + r)^n
     * 
     * Use case: How much will ₹100 grow to in 5 years at 10%?
     * Answer: ₹100 × (1.10)^5 = ₹161.05
     * 
     * @param presentValue Initial amount (e.g., 100)
     * @param annualRatePercent Annual interest rate (e.g., 12 for 12%)
     * @param years Time period in years
     * @return Future value
     */
     public BigDecimal calculateFutureValue(
            BigDecimal presentValue,
            BigDecimal annualRatePercent,
            BigDecimal years) {

        logger.debug("Calculating FV: PV={}, rate={}, years={}", 
            presentValue, annualRatePercent, years);

        if (!CalculationUtils.isPositive(presentValue) || 
            !CalculationUtils.isNonNegative(years)) {
            return BigDecimal.ZERO;
        }

        BigDecimal rate = CalculationUtils.percentToDecimal(annualRatePercent);
        BigDecimal factor = CalculationUtils.power(
            BigDecimal.ONE.add(rate),
            years.intValue()
        );

        BigDecimal fv = presentValue.multiply(factor);
        return CalculationUtils.format(fv);
    }
    // ==================== PRESENT VALUE ====================

    /**
     * Calculate Present Value: PV = FV / (1 + r)^n
     * 
     * Use case: How much should I invest today to have ₹1,00,000 in 5 years at 10%?
     * Answer: ₹1,00,000 / (1.10)^5 = ₹62,092
     * 
     * @param futureValue Target amount
     * @param annualRatePercent Annual return rate
     * @param years Time period
     * @return Present value
     */
    public BigDecimal calculatePresentValue(
            BigDecimal futureValue,
            BigDecimal annualRatePercent,
            BigDecimal years) {

        logger.debug("Calculating PV: FV={}, rate={}, years={}", 
            futureValue, annualRatePercent, years);

        if (!CalculationUtils.isPositive(futureValue)) {
            return BigDecimal.ZERO;
        }

        BigDecimal fvOfOne = calculateFutureValue(
            BigDecimal.ONE, 
            annualRatePercent, 
            years
        );

        BigDecimal pv = CalculationUtils.safeDivide(futureValue, fvOfOne);
        return CalculationUtils.format(pv);
    }
    // ==================== EMI (LOAN PAYMENT) ====================

    /**
     * Calculate EMI: EMI = P × [r(1 + r)^n] / [(1 + r)^n - 1]
     * 
     * Use case: Monthly payment for ₹50,00,000 loan at 8.5% for 20 years?
     * Answer: ₹41,543
     * 
     * @param principal Loan amount (e.g., 5000000)
     * @param annualRatePercent Annual interest rate (e.g., 8.5)
     * @param tenureMonths Loan tenure in months (e.g., 240 for 20 years)
     * @return Monthly EMI
     */
    public BigDecimal calculateEMI(
            BigDecimal principal,
            BigDecimal annualRatePercent,
            Integer tenureMonths) {

        logger.debug("Calculating EMI: principal={}, rate={}, months={}", 
            principal, annualRatePercent, tenureMonths);

        if (!CalculationUtils.isPositive(principal) || tenureMonths == null || tenureMonths <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyRate = CalculationUtils.percentToDecimal(annualRatePercent)
            .divide(new BigDecimal(12), SCALE, ROUNDING);

        // Edge case: 0% rate
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return CalculationUtils.format(
                principal.divide(new BigDecimal(tenureMonths), SCALE, ROUNDING)
            );
        }

        BigDecimal base = BigDecimal.ONE.add(monthlyRate);
        BigDecimal powerN = CalculationUtils.power(base, tenureMonths);

        BigDecimal numerator = monthlyRate.multiply(powerN);
        BigDecimal denominator = powerN.subtract(BigDecimal.ONE);

        BigDecimal factor = CalculationUtils.safeDivide(numerator, denominator);
        BigDecimal emi = principal.multiply(factor);

        return CalculationUtils.format(emi);
    }
    // ==================== ANNUITY FUTURE VALUE (SIP) ====================

    /**
     * Calculate Annuity Future Value: FVA = PMT × [((1 + r)^n - 1) / r]
     * 
     * Use case: SIP of ₹10,000/month at 12% for 5 years = ?
     * Answer: ₹8,16,700 (approximately)
     * 
     * @param monthlyPayment Monthly deposit (e.g., 10000)
     * @param annualRatePercent Annual return rate (e.g., 12)
     * @param months Total months (e.g., 60 for 5 years)
     * @return Future value of annuity
     */
    public BigDecimal calculateAnnuityFutureValue(
            BigDecimal monthlyPayment,
            BigDecimal annualRatePercent,
            Integer months) {

        logger.debug("Calculating AFV: payment={}, rate={}, months={}", 
            monthlyPayment, annualRatePercent, months);

        if (!CalculationUtils.isPositive(monthlyPayment) || months == null || months <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyRate = CalculationUtils.percentToDecimal(annualRatePercent)
            .divide(new BigDecimal(12), SCALE, ROUNDING);

        // Edge case: 0% rate
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return CalculationUtils.format(
                monthlyPayment.multiply(new BigDecimal(months))
            );
        }

        BigDecimal base = BigDecimal.ONE.add(monthlyRate);
        BigDecimal powerN = CalculationUtils.power(base, months);

        BigDecimal numerator = powerN.subtract(BigDecimal.ONE);
        BigDecimal factor = CalculationUtils.safeDivide(numerator, monthlyRate);

        BigDecimal fva = monthlyPayment.multiply(factor);
        return CalculationUtils.format(fva);
    }

    // ==================== INFLATION ADJUSTMENT ====================

    /**
     * Adjust value for inflation
     * 
     * Use case: ₹50,000 monthly expense today, 6% inflation, 30 years = ?
     * Answer: ₹2,87,000/month (at retirement)
     * 
     * @param currentValue Current amount
     * @param annualInflationPercent Annual inflation rate
     * @param years Years ahead
     * @return Value adjusted for inflation
     */
    public BigDecimal adjustForInflation(
            BigDecimal currentValue,
            BigDecimal annualInflationPercent,
            Integer years) {

        logger.debug("Adjusting for inflation: value={}, inflation={}, years={}", 
            currentValue, annualInflationPercent, years);

        return calculateFutureValue(
            currentValue,
            annualInflationPercent,
            new BigDecimal(years)
        );
    }

    // ==================== PRESENT VALUE OF ANNUITY (RETIREMENT CORPUS) ====================

    /**
     * Calculate PVA: PVA = PMT × [1 - (1 + r)^-n] / r
     * 
     * Use case: Need ₹1,00,000/month for 25 years at 8% return = corpus needed?
     * Answer: ₹10,67,000
     * 
     * @param monthlyPayment Monthly withdrawal amount
     * @param annualRatePercent Annual return rate
     * @param months Total months in retirement
     * @return Corpus needed today
     */
    public BigDecimal calculatePresentValueAnnuity(
            BigDecimal monthlyPayment,
            BigDecimal annualRatePercent,
            Integer months) {

        logger.debug("Calculating PVA: payment={}, rate={}, months={}", 
            monthlyPayment, annualRatePercent, months);

        if (!CalculationUtils.isPositive(monthlyPayment) || months == null || months <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyRate = CalculationUtils.percentToDecimal(annualRatePercent)
            .divide(new BigDecimal(12), SCALE, ROUNDING);

        // Edge case: 0% rate
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return CalculationUtils.format(
                monthlyPayment.multiply(new BigDecimal(months))
            );
        }

        BigDecimal base = BigDecimal.ONE.add(monthlyRate);
        BigDecimal powerN = CalculationUtils.power(base, months);

        BigDecimal discountFactor = CalculationUtils.safeDivide(BigDecimal.ONE, powerN);
        BigDecimal numerator = BigDecimal.ONE.subtract(discountFactor);

        BigDecimal factor = CalculationUtils.safeDivide(numerator, monthlyRate);
        BigDecimal pva = monthlyPayment.multiply(factor);

        return CalculationUtils.format(pva);
    }
}
