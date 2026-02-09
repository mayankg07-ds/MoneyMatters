package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.*;
import com.moneymatters.calculators.util.CalculationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetAllocationServiceImpl implements AssetAllocationService {

   /*  @Override
    public AssetAllocationResponse calculateRebalancing(AssetAllocationRequest request) {
        // Implementation in Day 30
        return null;
    }*/
   @Override
public AssetAllocationResponse calculateRebalancing(AssetAllocationRequest request) {
    log.info("Calculating asset allocation rebalancing");

    // Calculate total portfolio value
    BigDecimal totalValue = request.getCurrentHoldings().stream()
        .map(AssetAllocationRequest.AssetClass::getCurrentValue)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Add fresh investment if any
    if (request.getFreshInvestment() != null) {
        totalValue = totalValue.add(request.getFreshInvestment());
    }

    // Analyze each asset
    List<AssetAllocationResponse.AssetAnalysis> analyses = new ArrayList<>();
    List<AssetAllocationResponse.RebalancingAction> actions = new ArrayList<>();

    for (AssetAllocationRequest.TargetAllocation target : request.getTargetAllocations()) {
        String assetName = target.getAssetName();
        BigDecimal targetPercentage = target.getTargetPercentage();

        // Find current holding
        BigDecimal currentValue = request.getCurrentHoldings().stream()
            .filter(h -> h.getAssetName().equals(assetName))
            .map(AssetAllocationRequest.AssetClass::getCurrentValue)
            .findFirst()
            .orElse(BigDecimal.ZERO);

        // Calculate current %
        BigDecimal currentPercentage = CalculationUtils.safeDivide(
            currentValue.multiply(new BigDecimal(100)),
            totalValue
        );

        // Calculate drift
        BigDecimal drift = currentPercentage.subtract(targetPercentage);

        // Calculate target value
        BigDecimal targetValue = totalValue
            .multiply(targetPercentage)
            .divide(new BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);

        // Calculate adjustment needed
        BigDecimal adjustmentNeeded = targetValue.subtract(currentValue);

        analyses.add(new AssetAllocationResponse.AssetAnalysis(
            assetName,
            CalculationUtils.format(currentValue),
            CalculationUtils.format(currentPercentage),
            CalculationUtils.format(targetPercentage),
            CalculationUtils.format(drift),
            CalculationUtils.format(targetValue),
            CalculationUtils.format(adjustmentNeeded)
        ));

        // Generate action
        String action;
        String recommendation;
        BigDecimal actionAmount = adjustmentNeeded.abs();

        if (adjustmentNeeded.compareTo(new BigDecimal("100")) > 0) {
            action = "BUY";
            recommendation = String.format("Invest ₹%s more in %s", 
                CalculationUtils.format(actionAmount), assetName);
        } else if (adjustmentNeeded.compareTo(new BigDecimal("-100")) < 0) {
            action = "SELL";
            recommendation = String.format("Redeem ₹%s from %s", 
                CalculationUtils.format(actionAmount), assetName);
        } else {
            action = "HOLD";
            recommendation = String.format("%s is balanced", assetName);
            actionAmount = BigDecimal.ZERO;
        }

        actions.add(new AssetAllocationResponse.RebalancingAction(
            assetName,
            action,
            CalculationUtils.format(actionAmount),
            recommendation
        ));
    }

    // Calculate totals
    BigDecimal totalBuy = actions.stream()
        .filter(a -> a.getAction().equals("BUY"))
        .map(AssetAllocationResponse.RebalancingAction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalSell = actions.stream()
        .filter(a -> a.getAction().equals("SELL"))
        .map(AssetAllocationResponse.RebalancingAction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Check if balanced (all drifts < 1%)
    boolean isBalanced = analyses.stream()
        .allMatch(a -> a.getDrift().abs().compareTo(new BigDecimal("1")) < 0);

    // Generate chart
    List<ChartPoint> chart = analyses.stream()
        .map(a -> new ChartPoint(a.getAssetName(), a.getCurrentPercentage()))
        .toList();

    return new AssetAllocationResponse(
        CalculationUtils.format(totalValue),
        analyses,
        actions,
        CalculationUtils.format(totalBuy),
        CalculationUtils.format(totalSell),
        isBalanced,
        chart
    );
}

}
