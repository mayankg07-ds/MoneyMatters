package com.moneymatters.calculators.controller;

import com.moneymatters.calculators.dto.RetirementPlanRequest;
import com.moneymatters.calculators.dto.RetirementPlanResponse;
import com.moneymatters.calculators.service.RetirementPlannerService;
import com.moneymatters.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/calculators/retirement")
@RequiredArgsConstructor
@Slf4j
public class RetirementController {

    private final RetirementPlannerService retirementPlannerService;

    @PostMapping("/plan")
    public ResponseEntity<ApiResponse<RetirementPlanResponse>> calculatePlan(
            @Valid @RequestBody RetirementPlanRequest request) {

        log.info("Received retirement plan request for age {} retiring at {}",
            request.getCurrentAge(), request.getRetirementAge());

        RetirementPlanResponse response = 
            retirementPlannerService.calculateRetirementPlan(request);

        ApiResponse<RetirementPlanResponse> apiResponse =
            new ApiResponse<>(true, response, "Retirement plan calculated successfully");

        return ResponseEntity.ok(apiResponse);
    }
}

