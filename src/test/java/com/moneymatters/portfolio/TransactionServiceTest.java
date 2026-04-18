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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private HoldingService holdingService;

    private static final String TEST_USER = "test-user-unit-2";

    @Test
    void testBuyTransaction() {
        TransactionRequest request = createBuyTransaction("TEST", new BigDecimal("100"), new BigDecimal("1000"));

        TransactionResponse response = transactionService.recordTransaction(TEST_USER, request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(Transaction.TransactionType.BUY, response.getTransactionType());

        List<HoldingResponse> holdings = holdingService.getAllHoldingsForUser(TEST_USER);
        assertFalse(holdings.isEmpty());
    }

    @Test
    void testFIFOCalculation() {
        transactionService.recordTransaction(TEST_USER,
            createBuyTransaction("FIFO", new BigDecimal("100"), new BigDecimal("1000")));

        transactionService.recordTransaction(TEST_USER,
            createBuyTransaction("FIFO", new BigDecimal("50"), new BigDecimal("1200")));

        FIFOCalculationResult result = transactionService.calculateFIFOGain(
            TEST_USER, "FIFO", new BigDecimal("120"), new BigDecimal("1500")
        );

        assertNotNull(result);
        assertEquals(2, result.getBatches().size());

        assertEquals(new BigDecimal("100"), result.getBatches().get(0).getQuantitySold());
        assertEquals(new BigDecimal("1000"), result.getBatches().get(0).getPurchasePrice());

        assertEquals(new BigDecimal("20"), result.getBatches().get(1).getQuantitySold());
        assertEquals(new BigDecimal("1200"), result.getBatches().get(1).getPurchasePrice());

        assertEquals(0, result.getTotalRealizedGain().compareTo(new BigDecimal("56000")));
    }

    @Test
    void testSellTransaction() {
        transactionService.recordTransaction(TEST_USER,
            createBuyTransaction("SELL", new BigDecimal("100"), new BigDecimal("1000")));

        TransactionRequest sellRequest = new TransactionRequest(
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

        TransactionResponse response = transactionService.recordTransaction(TEST_USER, sellRequest);

        assertNotNull(response);

        List<HoldingResponse> holdings = holdingService.getAllHoldingsForUser(TEST_USER);
        HoldingResponse holding = holdings.stream()
            .filter(h -> h.getAssetSymbol().equals("SELL"))
            .findFirst()
            .orElse(null);

        assertNotNull(holding);
        assertEquals(0, new BigDecimal("50").compareTo(holding.getQuantity()));
    }

    private TransactionRequest createBuyTransaction(String symbol,
                                                    BigDecimal quantity, BigDecimal price) {
        return new TransactionRequest(
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
