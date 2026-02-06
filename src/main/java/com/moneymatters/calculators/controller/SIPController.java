package com.moneymatters.calculators.controller;

import com.moneymatters.calculators.dto.SIPStepupRequest;
import com.moneymatters.calculators.dto.SIPStepupResponse;
import com.moneymatters.calculators.service.SIPCalculatorService;
import com.moneymatters.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/calculators/sip-stepup")
@RequiredArgsConstructor
@Slf4j
public class SIPController {

    private final SIPCalculatorService sipCalculatorService;

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<SIPStepupResponse>> calculateStepup(
            @Valid @RequestBody SIPStepupRequest request) {

        log.info("Received SIP Step-up calculation request: {}", request);

        SIPStepupResponse response = sipCalculatorService.calculateStepupSIP(request);

        ApiResponse<SIPStepupResponse> apiResponse =
                new ApiResponse<>(true, response, "SIP Step-up calculation successful");

        return ResponseEntity.ok(apiResponse);
    }
}
