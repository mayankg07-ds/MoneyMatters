package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.AssetAllocationRequest;
import com.moneymatters.calculators.dto.AssetAllocationResponse;

public interface AssetAllocationService {
    AssetAllocationResponse calculateRebalancing(AssetAllocationRequest request);
}
