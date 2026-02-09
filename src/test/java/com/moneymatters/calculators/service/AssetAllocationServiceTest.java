package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Asset Allocation Service Tests")
public class AssetAllocationServiceTest {

    private AssetAllocationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AssetAllocationServiceImpl();
    }

    @Test
    @DisplayName("Basic rebalancing: 10L portfolio with drift")
    void testBasicRebalancing() {
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
            holdings, targets, BigDecimal.ZERO
        );

        AssetAllocationResponse response = service.calculateRebalancing(request);

        // Total portfolio = 10L
        assertEquals(0, response.getTotalPortfolioValue()
            .compareTo(new BigDecimal("1000000")));

        // Should have 3 asset analyses
        assertEquals(3, response.getAssetAnalyses().size());

        // Equity should show +10% drift
        AssetAllocationResponse.AssetAnalysis equityAnalysis = 
            response.getAssetAnalyses().stream()
                .filter(a -> a.getAssetName().equals("Equity"))
                .findFirst().orElseThrow();
        
        assertTrue(equityAnalysis.getDrift().compareTo(new BigDecimal("10")) == 0);

        // Should have rebalancing actions
        assertFalse(response.isBalanced());
        assertTrue(response.getRebalancingActions().size() > 0);
    }

    @Test
    @DisplayName("Already balanced portfolio")
    void testAlreadyBalanced() {
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
            holdings, targets, BigDecimal.ZERO
        );

        AssetAllocationResponse response = service.calculateRebalancing(request);

        // Should be balanced
        assertTrue(response.isBalanced());
        
        // All actions should be HOLD
        response.getRebalancingActions().forEach(action ->
            assertEquals("HOLD", action.getAction())
        );
    }

    @Test
    @DisplayName("Fresh investment added")
    void testWithFreshInvestment() {
        List<AssetAllocationRequest.AssetClass> holdings = List.of(
            new AssetAllocationRequest.AssetClass("Equity", new BigDecimal("600000")),
            new AssetAllocationRequest.AssetClass("Debt", new BigDecimal("400000"))
        );

        List<AssetAllocationRequest.TargetAllocation> targets = List.of(
            new AssetAllocationRequest.TargetAllocation("Equity", new BigDecimal("60")),
            new AssetAllocationRequest.TargetAllocation("Debt", new BigDecimal("40"))
        );

        // Add 1L fresh investment
        AssetAllocationRequest request = new AssetAllocationRequest(
            holdings, targets, new BigDecimal("100000")
        );

        AssetAllocationResponse response = service.calculateRebalancing(request);

        // Total should be 11L
        assertEquals(0, response.getTotalPortfolioValue()
            .compareTo(new BigDecimal("1100000")));

        // Fresh investment should be allocated per target %
        assertTrue(response.getRebalancingActions().stream()
            .anyMatch(a -> a.getAction().equals("BUY")));
    }
}
