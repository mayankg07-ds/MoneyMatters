package com.moneymatters.ai.service;

import com.moneymatters.portfolio.dto.PortfolioAnalyticsResponse;
import com.moneymatters.portfolio.entity.Holding;
import com.moneymatters.portfolio.repository.HoldingRepository;
import com.moneymatters.portfolio.service.PortfolioAnalyticsService;
import com.moneymatters.portfolio.service.StockPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiPortfolioAnalysisService {

    private final HoldingRepository holdingRepo;
    private final PortfolioAnalyticsService analyticsService;
    private final StockPriceService stockPrice;
    private final MarketFundamentalsService fundamentals;
    private final PromptBuilder prompts;
    private final NemotronService nemotron;

    @Cacheable(value = "aiPortfolioAnalysis", key = "#userId")
    public String analyse(String userId) {
        List<Holding> holdings = holdingRepo.findActiveHoldingsByClerkUserId(userId);
        if (holdings.isEmpty()) {
            return "Your portfolio is empty. Add some holdings first, then come back for an AI analysis.";
        }

        List<Map<String, Object>> enriched = new ArrayList<>();
        for (Holding h : holdings) {
            String yahoo = stockPrice.toYahooSymbol(h.getAssetSymbol(), h.getExchange());
            try {
                enriched.add(fundamentals.getFundamentals(yahoo));
            } catch (Exception e) {
                log.warn("Fundamentals fetch failed for {}: {}", yahoo, e.getMessage());
                enriched.add(Map.of());
            }
        }

        PortfolioAnalyticsResponse analytics = null;
        try {
            analytics = analyticsService.getPortfolioAnalytics(userId);
        } catch (Exception e) {
            log.warn("Analytics fetch failed for {}: {}", userId, e.getMessage());
        }
        String system = prompts.portfolioSystem();
        String user = prompts.buildPortfolioPrompt(holdings, enriched, analytics);
        return nemotron.chat(system, user);
    }
}
