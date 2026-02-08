package com.moneymatters.calculators.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class LoanComparisonResponse {
    private List<LoanAnalysisResponse> loanAnalyses;
    private String bestOption;  // Based on total interest
    private String recommendation;
}
