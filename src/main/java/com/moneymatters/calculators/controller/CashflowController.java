package com.moneymatters.calculators.controller;

import com.moneymatters.calculators.dto.CashflowRequest;
import com.moneymatters.calculators.dto.CashflowResponse;
import com.moneymatters.calculators.service.CashflowPlannerService;
import com.moneymatters.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/calculators/cashflow")
@RequiredArgsConstructor
@Slf4j
public class CashflowController {

    private final CashflowPlannerService cashflowPlannerService;

    @PostMapping("/project")
    public ResponseEntity<ApiResponse<CashflowResponse>> projectCashflow(
            @Valid @RequestBody CashflowRequest request) {

        log.info("Projecting cashflow with {} income sources and {} expenses for {} years",
            request.getIncomes().size(),
            request.getExpenses().size(),
            request.getProjectionYears());

        CashflowResponse response = cashflowPlannerService.projectCashflow(request);

        ApiResponse<CashflowResponse> apiResponse =
            new ApiResponse<>(true, response, "Cashflow projection completed successfully");

        return ResponseEntity.ok(apiResponse);
    }
}

