package com.moneymatters.portfolio;

import com.moneymatters.portfolio.dto.FIFOCalculationResult;
import com.moneymatters.portfolio.dto.HoldingResponse;
import com.moneymatters.portfolio.dto.TransactionRequest;
import com.moneymatters.portfolio.dto.TransactionResponse;
import com.moneymatters.portfolio.entity.Holding;
import com.moneymatters.portfolio.entity.Transaction;
import com.moneymatters.portfolio.service.HoldingService;
import com.moneymatters.portfolio.service.TransactionService;
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
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private HoldingService holdingService;

    @Test
    void testBuyTransaction() {
        TransactionRequest request = createBuyTransaction(
            1L, "TEST", new BigDecimal("100"), new BigDecimal("1000")
        );

        TransactionResponse response = transactionService.recordTransaction(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(Transaction.TransactionType.BUY, response.getTransactionType());

        // Verify holding was created/updated
        List<HoldingResponse> holdings = holdingService.getAllHoldingsForUser(1L);
        assertFalse(holdings.isEmpty());
    }

    @Test
    void testFIFOCalculation() {
        // Buy 100 @ 1000
        transactionService.recordTransaction(
            createBuyTransaction(1L, "FIFO", new BigDecimal("100"), new BigDecimal("1000"))
        );

        // Buy 50 @ 1200
        transactionService.recordTransaction(
            createBuyTransaction(1L, "FIFO", new BigDecimal("50"), new BigDecimal("1200"))
        );

        // Calculate FIFO for selling 120 @ 1500
        FIFOCalculationResult result = transactionService.calculateFIFOGain(
            1L, "FIFO", new BigDecimal("120"), new BigDecimal("1500")
        );

        assertNotNull(result);
        assertEquals(2, result.getBatches().size());

        // First batch: 100 shares @ 1000
        assertEquals(new BigDecimal("100"), result.getBatches().get(0).getQuantitySold());
        assertEquals(new BigDecimal("1000"), result.getBatches().get(0).getPurchasePrice());

        // Second batch: 20 shares @ 1200
        assertEquals(new BigDecimal("20"), result.getBatches().get(1).getQuantitySold());
        assertEquals(new BigDecimal("1200"), result.getBatches().get(1).getPurchasePrice());

        // Total gain = (1500-1000)*100 + (1500-1200)*20 = 50000 + 6000 = 56000
        assertTrue(result.getTotalRealizedGain().compareTo(new BigDecimal("56000")) == 0);
    }

    @Test
    void testSellTransaction() {
        // Buy first
        transactionService.recordTransaction(
            createBuyTransaction(1L, "SELL", new BigDecimal("100"), new BigDecimal("1000"))
        );

        // Sell
        TransactionRequest sellRequest = new TransactionRequest(
            1L,
            Transaction.TransactionType.SELL,
            Holding.AssetType.STOCK,
            "SELL Stock",
            "SELL",
            "NSE",
            new BigDecimal("50"),
            new BigDecimal("1200"),
            BigDecimal.ZERO,
            LocalDate.now(),
            "Selling 50"
        );

        TransactionResponse response = transactionService.recordTransaction(sellRequest);

        assertNotNull(response);

        // Verify holding quantity reduced
        List<HoldingResponse> holdings = holdingService.getAllHoldingsForUser(1L);
        HoldingResponse holding = holdings.stream()
            .filter(h -> h.getAssetSymbol().equals("SELL"))
            .findFirst()
            .orElse(null);

        assertNotNull(holding);
        assertEquals(0, new BigDecimal("50").compareTo(holding.getQuantity()));
    }

    private TransactionRequest createBuyTransaction(Long userId, String symbol,
                                                     BigDecimal quantity, BigDecimal price) {
        return new TransactionRequest(
            userId,
            Transaction.TransactionType.BUY,
            Holding.AssetType.STOCK,
            symbol + " Stock",
            symbol,
            "NSE",
            quantity,
            price,
            BigDecimal.ZERO,
            LocalDate.now(),
            "Test transaction"
        );
    }
}
