package com.example.springboottest.modules.ai.agent.service;

import com.example.springboottest.modules.ai.agent.entity.AgentRun;
import com.example.springboottest.modules.ai.agent.mapper.AgentRunMapper;
import com.example.springboottest.modules.ai.agent.runtime.AgentCallContext;
import com.example.springboottest.modules.ai.agent.runtime.AgentRuntimeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AgentRunService {

    private final AgentRunMapper runMapper;

    public AgentRun startRun(AgentCallContext context) {
        AgentRun existing = runMapper.findByRunId(context.getRunId());
        if (existing != null) {
            return existing;
        }
        AgentRun run = AgentRun.builder()
                .runId(context.getRunId())
                .conversationId(context.getThreadId())
                .parentRunId((String) context.getMetadata().get("parentRunId"))
                .agentName(context.getAgentName())
                .agentMode(defaultIfBlank((String) context.getMetadata().get("agentMode"), "DEFAULT"))
                .permissionMode(defaultIfBlank(context.getPermissionMode(), "DEFAULT"))
                .provider(context.getModel() == null ? null : context.getModel().provider())
                .model(context.getModel() == null ? null : context.getModel().modelName())
                .status(AgentRuntimeStatus.RUNNING)
                .maxTurns(context.getMaxTurns())
                .currentTurn(0)
                .resumeFromRunId((String) context.getMetadata().get("resumeFromRunId"))
                .startedAt(LocalDateTime.now())
                .build();
        runMapper.insert(run);
        return run;
    }

    public AgentRun getRun(String runId) {
        return runMapper.findByRunId(runId);
    }

    public void updateTurn(String runId, int turn) {
        runMapper.updateTurn(runId, turn);
    }

    public void complete(String runId, String summary) {
        runMapper.updateStatus(runId, AgentRuntimeStatus.COMPLETED, summary, null);
    }

    public void fail(String runId, String summary, String errorMessage) {
        runMapper.updateStatus(runId, AgentRuntimeStatus.FAILED, summary, errorMessage);
    }

    public void waitingPermission(String runId, String summary) {
        runMapper.updateStatus(runId, AgentRuntimeStatus.WAITING_PERMISSION, summary, null);
    }

    public void cancel(String runId) {
        runMapper.updateStatus(runId, AgentRuntimeStatus.CANCELLED, "用户取消运行", null);
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
