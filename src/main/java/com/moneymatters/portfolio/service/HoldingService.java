package com.moneymatters.portfolio.service;

import com.moneymatters.portfolio.dto.HoldingRequest;
import com.moneymatters.portfolio.dto.HoldingResponse;
import com.moneymatters.portfolio.dto.PortfolioSummaryResponse;

import java.util.List;

public interface HoldingService {
    
    HoldingResponse createHolding(HoldingRequest request);
    
    HoldingResponse updateHolding(Long id, HoldingRequest request);
    
    void deleteHolding(Long id);
    
    HoldingResponse getHoldingById(Long id);
    
    List<HoldingResponse> getAllHoldingsForUser(Long userId);
    
    PortfolioSummaryResponse getPortfolioSummary(Long userId);
    
    void refreshHoldingPrice(Long holdingId);
    
    void refreshAllHoldingPrices(Long userId);
}
