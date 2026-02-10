package com.moneymatters.portfolio.controller;

import com.moneymatters.common.dto.ApiResponse;
import com.moneymatters.portfolio.service.PriceUpdateService;
import com.moneymatters.portfolio.service.StockPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/v1/portfolio/prices")
@RequiredArgsConstructor
@Slf4j
public class StockPriceController {

    private final StockPriceService stockPriceService;
    private final PriceUpdateService priceUpdateService;

    @GetMapping("/current/{symbol}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentPrice(
            @PathVariable String symbol,
            @RequestParam(required = false, defaultValue = "NSE") String exchange) {

        String yahooSymbol = stockPriceService.toYahooSymbol(symbol, exchange);
        BigDecimal price = stockPriceService.getCurrentPrice(yahooSymbol);

        if (price == null) {
            return ResponseEntity.ok(new ApiResponse<>(false, null, 
                "Price not found for symbol: " + symbol));
        }

        return ResponseEntity.ok(new ApiResponse<>(true, 
            Map.of("symbol", symbol, "price", price), 
            "Price fetched successfully"));
    }

    @GetMapping("/details/{symbol}")
    public ResponseEntity<ApiResponse<StockPriceService.StockDetails>> getStockDetails(
            @PathVariable String symbol,
            @RequestParam(required = false, defaultValue = "NSE") String exchange) {

        String yahooSymbol = stockPriceService.toYahooSymbol(symbol, exchange);
        StockPriceService.StockDetails details = stockPriceService.getStockDetails(yahooSymbol);

        if (details == null) {
            return ResponseEntity.ok(new ApiResponse<>(false, null, 
                "Details not found for symbol: " + symbol));
        }

        return ResponseEntity.ok(new ApiResponse<>(true, details, 
            "Stock details fetched successfully"));
    }

    @PostMapping("/update/holding/{holdingId}")
    public ResponseEntity<ApiResponse<String>> updateHoldingPrice(
            @PathVariable Long holdingId) {

        priceUpdateService.updateHoldingPrice(holdingId);

        return ResponseEntity.ok(new ApiResponse<>(true, null, 
            "Holding price updated successfully"));
    }

    @PostMapping("/update/user/{userId}")
    public ResponseEntity<ApiResponse<String>> updateAllHoldingsForUser(
            @PathVariable Long userId) {

        priceUpdateService.updateAllHoldingsForUser(userId);

        return ResponseEntity.ok(new ApiResponse<>(true, null, 
            "All holdings updated successfully for user"));
    }
}
