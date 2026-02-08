package com.moneymatters.calculators.controller;

import com.moneymatters.calculators.dto.LoanAnalysisRequest;
import com.moneymatters.calculators.dto.LoanAnalysisResponse;
import com.moneymatters.calculators.service.LoanAnalyzerService;
import com.moneymatters.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/calculators/loan")
@RequiredArgsConstructor
@Slf4j
public class LoanController {

    private final LoanAnalyzerService loanAnalyzerService;

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<LoanAnalysisResponse>> analyzeLoan(
            @Valid @RequestBody LoanAnalysisRequest request) {

        log.info("Received loan analysis request: Principal={}, Rate={}, Tenure={}",
            request.getPrincipal(), 
            request.getAnnualInterestRatePercent(), 
            request.getTenureMonths());

        LoanAnalysisResponse response = loanAnalyzerService.analyzeLoan(request);

        ApiResponse<LoanAnalysisResponse> apiResponse =
            new ApiResponse<>(true, response, "Loan analysis completed successfully");

        return ResponseEntity.ok(apiResponse);
    }
}
