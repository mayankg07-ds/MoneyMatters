package 
com.moneymatters.calculators.controller;

import com.moneymatters.calculators.dto.LoanAnalysisRequest;
import com.moneymatters.calculators.dto.LoanAnalysisResponse;
import com.moneymatters.calculators.dto.LoanComparisonRequest;
import com.moneymatters.calculators.dto.LoanComparisonResponse;
import com.moneymatters.calculators.service.LoanAnalyzerService;
import com.moneymatters.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping("/compare")
public ResponseEntity<ApiResponse<LoanComparisonResponse>> compareLoans(
        @Valid @RequestBody LoanComparisonRequest request) {

    log.info("Comparing {} loan options", request.getLoanOptions().size());

    List<LoanAnalysisResponse> analyses = request.getLoanOptions().stream()
        .map(loanAnalyzerService::analyzeLoan)
        .toList();

    // Find best option (lowest total interest)
    LoanAnalysisResponse bestLoan = analyses.stream()
        .min((a, b) -> a.getTotalInterestPayable().compareTo(b.getTotalInterestPayable()))
        .orElse(analyses.get(0));

    int bestIndex = analyses.indexOf(bestLoan);
    String bestOption = "Loan Option " + (bestIndex + 1);
    
    String recommendation = String.format(
        "%s has the lowest total interest of ₹%s",
        bestOption,
        bestLoan.getTotalInterestPayable()
    );

    LoanComparisonResponse response = new LoanComparisonResponse(
        analyses,
        bestOption,
        recommendation
    );

    ApiResponse<LoanComparisonResponse> apiResponse =
        new ApiResponse<>(true, response, "Loan comparison completed");

    return ResponseEntity.ok(apiResponse);
}

}
