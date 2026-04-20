package com.moneymatters.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExplainResponse {
    private String explanation;
    private int remainingRequests;
}
