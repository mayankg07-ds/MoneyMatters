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
public class RetirementPlannerServiceImpl implements RetirementPlannerService {

    private final FinancialMathService financialMathService;

    @Override
    public RetirementPlanResponse calculateRetirementPlan(RetirementPlanRequest request) {
        log.info("Calculating retirement plan: {}", request);

        // Validation: retirement age > current age
        if (request.getRetirementAge() <= request.getCurrentAge()) {
            log.warn("Invalid: retirement age <= current age");
            return createEmptyResponse();
        }

        // Validation: life expectancy >= retirement age
        if (request.getLifeExpectancy() < request.getRetirementAge()) {
            log.warn("Invalid: life expectancy < retirement age");
            return createEmptyResponse();
        }

        // Continue with calculation...
        int yearsToRetirement = request.getRetirementAge() - request.getCurrentAge();
        int yearsInRetirement = request.getLifeExpectancy() - request.getRetirementAge();
        int monthsInRetirement = yearsInRetirement * 12;

        // Monthly expense at retirement (inflated)
        BigDecimal inflatedMonthlyExpense = financialMathService.adjustForInflation(
            request.getCurrentMonthlyExpense(),
            request.getExpectedInflationPercent(),
            yearsToRetirement
        );
        BigDecimal inflatedAnnualExpense = inflatedMonthlyExpense
            .multiply(new BigDecimal(12));

        // Corpus needed to withdraw inflatedMonthlyExpense for yearsInRetirement
        BigDecimal requiredCorpus = BigDecimal.ZERO;
        if (yearsInRetirement > 0) {
            requiredCorpus = financialMathService.calculatePresentValueAnnuity(
                inflatedMonthlyExpense,
                request.getExpectedReturnPostRetirementPercent(),
                monthsInRetirement
            );
        }

        // Existing corpus grown till retirement
        BigDecimal projectedExistingCorpus = financialMathService.calculateFutureValue(
            request.getExistingCorpus(),
            request.getExpectedReturnPreRetirementPercent(),
            new BigDecimal(yearsToRetirement)
        );

        // Shortfall
        BigDecimal corpusShortfall = requiredCorpus.subtract(projectedExistingCorpus);
        if (corpusShortfall.compareTo(BigDecimal.ZERO) < 0) {
            corpusShortfall = BigDecimal.ZERO; // No shortfall
        }

        BigDecimal recommendedMonthlySIP = BigDecimal.ZERO;
        BigDecimal totalSIPInvestment = BigDecimal.ZERO;

        if (corpusShortfall.compareTo(BigDecimal.ZERO) > 0 && yearsToRetirement > 0) {
            recommendedMonthlySIP = calculateReverseSIP(
                corpusShortfall,
                request.getExpectedReturnPreRetirementPercent(),
                yearsToRetirement
            );

            totalSIPInvestment = recommendedMonthlySIP
                .multiply(new BigDecimal(yearsToRetirement * 12));
        }

        // Pre-retirement projections
        List<YearlyRetirementProjection> preRetirementProjections =
            generatePreRetirementProjections(
                request.getCurrentAge(),
                yearsToRetirement,
                request.getExistingCorpus(),
                recommendedMonthlySIP,
                request.getExpectedReturnPreRetirementPercent()
            );

        // Post-retirement projections
        List<YearlyRetirementProjection> postRetirementProjections =
            generatePostRetirementProjections(
                request.getRetirementAge(),
                yearsInRetirement,
                requiredCorpus,
                inflatedMonthlyExpense,
                request.getExpectedReturnPostRetirementPercent()
            );

        // Combine pre and post projections for chart
        List<ChartPoint> chartPoints = new ArrayList<>();

        for (YearlyRetirementProjection proj : preRetirementProjections) {
            chartPoints.add(new ChartPoint(
                "Age " + proj.getAge(),
                proj.getCorpusAtEnd()
            ));
        }

        for (YearlyRetirementProjection proj : postRetirementProjections) {
            chartPoints.add(new ChartPoint(
                "Age " + proj.getAge(),
                proj.getCorpusAtEnd()
            ));
        }

        return new RetirementPlanResponse(
            CalculationUtils.format(inflatedMonthlyExpense),
            CalculationUtils.format(inflatedAnnualExpense),
            CalculationUtils.format(requiredCorpus),
            CalculationUtils.format(projectedExistingCorpus),
            CalculationUtils.format(corpusShortfall),
            CalculationUtils.format(recommendedMonthlySIP),
            CalculationUtils.format(totalSIPInvestment),
            yearsToRetirement,
            yearsInRetirement,
            preRetirementProjections,
            postRetirementProjections,
            chartPoints
        );
    }

