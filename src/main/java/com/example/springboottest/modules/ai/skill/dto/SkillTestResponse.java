package com.example.springboottest.modules.ai.skill.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SkillTestResponse {

    private Boolean success;
    private String output;
    private String error;
    private Long durationMs;
}
