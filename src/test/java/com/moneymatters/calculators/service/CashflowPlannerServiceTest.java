package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cashflow Planner Service Tests")
public class CashflowPlannerServiceTest {

    private CashflowPlannerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CashflowPlannerServiceImpl();
    }

    @Test
    @DisplayName("Basic cashflow: 1.3L income, 1L expense")
    void testBasicCashflow() {
        List<CashflowRequest.CashflowItem> incomes = List.of(
            new CashflowRequest.CashflowItem("Salary", new BigDecimal("100000"), "Fixed"),
            new CashflowRequest.CashflowItem("Rental", new BigDecimal("20000"), "Fixed"),
            new CashflowRequest.CashflowItem("Freelance", new BigDecimal("10000"), "Variable")
        );

        List<CashflowRequest.CashflowItem> expenses = List.of(
            new CashflowRequest.CashflowItem("Rent", new BigDecimal("30000"), "Fixed"),
            new CashflowRequest.CashflowItem("Groceries", new BigDecimal("15000"), "Variable"),
            new CashflowRequest.CashflowItem("EMI", new BigDecimal("25000"), "Fixed"),
            new CashflowRequest.CashflowItem("Utilities", new BigDecimal("5000"), "Fixed"),
            new CashflowRequest.CashflowItem("Entertainment", new BigDecimal("10000"), "Discretionary"),
            new CashflowRequest.CashflowItem("Insurance", new BigDecimal("5000"), "Fixed"),
            new CashflowRequest.CashflowItem("Misc", new BigDecimal("10000"), "Variable")
        );

        CashflowRequest request = new CashflowRequest(
            incomes,
            expenses,
            5,  // 5 years
            new BigDecimal("10"),  // 10% income growth
            new BigDecimal("6")    // 6% expense growth
        );

        CashflowResponse response = service.projectCashflow(request);

        // Current month: 130K income
        assertEquals(0, response.getCurrentMonthlyIncome()
            .compareTo(new BigDecimal("130000.00")));

        // Current month: 100K expense
        assertEquals(0, response.getCurrentMonthlyExpense()
            .compareTo(new BigDecimal("100000.00")));

        // Current net cashflow: 30K
        assertEquals(0, response.getCurrentNetCashflow()
            .compareTo(new BigDecimal("30000.00")));

        // Savings rate: ~23%
        assertTrue(response.getCurrentSavingsRate()
            .compareTo(new BigDecimal("23")) > 0);
        assertTrue(response.getCurrentSavingsRate()
            .compareTo(new BigDecimal("24")) < 0);

        // Should have 5 years of projections
        assertEquals(5, response.getProjections().size());

        // Year 1 should match current
        CashflowResponse.YearlyCashflow year1 = response.getProjections().get(0);
        assertEquals(1, year1.getYear());

        // Year 5 income should be higher (10% growth)
        CashflowResponse.YearlyCashflow year5 = response.getProjections().get(4);
        assertTrue(year5.getMonthlyIncome()
            .compareTo(response.getCurrentMonthlyIncome()) > 0);

        // Cumulative savings should be positive
        assertTrue(response.getTotalSavingsOverPeriod()
            .compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Zero growth scenario")
    void testZeroGrowth() {
        List<CashflowRequest.CashflowItem> incomes = List.of(
            new CashflowRequest.CashflowItem("Salary", new BigDecimal("100000"), "Fixed")
        );

        List<CashflowRequest.CashflowItem> expenses = List.of(
            new CashflowRequest.CashflowItem("Rent", new BigDecimal("50000"), "Fixed")
        );

        CashflowRequest request = new CashflowRequest(
            incomes,
            expenses,
            3,
            new BigDecimal("0"),  // No growth
            new BigDecimal("0")
        );

        CashflowResponse response = service.projectCashflow(request);

        // All years should have same income/expense
        List<CashflowResponse.YearlyCashflow> projections = response.getProjections();
        
        for (int i = 0; i < projections.size(); i++) {
            assertEquals(0, projections.get(i).getMonthlyIncome()
                .compareTo(new BigDecimal("100000.00")));
            assertEquals(0, projections.get(i).getMonthlyExpense()
                .compareTo(new BigDecimal("50000.00")));
        }
    }

    @Test
    @DisplayName("Expense exceeds income scenario")
    void testNegativeCashflow() {
        List<CashflowRequest.CashflowItem> incomes = List.of(
            new CashflowRequest.CashflowItem("Salary", new BigDecimal("50000"), "Fixed")
        );

        List<CashflowRequest.CashflowItem> expenses = List.of(
            new CashflowRequest.CashflowItem("Rent", new BigDecimal("60000"), "Fixed")
        );

        CashflowRequest request = new CashflowRequest(
            incomes,
            expenses,
            2,
            new BigDecimal("5"),
            new BigDecimal("5")
        );

        CashflowResponse response = service.projectCashflow(request);

        // Net cashflow should be negative
        assertTrue(response.getCurrentNetCashflow()
            .compareTo(BigDecimal.ZERO) < 0);

        // Savings rate should be negative
        assertTrue(response.getCurrentSavingsRate()
            .compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    @DisplayName("Income grows faster than expenses")
    void testImprovingSavingsRate() {
        List<CashflowRequest.CashflowItem> incomes = List.of(
            new CashflowRequest.CashflowItem("Salary", new BigDecimal("100000"), "Fixed")
        );

        List<CashflowRequest.CashflowItem> expenses = List.of(
            new CashflowRequest.CashflowItem("Living", new BigDecimal("80000"), "Variable")
        );

        CashflowRequest request = new CashflowRequest(
            incomes,
            expenses,
            5,
            new BigDecimal("12"),  // 12% income growth
            new BigDecimal("6")    // 6% expense growth
        );

        CashflowResponse response = service.projectCashflow(request);

        List<CashflowResponse.YearlyCashflow> projections = response.getProjections();

        // Year 1 savings rate
        BigDecimal year1Rate = projections.get(0).getSavingsRate();

        // Year 5 savings rate
        BigDecimal year5Rate = projections.get(4).getSavingsRate();

        // Year 5 should have higher savings rate
        assertTrue(year5Rate.compareTo(year1Rate) > 0);
    }
}
