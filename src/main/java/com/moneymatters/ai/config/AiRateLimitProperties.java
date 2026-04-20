package com.moneymatters.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.rate-limit")
public class AiRateLimitProperties {
    private int maxRequestsPerHour = 10;
}
