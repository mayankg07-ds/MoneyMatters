package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.LoanAnalysisRequest;
import com.moneymatters.calculators.dto.LoanAnalysisResponse;

public interface LoanAnalyzerService {
    LoanAnalysisResponse analyzeLoan(LoanAnalysisRequest request);
}
