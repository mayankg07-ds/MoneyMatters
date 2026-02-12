package com.moneymatters.portfolio.controller;

import com.moneymatters.common.dto.ApiResponse;
import com.moneymatters.portfolio.dto.PortfolioAnalyticsResponse;
import com.moneymatters.portfolio.service.PortfolioAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/portfolio/analytics")
@RequiredArgsConstructor
@Slf4j
public class PortfolioAnalyticsController {

    private final PortfolioAnalyticsService analyticsService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PortfolioAnalyticsResponse>> getPortfolioAnalytics(
            @PathVariable Long userId) {

        log.info("Generating analytics for user: {}", userId);

        PortfolioAnalyticsResponse analytics = analyticsService.getPortfolioAnalytics(userId);

        return ResponseEntity.ok(new ApiResponse<>(true, analytics,
            "Portfolio analytics generated successfully"));
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<ApiResponse<PortfolioAnalyticsResponse>> getAnalyticsForDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Generating analytics for user {} from {} to {}", userId, startDate, endDate);

        PortfolioAnalyticsResponse analytics = analyticsService
            .getPortfolioAnalyticsForDateRange(userId, startDate, endDate);

        return ResponseEntity.ok(new ApiResponse<>(true, analytics,
            "Portfolio analytics generated successfully"));
    }
}
