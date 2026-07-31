package com.example.springboottest.modules.ai.skill.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SkillTestRequest {

    private String conversationId;
    private Map<String, Object> arguments;
}
