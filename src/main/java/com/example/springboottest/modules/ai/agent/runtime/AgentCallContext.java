package com.example.springboottest.modules.ai.agent.runtime;

import com.example.springboottest.modules.ai.agent.message.AgentMessage;
import com.example.springboottest.modules.ai.agent.model.ResolvedAgentModel;
import com.example.springboottest.modules.ai.skill.entity.AiSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCallContext {

    @Builder.Default
    private String runId = UUID.randomUUID().toString();

    private String threadId;
    private String agentName;
    private ResolvedAgentModel model;

    @Builder.Default
    private int maxTurns = 12;

    @Builder.Default
    private String permissionMode = "DEFAULT";

    @Builder.Default
    private List<AgentMessage> history = List.of();

    @Builder.Default
    private List<AiSkill> mountedSkills = List.of();

    private String mountedSkillPrompt;

    @Builder.Default
    private Map<String, Object> metadata = Map.of();
}
