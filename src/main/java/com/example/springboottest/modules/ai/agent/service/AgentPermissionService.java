package com.example.springboottest.modules.ai.agent.service;

import com.example.springboottest.modules.ai.agent.entity.AgentPermissionRequest;
import com.example.springboottest.modules.ai.agent.mapper.AgentPermissionRequestMapper;
import com.example.springboottest.modules.ai.agent.runtime.AgentRuntimeStatus;
import com.example.springboottest.modules.ai.agent.tool.AgentToolDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentPermissionService {

    private static final Set<String> READ_ONLY_ALLOWED_TOOLS = Set.of(
            "get_weather",
            "get_hot_news",
            "get_current_time",
            "get_market_quote",
            "read_web_page"
    );

    private final AgentPermissionRequestMapper permissionMapper;
    private final ObjectMapper objectMapper;

    public boolean isAllowed(AgentToolDefinition definition) {
        if (definition == null) {
            return false;
        }
        if (definition.getName() != null && definition.getName().startsWith("skill_")) {
            return definition.isReadOnly() && !definition.isApprovalRequired();
        }
        return definition.isReadOnly()
                && !definition.isApprovalRequired()
                && READ_ONLY_ALLOWED_TOOLS.contains(definition.getName());
    }

    public AgentPermissionRequest requestPermission(String runId,
                                                    String conversationId,
                                                    String toolName,
                                                    Map<String, Object> arguments,
                                                    String reason) {
        try {
            AgentPermissionRequest request = AgentPermissionRequest.builder()
                    .requestId(UUID.randomUUID().toString())
                    .runId(runId)
                    .conversationId(conversationId)
                    .toolName(toolName)
                    .argumentsPayload(objectMapper.writeValueAsString(arguments == null ? Map.of() : arguments))
                    .reason(reason)
                    .status(AgentRuntimeStatus.PERMISSION_PENDING)
                    .build();
            permissionMapper.insert(request);
            return request;
        } catch (Exception e) {
            log.warn("Failed to create permission request for tool {}", toolName, e);
            throw new IllegalStateException("权限请求创建失败", e);
        }
    }

    public AgentPermissionRequest get(String requestId) {
        return permissionMapper.findByRequestId(requestId);
    }

    public void decide(String requestId, boolean approved) {
        permissionMapper.decide(
                requestId,
                approved ? AgentRuntimeStatus.PERMISSION_APPROVED : AgentRuntimeStatus.PERMISSION_DENIED,
                approved ? "APPROVE" : "DENY"
        );
    }
}
