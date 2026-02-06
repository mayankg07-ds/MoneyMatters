package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.*;
import com.moneymatters.calculators.util.CalculationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SIPCalculatorServiceImpl implements SIPCalculatorService {

    private final FinancialMathService financialMathService;

    @Override
    public SIPStepupResponse calculateStepupSIP(SIPStepupRequest request) {
        log.debug("Calculating SIP Step-up: {}", request);

        // Basic validation guards (business-level, separate from @Valid)
        if (request == null
                || request.getYears() == null
                || request.getYears() <= 0
                || request.getMonthlySIP() == null
                || request.getMonthlySIP().compareTo(BigDecimal.ZERO) <= 0) {

            return new SIPStepupResponse(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new ArrayList<>(),
                new ArrayList<>()
            );
        }

        BigDecimal baseMonthlySIP = request.getMonthlySIP();
        BigDecimal annualReturnPercent = request.getExpectedAnnualReturnPercent();
        BigDecimal annualStepupPercent = request.getAnnualStepupPercent();
        int years = request.getYears();

        // Step-up factor per year = 1 + s
        BigDecimal stepupFactor = BigDecimal.ONE.add(
            CalculationUtils.percentToDecimal(annualStepupPercent)
        );

        // We'll reuse FinancialMathService for annuity FV and future value
        List<YearlyBreakdown> breakdowns = new ArrayList<>();
        List<ChartPoint> chartPoints = new ArrayList<>();

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalMaturityValue = BigDecimal.ZERO;

        BigDecimal currentMonthlySIP = baseMonthlySIP;

        for (int year = 1; year <= years; year++) {

            // 1) Yearly contribution
            BigDecimal yearlyContribution = currentMonthlySIP.multiply(new BigDecimal(12));

            // 2) FV at end of this year using annuity formula for 12 months
            BigDecimal yearMaturityAtYearEnd = financialMathService.calculateAnnuityFutureValue(
                currentMonthlySIP,
                annualReturnPercent,
                12
            );

            // 3) Grow from end of this year to final year
            int remainingYears = years - year;
            BigDecimal yearMaturityAtFinal = yearMaturityAtYearEnd;

            if (remainingYears > 0) {
                yearMaturityAtFinal = financialMathService.calculateFutureValue(
                    yearMaturityAtYearEnd,
                    annualReturnPercent,
                    new BigDecimal(remainingYears)
                );
            }

            totalInvested = totalInvested.add(yearlyContribution);
            totalMaturityValue = totalMaturityValue.add(yearMaturityAtFinal);

            breakdowns.add(new YearlyBreakdown(
                year,
                CalculationUtils.format(currentMonthlySIP),
                CalculationUtils.format(yearlyContribution),
                CalculationUtils.format(yearMaturityAtYearEnd),
                CalculationUtils.format(yearMaturityAtFinal)
            ));

            chartPoints.add(new ChartPoint(
                "Year " + year,
                CalculationUtils.format(totalMaturityValue)
            ));

            // Step up SIP for next year
            currentMonthlySIP = currentMonthlySIP.multiply(stepupFactor);
        }

        BigDecimal wealthGained = totalMaturityValue.subtract(totalInvested);

        BigDecimal firstYearSIPFormatted = CalculationUtils.format(baseMonthlySIP);
        BigDecimal lastYearSIPFormatted = breakdowns.isEmpty()
                ? BigDecimal.ZERO
                : breakdowns.get(breakdowns.size() - 1).getMonthlySIP();

        return new SIPStepupResponse(
            CalculationUtils.format(totalInvested),
            CalculationUtils.format(totalMaturityValue),
            CalculationUtils.format(wealthGained),
            firstYearSIPFormatted,
            lastYearSIPFormatted,
            breakdowns,
            chartPoints
        );
    }
}
