package com.moneymatters.portfolio.service;

import com.moneymatters.portfolio.dto.PortfolioAnalyticsResponse;
import java.time.LocalDate;

public interface PortfolioAnalyticsService {
    
    PortfolioAnalyticsResponse getPortfolioAnalytics(String userId);
    
    PortfolioAnalyticsResponse getPortfolioAnalyticsForDateRange(
        String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Clear analytics cache when portfolio is updated
     */
    void clearAnalyticsCache(String userId);
}
