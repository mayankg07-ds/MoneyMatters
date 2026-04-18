package com.moneymatters.portfolio.controller;

import com.moneymatters.common.dto.ApiResponse;
import com.moneymatters.portfolio.dto.HoldingRequest;
import com.moneymatters.portfolio.dto.HoldingResponse;
import com.moneymatters.portfolio.dto.PortfolioSummaryResponse;
import com.moneymatters.portfolio.service.HoldingService;
import com.moneymatters.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/portfolio/holdings")
@RequiredArgsConstructor
@Slf4j
public class HoldingController {

    private final HoldingService holdingService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<HoldingResponse>> createHolding(
            @Valid @RequestBody HoldingRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String clerkUserId = jwt.getSubject();
        userService.ensureUserExists(clerkUserId, jwt.getClaimAsString("email"));
        log.info("Creating holding for user {}", clerkUserId);

        HoldingResponse response = holdingService.createHolding(clerkUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, response, "Holding created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HoldingResponse>> updateHolding(
            @PathVariable Long id,
            @Valid @RequestBody HoldingRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        userService.ensureUserExists(jwt.getSubject(), jwt.getClaimAsString("email"));
        log.info("Updating holding {}", id);

        HoldingResponse response = holdingService.updateHolding(id, request);

        return ResponseEntity.ok(new ApiResponse<>(true, response, "Holding updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHolding(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        userService.ensureUserExists(jwt.getSubject(), jwt.getClaimAsString("email"));
        log.info("Deleting holding {}", id);

        holdingService.deleteHolding(id);

        return ResponseEntity.ok(new ApiResponse<>(true, null, "Holding deleted successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HoldingResponse>> getHolding(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        userService.ensureUserExists(jwt.getSubject(), jwt.getClaimAsString("email"));

        HoldingResponse response = holdingService.getHoldingById(id);

        return ResponseEntity.ok(new ApiResponse<>(true, response, "Holding retrieved successfully"));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<HoldingResponse>>> getUserHoldings(
            @AuthenticationPrincipal Jwt jwt) {

        String clerkUserId = jwt.getSubject();
        userService.ensureUserExists(clerkUserId, jwt.getClaimAsString("email"));
        log.info("Fetching holdings for user {}", clerkUserId);

        List<HoldingResponse> holdings = holdingService.getAllHoldingsForUser(clerkUserId);

        return ResponseEntity.ok(new ApiResponse<>(true, holdings,
            holdings.size() + " holdings found"));
    }

    @GetMapping("/user/summary")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getPortfolioSummary(
            @AuthenticationPrincipal Jwt jwt) {

        String clerkUserId = jwt.getSubject();
        userService.ensureUserExists(clerkUserId, jwt.getClaimAsString("email"));
        log.info("Generating portfolio summary for user {}", clerkUserId);

        PortfolioSummaryResponse summary = holdingService.getPortfolioSummary(clerkUserId);

        return ResponseEntity.ok(new ApiResponse<>(true, summary,
            "Portfolio summary generated successfully"));
    }

    @PostMapping("/{id}/refresh-price")
    public ResponseEntity<ApiResponse<Void>> refreshHoldingPrice(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        userService.ensureUserExists(jwt.getSubject(), jwt.getClaimAsString("email"));
        log.info("Refreshing price for holding {}", id);

        holdingService.refreshHoldingPrice(id);

        return ResponseEntity.ok(new ApiResponse<>(true, null, "Price refreshed successfully"));
    }

    @PostMapping("/user/refresh-prices")
    public ResponseEntity<ApiResponse<Void>> refreshAllHoldingPrices(
            @AuthenticationPrincipal Jwt jwt) {

        String clerkUserId = jwt.getSubject();
        userService.ensureUserExists(clerkUserId, jwt.getClaimAsString("email"));
        log.info("Refreshing all prices for user {}", clerkUserId);

        holdingService.refreshAllHoldingPrices(clerkUserId);

        return ResponseEntity.ok(new ApiResponse<>(true, null,
            "All prices refreshed successfully"));
    }
}
