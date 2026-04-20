package com.moneymatters.ai.dto;

import lombok.Data;

@Data
public class FollowupRequest {
    private String context;
    private String question;
    private String topic;
}
