package com.moneymatters.portfolio;

import com.moneymatters.portfolio.service.StockPriceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StockPriceServiceTest {

    @Autowired
    private StockPriceService stockPriceService;

    @Test
    void testGetCurrentPriceForRelianceNSE() {
        BigDecimal price = stockPriceService.getCurrentPrice("RELIANCE.NS");
        
        assertNotNull(price, "Price should not be null");
        assertTrue(price.compareTo(BigDecimal.ZERO) > 0, "Price should be positive");
        
        System.out.println("Reliance NSE Current Price: ₹" + price);
    }

    @Test
    void testGetCurrentPriceForTCSBSE() {
        BigDecimal price = stockPriceService.getCurrentPrice("TCS.BO");
        
        assertNotNull(price, "Price should not be null");
        assertTrue(price.compareTo(BigDecimal.ZERO) > 0, "Price should be positive");
        
        System.out.println("TCS BSE Current Price: ₹" + price);
    }

    @Test
    void testGetMultiplePrices() {
        List<String> symbols = List.of("RELIANCE.NS", "TCS.BO", "INFY.NS", "HDFCBANK.NS");
        
        Map<String, BigDecimal> prices = stockPriceService.getCurrentPrices(symbols);
        
        assertFalse(prices.isEmpty(), "Should fetch at least one price");
        
        prices.forEach((symbol, price) -> 
            System.out.println(symbol + ": ₹" + price)
        );
    }

    @Test
    void testGetStockDetails() {
        StockPriceService.StockDetails details = 
            stockPriceService.getStockDetails("RELIANCE.NS");
        
        assertNotNull(details);
        assertNotNull(details.currentPrice());
        assertNotNull(details.name());
        
        System.out.println("Stock: " + details.name());
        System.out.println("Price: ₹" + details.currentPrice());
        System.out.println("Day High: ₹" + details.dayHigh());
        System.out.println("Day Low: ₹" + details.dayLow());
        System.out.println("Change: " + details.changePercent() + "%");
    }

    @Test
    void testToYahooSymbolConversion() {
        assertEquals("RELIANCE.NS", 
            stockPriceService.toYahooSymbol("RELIANCE", "NSE"));
        
        assertEquals("500325.BO", 
            stockPriceService.toYahooSymbol("500325", "BSE"));
        
        assertEquals("RELIANCE.NS", 
            stockPriceService.toYahooSymbol("RELIANCE.NS", "NSE"));
    }
}
