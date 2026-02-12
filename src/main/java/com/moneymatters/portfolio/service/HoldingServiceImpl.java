package com.moneymatters.portfolio.service;

import com.moneymatters.portfolio.dto.HoldingRequest;
import com.moneymatters.portfolio.dto.HoldingResponse;
import com.moneymatters.portfolio.dto.PortfolioSummaryResponse;
import com.moneymatters.portfolio.entity.Holding;
import com.moneymatters.portfolio.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingServiceImpl implements HoldingService {

    private final HoldingRepository holdingRepository;
    private final StockPriceService stockPriceService;

    @Override
    @Transactional
    public HoldingResponse createHolding(HoldingRequest request) {
        log.info("Creating holding for user {}: {}", request.getUserId(), request.getAssetSymbol());

        // Check if holding already exists
        if (holdingRepository.existsByUserIdAndAssetSymbol(request.getUserId(), request.getAssetSymbol())) {
            throw new RuntimeException("Holding already exists for this symbol. Use update instead.");
        }

        // Create holding entity
        Holding holding = new Holding();
        holding.setUserId(request.getUserId());
        holding.setAssetType(request.getAssetType());
        holding.setAssetName(request.getAssetName());
        holding.setAssetSymbol(request.getAssetSymbol());
        holding.setExchange(request.getExchange());
        holding.setQuantity(request.getQuantity());
        holding.setAvgBuyPrice(request.getAvgBuyPrice());
        
        // Calculate total invested
        BigDecimal totalInvested = request.getQuantity()
            .multiply(request.getAvgBuyPrice())
            .setScale(2, RoundingMode.HALF_UP);
        holding.setTotalInvested(totalInvested);
        
        holding.setPurchaseDate(request.getPurchaseDate() != null ? 
            request.getPurchaseDate() : LocalDate.now());

        // Fetch current price
        String yahooSymbol = stockPriceService.toYahooSymbol(
            request.getAssetSymbol(), 
            request.getExchange()
        );
        
        BigDecimal currentPrice = stockPriceService.getCurrentPrice(yahooSymbol);
        if (currentPrice == null) {
            // If price not available, use buy price as current price
            currentPrice = request.getAvgBuyPrice();
            log.warn("Could not fetch current price for {}. Using buy price.", request.getAssetSymbol());
        }

        calculateHoldingValues(holding, currentPrice);

        Holding saved = holdingRepository.save(holding);
        log.info("Holding created with ID: {}", saved.getId());

        return HoldingResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public HoldingResponse updateHolding(Long id, HoldingRequest request) {
        log.info("Updating holding ID: {}", id);

        Holding holding = holdingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Holding not found with ID: " + id));

        // Update fields
        holding.setQuantity(request.getQuantity());
        holding.setAvgBuyPrice(request.getAvgBuyPrice());
        
        BigDecimal totalInvested = request.getQuantity()
            .multiply(request.getAvgBuyPrice())
            .setScale(2, RoundingMode.HALF_UP);
        holding.setTotalInvested(totalInvested);

        // Recalculate with current price
        calculateHoldingValues(holding, holding.getCurrentPrice());

        Holding updated = holdingRepository.save(holding);
        log.info("Holding updated: {}", id);

        return HoldingResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteHolding(Long id) {
        log.info("Deleting holding ID: {}", id);

        if (!holdingRepository.existsById(id)) {
            throw new RuntimeException("Holding not found with ID: " + id);
        }

        holdingRepository.deleteById(id);
        log.info("Holding deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public HoldingResponse getHoldingById(Long id) {
        Holding holding = holdingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Holding not found with ID: " + id));

        return HoldingResponse.fromEntity(holding);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoldingResponse> getAllHoldingsForUser(Long userId) {
        log.info("Fetching all holdings for user: {}", userId);

        List<Holding> holdings = holdingRepository.findByUserId(userId);

        return holdings.stream()
            .map(HoldingResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getPortfolioSummary(Long userId) {
        log.info("Generating portfolio summary for user: {}", userId);

        List<Holding> holdings = holdingRepository.findActiveHoldingsByUserId(userId);

        if (holdings.isEmpty()) {
            return new PortfolioSummaryResponse(
                userId,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                new ArrayList<>(),
                new ArrayList<>()
            );
        }

        // Calculate totals
        BigDecimal totalInvested = holdings.stream()
            .map(Holding::getTotalInvested)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCurrentValue = holdings.stream()
            .map(Holding::getCurrentValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnrealizedGain = totalCurrentValue.subtract(totalInvested);

        BigDecimal totalUnrealizedGainPercent = BigDecimal.ZERO;
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            totalUnrealizedGainPercent = totalUnrealizedGain
                .multiply(new BigDecimal(100))
                .divide(totalInvested, 4, RoundingMode.HALF_UP);
        }

        // Asset type breakdown
        Map<Holding.AssetType, List<Holding>> holdingsByType = holdings.stream()
            .collect(Collectors.groupingBy(Holding::getAssetType));

        List<PortfolioSummaryResponse.AssetTypeBreakdown> assetTypeBreakdown = new ArrayList<>();

        for (Map.Entry<Holding.AssetType, List<Holding>> entry : holdingsByType.entrySet()) {
            Holding.AssetType assetType = entry.getKey();
            List<Holding> typeHoldings = entry.getValue();

            BigDecimal typeInvested = typeHoldings.stream()
                .map(Holding::getTotalInvested)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal typeCurrentValue = typeHoldings.stream()
                .map(Holding::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal allocation = BigDecimal.ZERO;
            if (totalCurrentValue.compareTo(BigDecimal.ZERO) > 0) {
                allocation = typeCurrentValue
                    .multiply(new BigDecimal(100))
                    .divide(totalCurrentValue, 2, RoundingMode.HALF_UP);
            }

            assetTypeBreakdown.add(new PortfolioSummaryResponse.AssetTypeBreakdown(
                assetType.name(),
                typeInvested,
                typeCurrentValue,
                allocation,
                typeHoldings.size()
            ));
        }

        List<HoldingResponse> holdingResponses = holdings.stream()
            .map(HoldingResponse::fromEntity)
            .collect(Collectors.toList());

        return new PortfolioSummaryResponse(
            userId,
            totalInvested,
            totalCurrentValue,
            totalUnrealizedGain,
            totalUnrealizedGainPercent,
            holdings.size(),
            holdingResponses,
            assetTypeBreakdown
        );
    }

    @Override
    @Transactional
    public void refreshHoldingPrice(Long holdingId) {
        log.info("Refreshing price for holding: {}", holdingId);

        Holding holding = holdingRepository.findById(holdingId)
            .orElseThrow(() -> new RuntimeException("Holding not found with ID: " + holdingId));

        String yahooSymbol = stockPriceService.toYahooSymbol(
            holding.getAssetSymbol(), 
            holding.getExchange()
        );

        BigDecimal currentPrice = stockPriceService.getCurrentPrice(yahooSymbol);

        if (currentPrice != null) {
            calculateHoldingValues(holding, currentPrice);
            holdingRepository.save(holding);
            log.info("Price refreshed for holding {}: {}", holdingId, currentPrice);
        } else {
            log.warn("Could not refresh price for holding: {}", holdingId);
        }
    }

    @Override
    @Transactional
    public void refreshAllHoldingPrices(Long userId) {
        log.info("Refreshing all holding prices for user: {}", userId);

        List<Holding> holdings = holdingRepository.findActiveHoldingsByUserId(userId);

        if (holdings.isEmpty()) {
            log.info("No holdings to refresh for user: {}", userId);
            return;
        }

        // Get all Yahoo symbols
        List<String> yahooSymbols = holdings.stream()
            .map(h -> stockPriceService.toYahooSymbol(h.getAssetSymbol(), h.getExchange()))
            .collect(Collectors.toList());

        // Fetch all prices in one call
        Map<String, BigDecimal> prices = stockPriceService.getCurrentPrices(yahooSymbols);

        // Update each holding
        for (Holding holding : holdings) {
            String yahooSymbol = stockPriceService.toYahooSymbol(
                holding.getAssetSymbol(), 
                holding.getExchange()
            );

            BigDecimal currentPrice = prices.get(yahooSymbol);
            if (currentPrice != null) {
                calculateHoldingValues(holding, currentPrice);
            }
        }

        holdingRepository.saveAll(holdings);
        log.info("Refreshed prices for {} holdings", holdings.size());
    }

    /**
     * Helper method to calculate holding values
     */
    private void calculateHoldingValues(Holding holding, BigDecimal currentPrice) {
        holding.setCurrentPrice(currentPrice);

        BigDecimal currentValue = currentPrice
            .multiply(holding.getQuantity())
            .setScale(2, RoundingMode.HALF_UP);
        holding.setCurrentValue(currentValue);

        BigDecimal unrealizedGain = currentValue
            .subtract(holding.getTotalInvested())
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
