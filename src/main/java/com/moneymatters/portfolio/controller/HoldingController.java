
package com.moneymatters.portfolio.controller;

import com.moneymatters.common.dto.ApiResponse;
import com.moneymatters.portfolio.dto.HoldingRequest;
import com.moneymatters.portfolio.dto.HoldingResponse;
import com.moneymatters.portfolio.dto.PortfolioSummaryResponse;
import com.moneymatters.portfolio.service.HoldingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/portfolio/holdings")
@RequiredArgsConstructor
@Slf4j
public class HoldingController {

    private final HoldingService holdingService;

    @PostMapping
    public ResponseEntity<ApiResponse<HoldingResponse>> createHolding(
            @Valid @RequestBody HoldingRequest request) {

        log.info("Creating holding for user {}", request.getUserId());

        HoldingResponse response = holdingService.createHolding(request);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, response, "Holding created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HoldingResponse>> updateHolding(
            @PathVariable Long id,
            @Valid @RequestBody HoldingRequest request) {

        log.info("Updating holding {}", id);

        HoldingResponse response = holdingService.updateHolding(id, request);

        return ResponseEntity.ok(new ApiResponse<>(true, response, "Holding updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHolding(@PathVariable Long id) {

        log.info("Deleting holding {}", id);

        holdingService.deleteHolding(id);

        return ResponseEntity.ok(new ApiResponse<>(true, null, "Holding deleted successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HoldingResponse>> getHolding(@PathVariable Long id) {

        HoldingResponse response = holdingService.getHoldingById(id);

        return ResponseEntity.ok(new ApiResponse<>(true, response, "Holding retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<HoldingResponse>>> getUserHoldings(
            @PathVariable Long userId) {

        log.info("Fetching holdings for user {}", userId);

        List<HoldingResponse> holdings = holdingService.getAllHoldingsForUser(userId);

        return ResponseEntity.ok(new ApiResponse<>(true, holdings, 
            holdings.size() + " holdings found"));
    }

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getPortfolioSummary(
            @PathVariable Long userId) {

        log.info("Generating portfolio summary for user {}", userId);

        PortfolioSummaryResponse summary = holdingService.getPortfolioSummary(userId);

        return ResponseEntity.ok(new ApiResponse<>(true, summary, 
            "Portfolio summary generated successfully"));
    }

    @PostMapping("/{id}/refresh-price")
    public ResponseEntity<ApiResponse<Void>> refreshHoldingPrice(@PathVariable Long id) {

        log.info("Refreshing price for holding {}", id);

        holdingService.refreshHoldingPrice(id);

        return ResponseEntity.ok(new ApiResponse<>(true, null, "Price refreshed successfully"));
    }

    @PostMapping("/user/{userId}/refresh-prices")
    public ResponseEntity<ApiResponse<Void>> refreshAllHoldingPrices(@PathVariable Long userId) {

        log.info("Refreshing all prices for user {}", userId);

        holdingService.refreshAllHoldingPrices(userId);

        return ResponseEntity.ok(new ApiResponse<>(true, null, 
            "All prices refreshed successfully"));
    }
}
