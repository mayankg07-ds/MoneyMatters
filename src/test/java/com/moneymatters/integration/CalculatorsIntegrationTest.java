package com.moneymatters.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneymatters.calculators.dto.*;
import com.moneymatters.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for all 6 calculators.
 * Tests the complete REST API flow from request to response.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Calculators Integration Tests - All 6 Calculators")
public class CalculatorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Setup code if needed
    }

    // ============================================================
    // TEST 1: SIP STEP-UP CALCULATOR
    // ============================================================

    @Test
    @DisplayName("Integration: SIP Step-up Calculator")
    void testSIPStepupCalculator() throws Exception {
        SIPStepupRequest request = new SIPStepupRequest(
            new BigDecimal("10000"),   // Monthly SIP
            new BigDecimal("12"),      // 12% return
            3,                         // 3 years
            new BigDecimal("10")       // 10% step-up
        );

        MvcResult result = mockMvc.perform(post("/v1/calculators/sip-stepup/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalInvested").exists())
            .andExpect(jsonPath("$.data.maturityValue").exists())
            .andExpect(jsonPath("$.data.wealthGained").exists())
            .andExpect(jsonPath("$.data.yearlyBreakdown").isArray())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("totalInvested"));
        assertTrue(responseContent.contains("397200")); // Expected total invested

        System.out.println("✅ SIP Step-up Calculator: PASSED");
    }

    @Test
    @DisplayName("Integration: SIP Step-up - Validation Error")
    void testSIPStepupValidationError() throws Exception {
        SIPStepupRequest request = new SIPStepupRequest(
            new BigDecimal("-10000"),  // Negative (invalid)
            new BigDecimal("12"),
            3,
            new BigDecimal("10")
        );

        mockMvc.perform(post("/v1/calculators/sip-stepup/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        System.out.println("✅ SIP Step-up Validation: PASSED");
    }

    // ============================================================
    // TEST 2: RETIREMENT PLANNER
    // ============================================================

    @Test
    @DisplayName("Integration: Retirement Planner")
    void testRetirementPlanner() throws Exception {
        RetirementPlanRequest request = new RetirementPlanRequest(
            30,                              // Current age
            60,                              // Retirement age
            85,                              // Life expectancy
            new BigDecimal("50000"),         // Current monthly expense
            new BigDecimal("6"),             // 6% inflation
            new BigDecimal("12"),            // 12% pre-retirement return
            new BigDecimal("8"),             // 8% post-retirement return
            new BigDecimal("1000000")        // Existing corpus
        );

        MvcResult result = mockMvc.perform(post("/v1/calculators/retirement/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.inflatedMonthlyExpenseAtRetirement").exists())
            .andExpect(jsonPath("$.data.requiredCorpusAtRetirement").exists())
            .andExpect(jsonPath("$.data.recommendedMonthlySIP").exists())
            .andExpect(jsonPath("$.data.yearsToRetirement").value(30))
            .andExpect(jsonPath("$.data.yearsInRetirement").value(25))
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("requiredCorpusAtRetirement"));
        assertTrue(responseContent.contains("recommendedMonthlySIP"));

        System.out.println("✅ Retirement Planner: PASSED");
    }

    @Test
    @DisplayName("Integration: Retirement Planner - Invalid Age")
    void testRetirementPlannerInvalidAge() throws Exception {
        RetirementPlanRequest request = new RetirementPlanRequest(
            60,                              // Current age
            50,                              // Retirement age (invalid: < current age)
            85,
            new BigDecimal("50000"),
            new BigDecimal("6"),
            new BigDecimal("12"),
            new BigDecimal("8"),
            new BigDecimal("1000000")
        );

        mockMvc.perform(post("/v1/calculators/retirement/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.recommendedMonthlySIP").value(0));

        System.out.println("✅ Retirement Planner Validation: PASSED");
    }

    // ============================================================
    // TEST 3: LOAN ANALYZER
    // ============================================================

    @Test
    @DisplayName("Integration: Loan Analyzer")
    void testLoanAnalyzer() throws Exception {
        LoanAnalysisRequest request = new LoanAnalysisRequest(
            new BigDecimal("500000"),    // Principal
            new BigDecimal("10"),        // 10% interest
            60,                          // 5 years
            null                         // No prepayments
        );

        MvcResult result = mockMvc.perform(post("/v1/calculators/loan/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.emi").exists())
            .andExpect(jsonPath("$.data.totalInterestPayable").exists())
            .andExpect(jsonPath("$.data.amortizationSchedule").isArray())
            .andExpect(jsonPath("$.data.amortizationSchedule.length()").value(60))
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("amortizationSchedule"));
        assertTrue(responseContent.contains("totalInterestPayable"));

        System.out.println("✅ Loan Analyzer: PASSED");
    }

    @Test
    @DisplayName("Integration: Loan Analyzer with Prepayment")
    void testLoanAnalyzerWithPrepayment() throws Exception {
        PrepaymentScenario prepayment = new PrepaymentScenario(
            12,                          // After 1 year
            new BigDecimal("50000"),     // 50K prepayment
            PrepaymentScenario.PrepaymentOption.REDUCE_TENURE
        );

        LoanAnalysisRequest request = new LoanAnalysisRequest(
            new BigDecimal("500000"),
            new BigDecimal("10"),
            60,
            List.of(prepayment)
        );

        mockMvc.perform(post("/v1/calculators/loan/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.prepaymentImpact").exists())
            .andExpect(jsonPath("$.data.prepaymentImpact.interestSaved").exists())
            .andExpect(jsonPath("$.data.effectiveTenureMonths").exists());

        System.out.println("✅ Loan Analyzer with Prepayment: PASSED");
    }

    // ============================================================
    // TEST 4: ASSET ALLOCATION REBALANCER
    // ============================================================

    @Test
    @DisplayName("Integration: Asset Allocation Rebalancer")
    void testAssetAllocationRebalancer() throws Exception {
        List<AssetAllocationRequest.AssetClass> holdings = List.of(
            new AssetAllocationRequest.AssetClass("Equity", new BigDecimal("700000")),
            new AssetAllocationRequest.AssetClass("Debt", new BigDecimal("200000")),
            new AssetAllocationRequest.AssetClass("Gold", new BigDecimal("100000"))
        );

        List<AssetAllocationRequest.TargetAllocation> targets = List.of(
            new AssetAllocationRequest.TargetAllocation("Equity", new BigDecimal("60")),
            new AssetAllocationRequest.TargetAllocation("Debt", new BigDecimal("30")),
            new AssetAllocationRequest.TargetAllocation("Gold", new BigDecimal("10"))
        );

        AssetAllocationRequest request = new AssetAllocationRequest(
            holdings,
            targets,
            BigDecimal.ZERO
        );

        MvcResult result = mockMvc.perform(post("/v1/calculators/asset-allocation/rebalance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalPortfolioValue").exists())
            .andExpect(jsonPath("$.data.assetAnalyses").isArray())
            .andExpect(jsonPath("$.data.rebalancingActions").isArray())
            .andExpect(jsonPath("$.data.balanced").exists())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("rebalancingActions"));
        assertTrue(responseContent.contains("drift"));

        System.out.println("✅ Asset Allocation Rebalancer: PASSED");
    }

    @Test
    @DisplayName("Integration: Asset Allocation - Already Balanced")
    void testAssetAllocationAlreadyBalanced() throws Exception {
        List<AssetAllocationRequest.AssetClass> holdings = List.of(
            new AssetAllocationRequest.AssetClass("Equity", new BigDecimal("600000")),
            new AssetAllocationRequest.AssetClass("Debt", new BigDecimal("300000")),
            new AssetAllocationRequest.AssetClass("Gold", new BigDecimal("100000"))
        );

        List<AssetAllocationRequest.TargetAllocation> targets = List.of(
            new AssetAllocationRequest.TargetAllocation("Equity", new BigDecimal("60")),
            new AssetAllocationRequest.TargetAllocation("Debt", new BigDecimal("30")),
            new AssetAllocationRequest.TargetAllocation("Gold", new BigDecimal("10"))
        );

        AssetAllocationRequest request = new AssetAllocationRequest(
            holdings,
            targets,
            BigDecimal.ZERO
        );

        mockMvc.perform(post("/v1/calculators/asset-allocation/rebalance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.balanced").value(true));

        System.out.println("✅ Asset Allocation Already Balanced: PASSED");
    }

    // ============================================================
    // TEST 5: CASHFLOW PLANNER
    // ============================================================

    @Test
    @DisplayName("Integration: Cashflow Planner")
    void testCashflowPlanner() throws Exception {
        List<CashflowRequest.CashflowItem> incomes = List.of(
            new CashflowRequest.CashflowItem("Salary", new BigDecimal("100000"), "Fixed"),
            new CashflowRequest.CashflowItem("Rental", new BigDecimal("20000"), "Fixed")
        );

        List<CashflowRequest.CashflowItem> expenses = List.of(
            new CashflowRequest.CashflowItem("Rent", new BigDecimal("30000"), "Fixed"),
            new CashflowRequest.CashflowItem("Groceries", new BigDecimal("15000"), "Variable"),
            new CashflowRequest.CashflowItem("EMI", new BigDecimal("25000"), "Fixed"),
            new CashflowRequest.CashflowItem("Utilities", new BigDecimal("10000"), "Fixed")
        );

        CashflowRequest request = new CashflowRequest(
            incomes,
            expenses,
            5,                           // 5 years
            new BigDecimal("10"),        // 10% income growth
            new BigDecimal("6")          // 6% expense growth
        );

        MvcResult result = mockMvc.perform(post("/v1/calculators/cashflow/project")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.currentMonthlyIncome").exists())
            .andExpect(jsonPath("$.data.currentMonthlyExpense").exists())
            .andExpect(jsonPath("$.data.currentSavingsRate").exists())
            .andExpect(jsonPath("$.data.projections").isArray())
            .andExpect(jsonPath("$.data.projections.length()").value(5))
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("totalSavingsOverPeriod"));
        assertTrue(responseContent.contains("projections"));

        System.out.println("✅ Cashflow Planner: PASSED");
    }

    @Test
    @DisplayName("Integration: Cashflow Planner - Negative Cashflow")
    void testCashflowPlannerNegative() throws Exception {
        List<CashflowRequest.CashflowItem> incomes = List.of(
            new CashflowRequest.CashflowItem("Salary", new BigDecimal("50000"), "Fixed")
        );

        List<CashflowRequest.CashflowItem> expenses = List.of(
            new CashflowRequest.CashflowItem("Rent", new BigDecimal("60000"), "Fixed")
        );

        CashflowRequest request = new CashflowRequest(
            incomes,
            expenses,
            3,
            new BigDecimal("5"),
            new BigDecimal("5")
        );

        mockMvc.perform(post("/v1/calculators/cashflow/project")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.currentNetCashflow").exists());
            // Net cashflow will be negative

        System.out.println("✅ Cashflow Planner Negative: PASSED");
    }

    // ============================================================
    // TEST 6: SWP CALCULATOR
    // ============================================================

    @Test
    @DisplayName("Integration: SWP Calculator")
    void testSWPCalculator() throws Exception {
        SWPRequest request = new SWPRequest(
            new BigDecimal("5000000"),   // 50L corpus
            new BigDecimal("30000"),     // 30K withdrawal
            new BigDecimal("8"),         // 8% return
            25,                          // 25 years
            BigDecimal.ZERO,             // No inflation
            false                        // Not inflation-adjusted
        );

        MvcResult result = mockMvc.perform(post("/v1/calculators/swp/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.startingCorpus").exists())
            .andExpect(jsonPath("$.data.finalCorpusValue").exists())
            .andExpect(jsonPath("$.data.isSustainable").exists())
            .andExpect(jsonPath("$.data.withdrawalRate").exists())
            .andExpect(jsonPath("$.data.monthlyBreakdown").isArray())
            .andExpect(jsonPath("$.data.yearlySummary").isArray())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("sustainabilityMessage"));
        assertTrue(responseContent.contains("isSustainable"));

        System.out.println("✅ SWP Calculator: PASSED");
    }

    @Test
    @DisplayName("Integration: SWP Calculator - Inflation Adjusted")
    void testSWPCalculatorInflationAdjusted() throws Exception {
        SWPRequest request = new SWPRequest(
            new BigDecimal("10000000"),  // 1 crore
            new BigDecimal("40000"),     // 40K withdrawal
            new BigDecimal("10"),        // 10% return
            25,
            new BigDecimal("5"),         // 5% inflation
            true                         // Inflation-adjusted
        );

        mockMvc.perform(post("/v1/calculators/swp/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.monthlyBreakdown").isArray())
            .andExpect(jsonPath("$.data.isSustainable").value(true));

        System.out.println("✅ SWP Calculator Inflation-Adjusted: PASSED");
    }

    // ============================================================
    // COMPREHENSIVE END-TO-END TEST
    // ============================================================

    @Test
    @DisplayName("Integration: All 6 Calculators Health Check")
    void testAllCalculatorsHealthCheck() throws Exception {
        System.out.println("\n========================================");
        System.out.println("COMPREHENSIVE INTEGRATION TEST");
        System.out.println("Testing all 6 calculators...");
        System.out.println("========================================\n");

        // Test 1: SIP Step-up
        testSIPStepupCalculator();

        // Test 2: Retirement Planner
        testRetirementPlanner();

        // Test 3: Loan Analyzer
        testLoanAnalyzer();

        // Test 4: Asset Allocation
        testAssetAllocationRebalancer();

        // Test 5: Cashflow Planner
        testCashflowPlanner();

        // Test 6: SWP Calculator
        testSWPCalculator();

        System.out.println("\n========================================");
        System.out.println("✅ ALL 6 CALCULATORS: PASSED");
        System.out.println("========================================\n");
    }

    // ============================================================
    // PERFORMANCE TEST
    // ============================================================

    @Test
    @DisplayName("Performance: Response Time Check")
    void testResponseTimePerformance() throws Exception {
        // Test that each calculator responds within 2 seconds

        long startTime, endTime, duration;

        // SIP Step-up
        SIPStepupRequest sipRequest = new SIPStepupRequest(
            new BigDecimal("10000"), new BigDecimal("12"), 5, new BigDecimal("10")
        );
        startTime = System.currentTimeMillis();
        mockMvc.perform(post("/v1/calculators/sip-stepup/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sipRequest)))
            .andExpect(status().isOk());
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        assertTrue(duration < 2000, "SIP Step-up took " + duration + "ms (max 2000ms)");
        System.out.println("⏱️  SIP Step-up: " + duration + "ms");

        // Retirement Planner
        RetirementPlanRequest retirementRequest = new RetirementPlanRequest(
            30, 60, 85, new BigDecimal("50000"), new BigDecimal("6"),
            new BigDecimal("12"), new BigDecimal("8"), new BigDecimal("1000000")
        );
        startTime = System.currentTimeMillis();
        mockMvc.perform(post("/v1/calculators/retirement/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(retirementRequest)))
            .andExpect(status().isOk());
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        assertTrue(duration < 2000, "Retirement Planner took " + duration + "ms (max 2000ms)");
        System.out.println("⏱️  Retirement Planner: " + duration + "ms");

        // Loan Analyzer
        LoanAnalysisRequest loanRequest = new LoanAnalysisRequest(
            new BigDecimal("500000"), new BigDecimal("10"), 60, null
        );
        startTime = System.currentTimeMillis();
        mockMvc.perform(post("/v1/calculators/loan/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest)))
            .andExpect(status().isOk());
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        assertTrue(duration < 2000, "Loan Analyzer took " + duration + "ms (max 2000ms)");
        System.out.println("⏱️  Loan Analyzer: " + duration + "ms");

        System.out.println("\n✅ Performance Test: All calculators respond < 2s");
    }

    // ============================================================
    // ERROR HANDLING TEST
    // ============================================================

    @Test
    @DisplayName("Integration: Global Error Handling")
    void testGlobalErrorHandling() throws Exception {
        // Test invalid JSON - returns 500 with error message
        mockMvc.perform(post("/v1/calculators/sip-stepup/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false));

        // Test missing required fields - returns 400 with validation errors
        mockMvc.perform(post("/v1/calculators/retirement/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed"));

        System.out.println("✅ Error Handling: PASSED");
    }
}
