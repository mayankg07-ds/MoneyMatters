package com.moneymatters.portfolio;

import com.moneymatters.portfolio.dto.HoldingRequest;
import com.moneymatters.portfolio.dto.HoldingResponse;
import com.moneymatters.portfolio.dto.PortfolioSummaryResponse;
import com.moneymatters.portfolio.entity.Holding;
import com.moneymatters.portfolio.repository.HoldingRepository;
import com.moneymatters.portfolio.service.HoldingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class HoldingServiceTest {

    @Autowired
    private HoldingService holdingService;

    @Autowired
    private HoldingRepository holdingRepository;

    @Test
    void testCreateHolding() {
        HoldingRequest request = new HoldingRequest(
            1L,
            Holding.AssetType.STOCK,
            "Test Company",
            "TEST",
            "NSE",
            new BigDecimal("100"),
            new BigDecimal("1000.00"),
            LocalDate.now()
        );

        HoldingResponse response = holdingService.createHolding(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("TEST", response.getAssetSymbol());
        assertEquals(0, new BigDecimal("100").compareTo(response.getQuantity()));
        assertEquals(0, new BigDecimal("100000.00").compareTo(response.getTotalInvested()));
    }

    @Test
    void testGetPortfolioSummary() {
        // Create multiple holdings
        createTestHolding(1L, "STOCK1", Holding.AssetType.STOCK, 
            new BigDecimal("100"), new BigDecimal("1000"));
        createTestHolding(1L, "STOCK2", Holding.AssetType.STOCK, 
            new BigDecimal("50"), new BigDecimal("2000"));
        createTestHolding(1L, "BOND1", Holding.AssetType.BOND, 
            new BigDecimal("10"), new BigDecimal("10000"));

        PortfolioSummaryResponse summary = holdingService.getPortfolioSummary(1L);

        assertNotNull(summary);
        assertEquals(3, summary.getTotalHoldings());
        assertTrue(summary.getTotalInvested().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(summary.getAssetTypeBreakdown().size() >= 2); // At least STOCK and BOND
    }

    private void createTestHolding(Long userId, String symbol, Holding.AssetType type,
                                   BigDecimal quantity, BigDecimal price) {
        HoldingRequest request = new HoldingRequest(
            userId, type, symbol + " Company", symbol, "NSE",
            quantity, price, LocalDate.now()
        );
        holdingService.createHolding(request);
    }
}
