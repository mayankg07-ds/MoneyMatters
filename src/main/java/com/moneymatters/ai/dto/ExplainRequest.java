package com.moneymatters.ai.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ExplainRequest {
    private String type;
    private Map<String, Object> inputs;
    private Map<String, Object> result;
}
