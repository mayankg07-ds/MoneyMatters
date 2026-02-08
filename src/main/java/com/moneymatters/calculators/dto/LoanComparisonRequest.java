package com.moneymatters.calculators.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanComparisonRequest {

    @NotEmpty(message = "At least 2 loan options required")
    @Size(min = 2, max = 5, message = "Compare 2-5 loans at a time")
    @Valid
    private List<LoanAnalysisRequest> loanOptions;
}