    private BigDecimal calculateReverseSIP(
        BigDecimal targetAmount,
        BigDecimal annualReturnPercent,
        int years) {

        int months = years * 12;
        BigDecimal monthlyRate = CalculationUtils.percentToDecimal(annualReturnPercent)
            .divide(new BigDecimal(12), 10, java.math.RoundingMode.HALF_UP);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return CalculationUtils.safeDivide(targetAmount, new BigDecimal(months));
        }

        // Reverse SIP formula: SIP = Target × r / [(1+r)^n - 1]
        BigDecimal base = BigDecimal.ONE.add(monthlyRate);
        BigDecimal powerN = CalculationUtils.power(base, months);
        BigDecimal denominator = powerN.subtract(BigDecimal.ONE);

        BigDecimal monthlySIP = CalculationUtils.safeDivide(
            targetAmount.multiply(monthlyRate),
            denominator
        );

        return CalculationUtils.format(monthlySIP);
    }

    private List<YearlyRetirementProjection> generatePreRetirementProjections(
        int startAge, int years, BigDecimal startingCorpus,
        BigDecimal monthlySIP, BigDecimal annualReturnPercent) {

        List<YearlyRetirementProjection> projections = new ArrayList<>();
        BigDecimal corpus = startingCorpus;
        BigDecimal returnRate = CalculationUtils.percentToDecimal(annualReturnPercent);

        for (int year = 1; year <= years; year++) {
            int age = startAge + year;
            BigDecimal corpusAtStart = corpus;

            // Annual SIP contribution
            BigDecimal sipContribution = monthlySIP.multiply(new BigDecimal(12));

            // Investment return
            BigDecimal investmentReturn = corpus.multiply(returnRate);

            // Corpus at end
            corpus = corpus.add(sipContribution).add(investmentReturn);

            projections.add(new YearlyRetirementProjection(
                year,
                age,
                CalculationUtils.format(corpusAtStart),
                CalculationUtils.format(sipContribution),
                BigDecimal.ZERO, // No withdrawals yet
                CalculationUtils.format(investmentReturn),
                CalculationUtils.format(corpus)
            ));
        }

        return projections;
    }

    private List<YearlyRetirementProjection> generatePostRetirementProjections(
        int retirementAge, int years, BigDecimal startingCorpus,
        BigDecimal monthlyWithdrawal, BigDecimal annualReturnPercent) {

        List<YearlyRetirementProjection> projections = new ArrayList<>();
        BigDecimal corpus = startingCorpus;
        BigDecimal returnRate = CalculationUtils.percentToDecimal(annualReturnPercent);
        BigDecimal annualWithdrawal = monthlyWithdrawal.multiply(new BigDecimal(12));

        for (int year = 1; year <= years; year++) {
            int age = retirementAge + year;
            BigDecimal corpusAtStart = corpus;

            // Investment return
            BigDecimal investmentReturn = corpus.multiply(returnRate);

            // Withdrawals
            corpus = corpus.add(investmentReturn).subtract(annualWithdrawal);

            // Ensure corpus doesn't go negative
            if (corpus.compareTo(BigDecimal.ZERO) < 0) {
                corpus = BigDecimal.ZERO;
            }

            projections.add(new YearlyRetirementProjection(
                year,
                age,
                CalculationUtils.format(corpusAtStart),
                BigDecimal.ZERO, // No SIP contributions
                CalculationUtils.format(annualWithdrawal),
                CalculationUtils.format(investmentReturn),
                CalculationUtils.format(corpus)
            ));
        }

        return projections;
    }

    private RetirementPlanResponse createEmptyResponse() {
        return new RetirementPlanResponse(
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, 0, 0,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
    }
}
