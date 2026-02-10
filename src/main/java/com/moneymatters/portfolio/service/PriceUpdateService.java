package com.moneymatters.portfolio.service;

import com.moneymatters.portfolio.entity.Holding;
import com.moneymatters.portfolio.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceUpdateService {

    private final HoldingRepository holdingRepository;
    private final StockPriceService stockPriceService;

    /**
     * Update prices for a single holding
     */
    @Transactional
    public void updateHoldingPrice(Long holdingId) {
        Holding holding = holdingRepository.findById(holdingId)
            .orElseThrow(() -> new RuntimeException("Holding not found"));
        
        String yahooSymbol = stockPriceService.toYahooSymbol(
            holding.getAssetSymbol(), 
            holding.getExchange()
        );
        
        BigDecimal currentPrice = stockPriceService.getCurrentPrice(yahooSymbol);
        
        if (currentPrice != null) {
            updateHoldingWithNewPrice(holding, currentPrice);
            holdingRepository.save(holding);
            log.info("Updated price for {}: {}", holding.getAssetSymbol(), currentPrice);
        }
    }

    /**
     * Update prices for all holdings of a user
     */
    @Transactional
    public void updateAllHoldingsForUser(Long userId) {
        List<Holding> holdings = holdingRepository.findActiveHoldingsByUserId(userId);
        
        if (holdings.isEmpty()) {
            log.info("No active holdings found for user {}", userId);
            return;
        }
        
        // Get all symbols
        List<String> yahooSymbols = holdings.stream()
            .map(h -> stockPriceService.toYahooSymbol(h.getAssetSymbol(), h.getExchange()))
            .collect(Collectors.toList());
        
        // Fetch all prices in one API call (efficient!)
        Map<String, BigDecimal> prices = stockPriceService.getCurrentPrices(yahooSymbols);
        
        // Update each holding
        for (Holding holding : holdings) {
            String yahooSymbol = stockPriceService.toYahooSymbol(
                holding.getAssetSymbol(), 
                holding.getExchange()
            );
            
            BigDecimal currentPrice = prices.get(yahooSymbol);
            if (currentPrice != null) {
                updateHoldingWithNewPrice(holding, currentPrice);
            }
        }
        
        holdingRepository.saveAll(holdings);
        log.info("Updated prices for {} holdings of user {}", holdings.size(), userId);
    }

    /**
     * Scheduled job: Update all holdings every 15 minutes during market hours
     * (9:15 AM to 3:30 PM IST on weekdays)
     */
    @Scheduled(cron = "0 */15 9-15 * * MON-FRI", zone = "Asia/Kolkata")
    @Transactional
    public void scheduledPriceUpdate() {
        log.info("Starting scheduled price update for all holdings");
        
        List<Holding> allHoldings = holdingRepository.findAll();
        
        if (allHoldings.isEmpty()) {
            log.info("No holdings to update");
            return;
        }
        
        // Group by user and update
        Map<Long, List<Holding>> holdingsByUser = allHoldings.stream()
            .collect(Collectors.groupingBy(Holding::getUserId));
        
        for (Long userId : holdingsByUser.keySet()) {
            try {
                updateAllHoldingsForUser(userId);
            } catch (Exception e) {
                log.error("Error updating holdings for user {}: {}", userId, e.getMessage());
            }
        }
        
        log.info("Completed scheduled price update");
    }

    /**
     * Helper: Update holding calculations with new price
     */
    private void updateHoldingWithNewPrice(Holding holding, BigDecimal currentPrice) {
        holding.setCurrentPrice(currentPrice);
        
        BigDecimal currentValue = currentPrice.multiply(holding.getQuantity())
            .setScale(2, RoundingMode.HALF_UP);
        holding.setCurrentValue(currentValue);
        
        BigDecimal unrealizedGain = currentValue.subtract(holding.getTotalInvested())
            .setScale(2, RoundingMode.HALF_UP);
        holding.setUnrealizedGain(unrealizedGain);
        
        BigDecimal unrealizedGainPercent = BigDecimal.ZERO;
        if (holding.getTotalInvested().compareTo(BigDecimal.ZERO) > 0) {
            unrealizedGainPercent = unrealizedGain
                .multiply(new BigDecimal(100))
                .divide(holding.getTotalInvested(), 4, RoundingMode.HALF_UP);
        }
        holding.setUnrealizedGainPercent(unrealizedGainPercent);
        
        holding.setLastUpdated(LocalDateTime.now());
    }
}
