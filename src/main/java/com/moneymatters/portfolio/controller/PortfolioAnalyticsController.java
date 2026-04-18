package com.moneymatters.portfolio.controller;

import com.moneymatters.common.dto.ApiResponse;
import com.moneymatters.portfolio.dto.PortfolioAnalyticsResponse;
import com.moneymatters.portfolio.service.PortfolioAnalyticsService;
import com.moneymatters.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/portfolio/analytics")
@RequiredArgsConstructor
@Slf4j
public class PortfolioAnalyticsController {

    private final PortfolioAnalyticsService analyticsService;
    private final UserService userService;

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<PortfolioAnalyticsResponse>> getPortfolioAnalytics(
            @AuthenticationPrincipal Jwt jwt) {

        String clerkUserId = jwt.getSubject();
        userService.ensureUserExists(clerkUserId, jwt.getClaimAsString("email"));
        log.info("Generating analytics for user: {}", clerkUserId);

        PortfolioAnalyticsResponse analytics = analyticsService.getPortfolioAnalytics(clerkUserId);

        return ResponseEntity.ok(new ApiResponse<>(true, analytics,
            "Portfolio analytics generated successfully"));
    }

    @GetMapping("/user/date-range")
    public ResponseEntity<ApiResponse<PortfolioAnalyticsResponse>> getAnalyticsForDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal Jwt jwt) {

        String clerkUserId = jwt.getSubject();
        userService.ensureUserExists(clerkUserId, jwt.getClaimAsString("email"));
        log.info("Generating analytics for user {} from {} to {}", clerkUserId, startDate, endDate);

        PortfolioAnalyticsResponse analytics = analyticsService
            .getPortfolioAnalyticsForDateRange(clerkUserId, startDate, endDate);

        return ResponseEntity.ok(new ApiResponse<>(true, analytics,
            "Portfolio analytics generated successfully"));
    }
}
