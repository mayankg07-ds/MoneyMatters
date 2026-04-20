package com.moneymatters.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nvidia")
public class NvidiaProperties {
    private String apiKey;
    private String apiUrl;
    private String model;
    private int maxTokens = 1500;
    private double temperature = 0.6;
}
