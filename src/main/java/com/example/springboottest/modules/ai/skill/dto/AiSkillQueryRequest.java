package com.example.springboottest.modules.ai.skill.dto;

import lombok.Data;

@Data
public class AiSkillQueryRequest {

    private String keyword;
    private String sourceType;
    private String originType;
    private String skillType;
    private String category;
    private String scenario;
    private Boolean enabledOnly;
}
