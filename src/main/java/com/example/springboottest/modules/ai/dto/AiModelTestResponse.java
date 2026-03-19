package com.example.springboottest.modules.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiModelTestResponse {

    private Boolean success;

    private String message;

    private Long responseTime;
}
