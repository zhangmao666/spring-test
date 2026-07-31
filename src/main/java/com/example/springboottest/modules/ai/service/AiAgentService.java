package com.example.springboottest.modules.ai.service;

import com.example.springboottest.modules.ai.agent.message.AgentMessage;
import com.example.springboottest.modules.ai.agent.message.AgentRole;
import com.example.springboottest.modules.ai.agent.model.AgentGenerateOptions;
import com.example.springboottest.modules.ai.agent.model.ResolvedAgentModel;
import com.example.springboottest.modules.ai.agent.runtime.AgentCallContext;
import com.example.springboottest.modules.ai.agent.runtime.AgentResponse;
import com.example.springboottest.modules.ai.agent.runtime.AgentRuntime;
import com.example.springboottest.modules.ai.setvice.AiModelService;
import com.example.springboottest.modules.ai.skill.entity.AiSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAgentService {

    private static final String DEFAULT_AGENT_NAME = "spring_react_agent";

    private final AgentRuntime agentRuntime;
    private final AiModelService aiModelService;

    public AgentRunResult run(AgentModelConfig modelConfig,
                              String conversationId,
                              String userMessage,
                              List<Message> historyMessages,
                              List<AiSkill> mountedSkills,
                              String mountedSkillPrompt,
                              Consumer<AiAgentService.AgentEvent> eventConsumer) {
        Consumer<AiAgentService.AgentEvent> safeConsumer = eventConsumer == null ? event -> {
        } : eventConsumer;

        try {
            ResolvedAgentModel resolvedModel = resolveModel(modelConfig);
            AgentCallContext context = AgentCallContext.builder()
                    .threadId(conversationId)
                    .agentName(DEFAULT_AGENT_NAME)
                    .model(resolvedModel)
                    .history(toAgentMessages(historyMessages))
                    .mountedSkills(mountedSkills == null ? List.of() : mountedSkills)
                    .mountedSkillPrompt(mountedSkillPrompt)
                    .permissionMode(defaultIfBlank(modelConfig.getPermissionMode(), "DEFAULT"))
                    .metadata(buildMetadata(modelConfig))
                    .build();

            AgentResponse response = agentRuntime.call(
                    context,
                    AgentMessage.text(AgentRole.USER, defaultIfBlank(userMessage, "")),
                    event -> safeConsumer.accept(toLegacyEvent(event))
            );

            return AgentRunResult.builder()
                    .answer(defaultIfBlank(response.answer(), "抱歉，智能体暂时没有生成有效回复。"))
                    .success(response.success())
                    .error(response.error())
                    .runId(context.getRunId())
                    .status(response.success() ? "COMPLETED" : "FAILED")
                    .build();
        } catch (Exception e) {
            log.error("Spring agent execution failed", e);
            safeConsumer.accept(AiAgentService.AgentEvent.status("failed", defaultIfBlank(e.getMessage(), "智能体执行失败")));
            return AgentRunResult.builder()
                    .answer("抱歉，智能体暂时无法完成这次任务。")
                    .success(false)
                    .error(defaultIfBlank(e.getMessage(), "智能体执行失败"))
                    .status("FAILED")
                    .build();
        }
    }

    private Map<String, Object> buildMetadata(AgentModelConfig config) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("agentMode", defaultIfBlank(config.getAgentMode(), "DEFAULT"));
        metadata.put("resumeFromRunId", config.getResumeRunId());
        return metadata;
    }

    private ResolvedAgentModel resolveModel(AgentModelConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("智能体模型配置不能为空");
        }
        if (!StringUtils.hasText(config.getBaseUrl()) || !StringUtils.hasText(config.getApiKey()) || !StringUtils.hasText(config.getModelName())) {
            throw new IllegalArgumentException("智能体模型配置不完整，请检查 Base URL、API Key 和模型名称");
        }

        AgentGenerateOptions options = AgentGenerateOptions.builder()
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .useDeepThinking(false)
                .build();

        return ResolvedAgentModel.builder()
                .available(true)
                .managed(true)
                .chatModel(aiModelService.createChatModel(
                        config.getBaseUrl(),
                        config.getApiKey(),
                        config.getModelName(),
                        config.getTemperature(),
                        config.getMaxTokens(),
                        false
                ))
                .provider(defaultIfBlank(config.getProvider(), "openai"))
                .modelName(config.getModelName())
                .displayName(config.getModelName())
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .options(options)
                .build();
    }

    private List<AgentMessage> toAgentMessages(List<Message> historyMessages) {
        if (historyMessages == null || historyMessages.isEmpty()) {
            return List.of();
        }
        List<AgentMessage> messages = new ArrayList<>();
        for (Message item : historyMessages) {
            String text = extractSpringAiText(item);
            if (!StringUtils.hasText(text)) {
                continue;
            }
            if (item instanceof SystemMessage) {
                messages.add(AgentMessage.text(AgentRole.SYSTEM, text));
            } else if (item instanceof AssistantMessage) {
                messages.add(AgentMessage.text(AgentRole.ASSISTANT, text));
            } else {
                messages.add(AgentMessage.text(AgentRole.USER, text));
            }
        }
        return messages;
    }

    private AiAgentService.AgentEvent toLegacyEvent(com.example.springboottest.modules.ai.agent.event.AgentEvent event) {
        com.example.springboottest.modules.ai.agent.event.AgentEvent.LegacySseEvent legacy = event.toLegacySseEvent();
        return AiAgentService.AgentEvent.builder()
                .type(legacy.name())
                .payload(legacy.payload())
                .build();
    }

    private String extractSpringAiText(Message message) {
        if (message == null) {
            return "";
        }
        try {
            return defaultIfBlank(message.getText(), "");
        } catch (Exception ignored) {
            return "";
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentModelConfig {
        private String provider;
        private String baseUrl;
        private String apiKey;
        private String modelName;
        private Double temperature;
        private Integer maxTokens;
        private String agentMode;
        private String permissionMode;
        private String resumeRunId;
    }

    @Data
    @Builder
    public static class AgentRunResult {
        private boolean success;
        private String answer;
        private String error;
        private String runId;
        private String status;
    }

    @Data
    @Builder
    public static class AgentEvent {
        private String type;
        private Map<String, Object> payload;

        public static AgentEvent status(String status, String message) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("status", status);
            payload.put("message", message);
            return AgentEvent.builder().type("agent_status").payload(payload).build();
        }
    }
}
