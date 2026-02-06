package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.SIPStepupRequest;
import com.moneymatters.calculators.dto.SIPStepupResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class SIPCalculatorServiceTest {

    private SIPCalculatorServiceImpl service;

    @BeforeEach
    void setUp() {
        FinancialMathService mathService = new FinancialMathService();
        service = new SIPCalculatorServiceImpl(mathService);
    }

    @Test
    void testBasicStepupExample3Years() {
        SIPStepupRequest request = new SIPStepupRequest(
            new BigDecimal("10000"),   // monthly SIP
            new BigDecimal("12"),      // 12% annual
            3,                         // 3 years
            new BigDecimal("10")       // 10% step-up per year
        );

        SIPStepupResponse response = service.calculateStepupSIP(request);

        // Total invested from manual example: 3,97,200
        BigDecimal expectedInvested = new BigDecimal("397200");
        assertEquals(0, response.getTotalInvested().compareTo(expectedInvested),
            "Total invested should match manual calculation");

        // Maturity value should be greater than invested
        assertTrue(response.getMaturityValue().compareTo(response.getTotalInvested()) > 0);

        // Wealth gained = maturity - invested > 0
        assertTrue(response.getWealthGained().compareTo(BigDecimal.ZERO) > 0);

        // First and last year SIP
        assertEquals(new BigDecimal("10000.00"), response.getFirstYearMonthlySIP());
        assertEquals(new BigDecimal("12100.00"), response.getLastYearMonthlySIP());

        // Expect 3 rows of yearly breakdown
        assertEquals(3, response.getYearlyBreakdown().size());
    }

    @Test
    void testStepupZeroStepBehavesLikeNormalSIP() {
        SIPStepupRequest request = new SIPStepupRequest(
            new BigDecimal("10000"),
            new BigDecimal("12"),
            5,
            new BigDecimal("0")  // no step-up
        );

        SIPStepupResponse response = service.calculateStepupSIP(request);

        // Total invested = 10000 * 12 * 5
        BigDecimal expectedInvested = new BigDecimal("600000");
        assertEquals(0, response.getTotalInvested().compareTo(expectedInvested));

        // Maturity should be between 6L and 9L roughly (depending on compounding)
        assertTrue(response.getMaturityValue().compareTo(new BigDecimal("600000")) > 0);
    }

    @Test
    void testInvalidYearsReturnsZero() {
        SIPStepupRequest request = new SIPStepupRequest(
            new BigDecimal("10000"),
            new BigDecimal("12"),
            0,                         // invalid
            new BigDecimal("10")
        );

        SIPStepupResponse response = service.calculateStepupSIP(request);

        assertEquals(BigDecimal.ZERO, response.getTotalInvested());
        assertEquals(BigDecimal.ZERO, response.getMaturityValue());
        assertEquals(BigDecimal.ZERO, response.getWealthGained());
    }
}
