package com.example.springboottest.modules.ai.agent.tool;

import com.example.springboottest.modules.ai.skill.dto.SkillTestResponse;
import com.example.springboottest.modules.ai.skill.entity.AiSkill;
import com.example.springboottest.modules.ai.skill.service.SkillScriptExecutor;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;

public class SkillAgentToolExecutor implements AgentToolExecutor {

    private final AiSkill skill;
    private final String conversationId;
    private final SkillScriptExecutor scriptExecutor;

    public SkillAgentToolExecutor(AiSkill skill, String conversationId, SkillScriptExecutor scriptExecutor) {
        this.skill = skill;
        this.conversationId = conversationId;
        this.scriptExecutor = scriptExecutor;
    }

    @Override
    public AgentToolDefinition definition() {
        return AgentToolDefinition.builder()
                .id("skill_" + skill.getSkillKey())
                .name("skill_" + skill.getSkillKey())
                .description(defaultIfBlank(skill.getDescription(), skill.getName()))
                .parametersSchema(parseSchema(skill.getParameterSchema()))
                .readOnly(true)
                .approvalRequired(false)
                .timeout(Duration.ofSeconds(30))
                .resultSummaryStrategy("plain_text")
                .build();
    }

    @Override
    public AgentToolResult execute(Map<String, Object> arguments) {
        SkillTestResponse response = scriptExecutor.execute(skill, conversationId, arguments);
        if (Boolean.TRUE.equals(response.getSuccess())) {
            return AgentToolResult.success(response.getOutput());
        }
        return AgentToolResult.error(response.getError());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseSchema(String schema) {
        if (!StringUtils.hasText(schema)) {
            return Map.of("type", "object", "properties", Map.of());
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(schema, Map.class);
        } catch (Exception e) {
            return Map.of("type", "object", "properties", Map.of());
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
