package com.moneymatters.calculators.controller;

import com.moneymatters.calculators.dto.SWPRequest;
import com.moneymatters.calculators.dto.SWPResponse;
import com.moneymatters.calculators.service.SWPCalculatorService;
import com.moneymatters.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/calculators/swp")
@RequiredArgsConstructor
@Slf4j
public class SWPController {

    private final SWPCalculatorService swpCalculatorService;

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<SWPResponse>> calculateSWP(
            @Valid @RequestBody SWPRequest request) {

        log.info("Calculating SWP: Corpus={}, Withdrawal={}, Duration={} years, Inflation={}",
            request.getStartingCorpus(),
            request.getMonthlyWithdrawal(),
            request.getDurationYears(),
            request.getInflationAdjusted() ? "Yes" : "No");

        SWPResponse response = swpCalculatorService.calculateSWP(request);

        ApiResponse<SWPResponse> apiResponse =
            new ApiResponse<>(true, response, "SWP calculation completed successfully");

        return ResponseEntity.ok(apiResponse);
    }
}

