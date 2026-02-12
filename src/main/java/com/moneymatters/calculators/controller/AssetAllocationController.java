package com.moneymatters.calculators.controller;

import com.moneymatters.calculators.dto.*;
import com.moneymatters.calculators.service.AssetAllocationService;
import com.moneymatters.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/calculators/asset-allocation")
@RequiredArgsConstructor
public class AssetAllocationController {

    private static final Logger log = LoggerFactory.getLogger(AssetAllocationController.class);

    private final AssetAllocationService assetAllocationService;

    @PostMapping("/rebalance")
    public ResponseEntity<ApiResponse<AssetAllocationResponse>> calculateRebalancing(
            @Valid @RequestBody AssetAllocationRequest request) {

        log.info("Rebalancing portfolio with {} assets", 
            request.getCurrentHoldings().size());

        AssetAllocationResponse response = 
            assetAllocationService.calculateRebalancing(request);

        ApiResponse<AssetAllocationResponse> apiResponse =
            new ApiResponse<>(true, response, "Rebalancing analysis completed");

        return ResponseEntity.ok(apiResponse);
    }
}
