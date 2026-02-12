package com.moneymatters.portfolio.service;

import com.moneymatters.portfolio.dto.PortfolioAnalyticsResponse;
import java.time.LocalDate;

public interface PortfolioAnalyticsService {
    
    PortfolioAnalyticsResponse getPortfolioAnalytics(Long userId);
    
    PortfolioAnalyticsResponse getPortfolioAnalyticsForDateRange(
        Long userId, LocalDate startDate, LocalDate endDate);
}
