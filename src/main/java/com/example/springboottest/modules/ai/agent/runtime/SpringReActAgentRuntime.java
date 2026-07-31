package com.example.springboottest.modules.ai.agent.runtime;

import com.example.springboottest.modules.ai.agent.entity.AgentContextSnapshot;
import com.example.springboottest.modules.ai.agent.entity.AgentPermissionRequest;
import com.example.springboottest.modules.ai.agent.entity.AgentSubagent;
import com.example.springboottest.modules.ai.agent.entity.AgentTodo;
import com.example.springboottest.modules.ai.agent.event.AgentEvent;
import com.example.springboottest.modules.ai.agent.event.AgentEventType;
import com.example.springboottest.modules.ai.agent.hook.AgentHookChain;
import com.example.springboottest.modules.ai.agent.message.AgentMessage;
import com.example.springboottest.modules.ai.agent.message.AgentRole;
import com.example.springboottest.modules.ai.agent.service.AgentContextCompactService;
import com.example.springboottest.modules.ai.agent.service.AgentEventStore;
import com.example.springboottest.modules.ai.agent.service.AgentPermissionService;
import com.example.springboottest.modules.ai.agent.service.AgentRunService;
import com.example.springboottest.modules.ai.agent.service.AgentSubagentService;
import com.example.springboottest.modules.ai.agent.service.AgentTodoService;
import com.example.springboottest.modules.ai.agent.tool.AgentToolDefinition;
import com.example.springboottest.modules.ai.agent.tool.AgentToolExecutor;
import com.example.springboottest.modules.ai.agent.tool.AgentToolRegistry;
import com.example.springboottest.modules.ai.agent.tool.AgentToolResult;
import com.example.springboottest.modules.ai.agent.tool.SkillAgentToolExecutor;
import com.example.springboottest.modules.ai.skill.entity.AiSkill;
import com.example.springboottest.modules.ai.skill.service.SkillScriptExecutor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
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
public class SpringReActAgentRuntime implements AgentRuntime {

    private static final String SYSTEM_PROMPT = """
            你是 AI-world 的 Claude Code 风格对话智能体运行时。
            你必须通过外部化步骤完成任务：需要多步处理时先写 Todo，再按 Todo 执行。
            你只能请求只读工具；不要请求写文件、支付、删除、提交表单或修改外部状态。
            你可以使用内置工具、挂载技能元信息、上下文摘要和历史消息。
            如果工具失败，请说明失败原因并给出保守回答。
            输出必须是严格 JSON，不要 Markdown。
            """;

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final AgentToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;
    private final SkillScriptExecutor skillScriptExecutor;
    private final AgentRunService runService;
    private final AgentTodoService todoService;
    private final AgentEventStore eventStore;
    private final AgentContextCompactService compactService;
    private final AgentPermissionService permissionService;
    private final AgentSubagentService subagentService;

    @Override
    public AgentResponse call(AgentCallContext context, AgentMessage userMessage, Consumer<AgentEvent> eventConsumer) {
        List<AgentEvent> events = new ArrayList<>();
        AgentHookChain hookChain = new AgentHookChain(List.of());
        AgentExecutionState state = new AgentExecutionState();
        Consumer<AgentEvent> publisher = event -> {
            events.add(event);
            eventStore.save(event, state.turnIndex);
            if (eventConsumer != null) {
                eventConsumer.accept(event);
            }
        };

        try {
            runService.startRun(context);
            publish(context, publisher, hookChain, AgentEventType.RUN_CREATED, statusPayload("running", "Agent run 已创建"));
            publish(context, publisher, hookChain, AgentEventType.REPLY_START, statusPayload("running", "智能体已启动"));
            publish(context, publisher, hookChain, AgentEventType.SYSTEM_MESSAGE, Map.of("summary", "进入多轮 Agent Loop，按需写 Todo、调用只读工具并压缩上下文。"));

            List<AgentMessage> loopMessages = new ArrayList<>(context.getHistory() == null ? List.of() : context.getHistory());
            loopMessages.add(userMessage);

            for (int turn = 1; turn <= context.getMaxTurns(); turn++) {
                state.turnIndex = turn;
                runService.updateTurn(context.getRunId(), turn);

                if (compactService.shouldCompact(loopMessages, state.scratchpad.toString())) {
                    compact(context, loopMessages, state, publisher, hookChain, "AUTO_COMPACT", "上下文超过预算");
                }

                AgentStepDecision decision = decideNextStep(context, loopMessages, state, publisher, hookChain);
                if (StringUtils.hasText(decision.getThought())) {
                    publish(context, publisher, hookChain, AgentEventType.REASONING_DELTA, Map.of("content", decision.getThought()));
                    state.scratchpad.append("Thought: ").append(decision.getThought()).append("\n");
                }

                String action = defaultIfBlank(decision.getAction(), "answer").toLowerCase();
                switch (action) {
                    case "todo_write" -> handleTodoWrite(context, decision, publisher, hookChain);
                    case "todo_update" -> handleTodoUpdate(context, decision, publisher, hookChain);
                    case "tool_call" -> handleToolCall(context, loopMessages, decision, state, publisher, hookChain);
                    case "compact" -> compact(context, loopMessages, state, publisher, hookChain,
                            decision.getCompact() == null ? "MANUAL" : decision.getCompact().getLevel(),
                            decision.getCompact() == null ? "模型请求压缩" : decision.getCompact().getReason());
                    case "subagent" -> handleSubagent(context, decision, state, publisher, hookChain);
                    case "answer" -> {
                        String answer = defaultIfBlank(decision.getAnswer(), decision.getSummary());
                        if (!StringUtils.hasText(answer)) {
                            answer = generateFallbackAnswer(context, loopMessages, state, publisher, hookChain);
                        }
                        return complete(context, answer, events, publisher, hookChain);
                    }
                    default -> state.scratchpad.append("Unknown action ignored: ").append(action).append("\n");
                }
            }

            String answer = generateFallbackAnswer(context, loopMessages, state, publisher, hookChain);
            return complete(context, answer, events, publisher, hookChain);
        } catch (WaitingPermissionException e) {
            runService.waitingPermission(context.getRunId(), e.getMessage());
            publish(context, publisher, hookChain, AgentEventType.RUN_PAUSED, statusPayload("waiting_permission", e.getMessage()));
            return AgentResponse.builder()
                    .success(false)
                    .answer("需要用户授权后才能继续。")
                    .error(e.getMessage())
                    .events(events)
                    .build();
        } catch (Exception e) {
            log.error("Agent runtime failed", e);
            runService.fail(context.getRunId(), "智能体执行失败", defaultIfBlank(e.getMessage(), "未知错误"));
            publish(context, publisher, hookChain, AgentEventType.REPLY_ERROR, statusPayload("failed", defaultIfBlank(e.getMessage(), "智能体执行失败")));
            return AgentResponse.builder()
                    .success(false)
                    .answer("抱歉，智能体暂时无法完成这次任务。")
                    .error(defaultIfBlank(e.getMessage(), "智能体执行失败"))
                    .events(events)
                    .build();
        }
    }

    private AgentStepDecision decideNextStep(AgentCallContext context,
                                             List<AgentMessage> messages,
                                             AgentExecutionState state,
                                             Consumer<AgentEvent> publisher,
                                             AgentHookChain hookChain) {
        publish(context, publisher, hookChain, AgentEventType.MODEL_CALL_START, Map.of("stage", "agent_step", "turn", state.turnIndex));
        String prompt = buildStepPrompt(context, messages, state, null);
        String raw = callModel(context, List.of(
                new SystemMessage(SYSTEM_PROMPT),
                new UserMessage(prompt)
        ));
        publish(context, publisher, hookChain, AgentEventType.MODEL_CALL_END, Map.of("stage", "agent_step", "turn", state.turnIndex));
        AgentStepDecision parsed = parseStep(raw);
        if (parsed != null) {
            return parsed;
        }

        publish(context, publisher, hookChain, AgentEventType.MODEL_CALL_START, Map.of("stage", "agent_step_repair", "turn", state.turnIndex));
        String repairedRaw = callModel(context, List.of(
                new SystemMessage("你只负责把非 JSON 内容修复为符合 schema 的 JSON。"),
                new UserMessage(buildStepPrompt(context, messages, state, raw))
        ));
        publish(context, publisher, hookChain, AgentEventType.MODEL_CALL_END, Map.of("stage", "agent_step_repair", "turn", state.turnIndex));
        AgentStepDecision repaired = parseStep(repairedRaw);
        if (repaired != null) {
            return repaired;
        }
        AgentStepDecision fallback = new AgentStepDecision();
        fallback.setAction("answer");
        fallback.setAnswer(defaultIfBlank(raw, "抱歉，我没有生成有效的结构化步骤。"));
        return fallback;
    }

    private void handleTodoWrite(AgentCallContext context,
                                 AgentStepDecision decision,
                                 Consumer<AgentEvent> publisher,
                                 AgentHookChain hookChain) {
        List<Map<String, Object>> todoMaps = new ArrayList<>();
        for (AgentStepDecision.TodoItem item : decision.getTodos() == null ? List.<AgentStepDecision.TodoItem>of() : decision.getTodos()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("todoId", item.getTodoId());
            map.put("content", item.getContent());
            map.put("status", item.getStatus());
            todoMaps.add(map);
        }
        List<AgentTodo> todos = todoService.replaceTodos(context.getRunId(), context.getThreadId(), todoMaps);
        publish(context, publisher, hookChain, AgentEventType.TODO_CREATED, Map.of("todos", todos));
    }

    private void handleTodoUpdate(AgentCallContext context,
                                  AgentStepDecision decision,
                                  Consumer<AgentEvent> publisher,
                                  AgentHookChain hookChain) {
        if (decision.getTodoUpdate() == null || !StringUtils.hasText(decision.getTodoUpdate().getTodoId())) {
            return;
        }
        AgentTodo todo = todoService.updateStatus(
                context.getRunId(),
                decision.getTodoUpdate().getTodoId(),
                decision.getTodoUpdate().getStatus()
        );
        publish(context, publisher, hookChain, AgentEventType.TODO_UPDATED, Map.of("todo", todo));
    }

    private void handleToolCall(AgentCallContext context,
                                List<AgentMessage> loopMessages,
                                AgentStepDecision decision,
                                AgentExecutionState state,
                                Consumer<AgentEvent> publisher,
                                AgentHookChain hookChain) {
        if (decision.getTool() == null || !StringUtils.hasText(decision.getTool().getToolName())) {
            state.scratchpad.append("Tool call skipped: missing toolName\n");
            return;
        }
        String toolName = decision.getTool().getToolName().trim();
        Map<String, Object> arguments = decision.getTool().getArguments() == null ? Map.of() : decision.getTool().getArguments();
        AgentToolExecutor executor = resolveTool(context, toolName);
        if (executor == null) {
            AgentToolResult result = AgentToolResult.error("工具不存在：" + toolName);
            publish(context, publisher, hookChain, AgentEventType.TOOL_CALL_ERROR, toolPayload(toolName, arguments, result.displayText()));
            state.scratchpad.append("Tool ").append(toolName).append(" failed: ").append(result.displayText()).append("\n");
            loopMessages.add(AgentMessage.text(AgentRole.TOOL, "Tool " + toolName + " result:\n" + result.displayText()));
            return;
        }
        AgentToolDefinition definition = executor == null ? null : executor.definition();
        if (!permissionService.isAllowed(definition)) {
            AgentPermissionRequest request = permissionService.requestPermission(
                    context.getRunId(),
                    context.getThreadId(),
                    toolName,
                    arguments,
                    "工具不在只读白名单或需要审批"
            );
            publish(context, publisher, hookChain, AgentEventType.PERMISSION_REQUESTED, Map.of("request", request));
            throw new WaitingPermissionException("工具需要授权: " + toolName);
        }

        publish(context, publisher, hookChain, AgentEventType.TOOL_CALL_START, toolPayload(toolName, arguments, null));
        AgentToolResult result = executor.execute(arguments);
        String displayText = compactService.trimToolResult(result.displayText());
        publish(context, publisher, hookChain,
                result.success() ? AgentEventType.TOOL_CALL_END : AgentEventType.TOOL_CALL_ERROR,
                toolPayload(toolName, arguments, displayText));
        state.scratchpad.append("Tool ").append(toolName)
                .append(result.success() ? " success: " : " failed: ")
                .append(displayText).append("\n");
        loopMessages.add(AgentMessage.text(AgentRole.TOOL, "Tool " + toolName + " result:\n" + displayText));
    }

    private void compact(AgentCallContext context,
                         List<AgentMessage> loopMessages,
                         AgentExecutionState state,
                         Consumer<AgentEvent> publisher,
                         AgentHookChain hookChain,
                         String level,
                         String reason) {
        AgentContextSnapshot snapshot = compactService.compact(
                context.getRunId(),
                context.getThreadId(),
                loopMessages,
                state.scratchpad.toString(),
                defaultIfBlank(level, "AUTO_COMPACT")
        );
        loopMessages.clear();
        loopMessages.add(AgentMessage.text(AgentRole.SYSTEM, snapshot.getSummary()));
        state.scratchpad.setLength(0);
        state.scratchpad.append("Context compacted: ").append(defaultIfBlank(reason, "压缩上下文")).append("\n");
        publish(context, publisher, hookChain, AgentEventType.CONTEXT_COMPACTED,
                Map.of("snapshot", snapshot, "reason", defaultIfBlank(reason, "")));
    }

    private void handleSubagent(AgentCallContext context,
                                AgentStepDecision decision,
                                AgentExecutionState state,
                                Consumer<AgentEvent> publisher,
                                AgentHookChain hookChain) {
        AgentStepDecision.SubagentAction subagentAction = decision.getSubagent();
        String agentType = subagentAction == null ? "general-purpose" : defaultIfBlank(subagentAction.getAgentType(), "general-purpose");
        String task = subagentAction == null ? "" : defaultIfBlank(subagentAction.getTask(), "");
        AgentSubagent subagent = subagentService.createPlaceholder(context.getRunId(), context.getThreadId(), agentType, task);
        publish(context, publisher, hookChain, AgentEventType.SUBAGENT_STARTED, Map.of("subagent", subagent));
        publish(context, publisher, hookChain, AgentEventType.SUBAGENT_FINISHED, Map.of("subagent", subagent));
        state.scratchpad.append("Subagent ").append(agentType).append(" recorded: ").append(task).append("\n");
    }

    private AgentResponse complete(AgentCallContext context,
                                   String answer,
                                   List<AgentEvent> events,
                                   Consumer<AgentEvent> publisher,
                                   AgentHookChain hookChain) {
        publish(context, publisher, hookChain, AgentEventType.TEXT_BLOCK_START, Map.of("role", "assistant"));
        publish(context, publisher, hookChain, AgentEventType.TEXT_BLOCK_DELTA, Map.of("content", answer));
        publish(context, publisher, hookChain, AgentEventType.TEXT_BLOCK_END, Map.of("contentLength", answer.length()));
        publish(context, publisher, hookChain, AgentEventType.REPLY_END, statusPayload("completed", "智能体执行完成"));
        runService.complete(context.getRunId(), answer);
        return AgentResponse.builder()
                .success(true)
                .answer(answer)
                .message(AgentMessage.text(AgentRole.ASSISTANT, answer))
                .events(events)
                .build();
    }

    private String generateFallbackAnswer(AgentCallContext context,
                                          List<AgentMessage> loopMessages,
                                          AgentExecutionState state,
                                          Consumer<AgentEvent> publisher,
                                          AgentHookChain hookChain) {
        publish(context, publisher, hookChain, AgentEventType.MODEL_CALL_START, Map.of("stage", "final_answer"));
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(buildFinalSystemPrompt(context)));
        messages.addAll(toSpringMessages(loopMessages));
        messages.add(new UserMessage("执行轨迹摘要：\n" + state.scratchpad + "\n请给出最终中文回答。"));
        String answer = callModel(context, messages);
        publish(context, publisher, hookChain, AgentEventType.MODEL_CALL_END, Map.of("stage", "final_answer"));
        return defaultIfBlank(answer, "抱歉，我暂时没有生成有效回复。");
    }

    private String buildStepPrompt(AgentCallContext context,
                                   List<AgentMessage> messages,
                                   AgentExecutionState state,
                                   String invalidJson) {
        StringBuilder builder = new StringBuilder();
        builder.append("你要输出一个 Agent step JSON，schema 如下：\n");
        builder.append("""
                {
                  "thought": "一句话说明当前判断",
                  "action": "todo_write|todo_update|tool_call|compact|subagent|answer",
                  "todos": [{"todoId":"t1","content":"任务","status":"PENDING"}],
                  "todoUpdate": {"todoId":"t1","status":"IN_PROGRESS"},
                  "tool": {"toolName":"get_weather","arguments":{"city":"上海"}},
                  "compact": {"level":"AUTO_COMPACT","reason":"..."},
                  "subagent": {"agentType":"explore|planner|general-purpose|code-reviewer","task":"..."},
                  "answer": "最终答案",
                  "summary": "可选摘要"
                }
                """);
        builder.append("\n规则：\n");
        builder.append("1. 多步任务如果还没有 Todo，优先 action=todo_write。\n");
        builder.append("2. 调工具前如果有 Todo，先把相关 Todo 设为 IN_PROGRESS。\n");
        builder.append("3. 工具只能选择只读工具。\n");
        builder.append("4. 已有足够信息时 action=answer。\n");
        builder.append("\n可用工具：\n").append(renderToolDefinitions(context));
        builder.append("\n\n挂载技能元信息：\n").append(renderSkillMetadata(context));
        builder.append("\n\n当前 Todo：\n").append(renderTodos(context.getRunId()));
        AgentContextSnapshot snapshot = compactService.latest(context.getRunId());
        if (snapshot != null && StringUtils.hasText(snapshot.getSummary())) {
            builder.append("\n\n上下文摘要：\n").append(snapshot.getSummary());
        }
        builder.append("\n\n执行草稿：\n").append(state.scratchpad);
        builder.append("\n\n消息：\n");
        for (AgentMessage message : messages) {
            builder.append(message.getRole()).append(": ").append(message.textContent()).append("\n");
        }
        if (StringUtils.hasText(invalidJson)) {
            builder.append("\n上一次输出不是有效 JSON，请修复：\n").append(invalidJson);
        }
        return builder.toString();
    }

    private AgentToolExecutor resolveTool(AgentCallContext context, String toolName) {
        AgentToolExecutor builtin = toolRegistry.get(toolName).orElse(null);
        if (builtin != null) {
            return builtin;
        }
        if (context.getMountedSkills() == null || context.getMountedSkills().isEmpty()) {
            return null;
        }
        return context.getMountedSkills().stream()
                .filter(this::isExecutableSkill)
                .filter(skill -> ("skill_" + skill.getSkillKey()).equals(toolName))
                .findFirst()
                .map(skill -> (AgentToolExecutor) new SkillAgentToolExecutor(skill, context.getThreadId(), skillScriptExecutor))
                .orElse(null);
    }

    private String renderToolDefinitions(AgentCallContext context) {
        try {
            List<Map<String, Object>> definitions = new ArrayList<>(toolRegistry.definitions().stream()
                    .map(this::toolDefinitionMap)
                    .toList());
            if (context.getMountedSkills() != null) {
                for (AiSkill skill : context.getMountedSkills()) {
                    if (isExecutableSkill(skill)) {
                        definitions.add(toolDefinitionMap(new SkillAgentToolExecutor(skill, context.getThreadId(), skillScriptExecutor).definition()));
                    }
                }
            }
            return objectMapper.writeValueAsString(definitions);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String renderSkillMetadata(AgentCallContext context) {
        try {
            if (context.getMountedSkills() == null || context.getMountedSkills().isEmpty()) {
                return "[]";
            }
            List<Map<String, Object>> metadata = context.getMountedSkills().stream()
                    .map(skill -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("key", skill.getSkillKey());
                        map.put("name", skill.getName());
                        map.put("description", skill.getDescription());
                        map.put("type", skill.getSkillType());
                        map.put("readonly", skill.getReadonly());
                        map.put("parameterSchema", skill.getParameterSchema());
                        return map;
                    })
                    .toList();
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String renderTodos(String runId) {
        try {
            return objectMapper.writeValueAsString(todoService.listTodos(runId));
        } catch (Exception e) {
            return "[]";
        }
    }

    private Map<String, Object> toolDefinitionMap(AgentToolDefinition definition) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", definition.getName());
        map.put("description", definition.getDescription());
        map.put("parameters", definition.getParametersSchema());
        map.put("readOnly", definition.isReadOnly());
        map.put("approvalRequired", definition.isApprovalRequired());
        return map;
    }

    private AgentStepDecision parseStep(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String normalized = stripCodeFence(raw).trim();
        int start = normalized.indexOf('{');
        int end = normalized.lastIndexOf('}');
        if (start >= 0 && end > start) {
            normalized = normalized.substring(start, end + 1);
        }
        try {
            objectMapper.readValue(normalized, MAP_TYPE);
            return objectMapper.readValue(normalized, AgentStepDecision.class);
        } catch (Exception e) {
            log.debug("Failed to parse agent step JSON: {}", raw, e);
            return null;
        }
    }

    private String callModel(AgentCallContext context, List<Message> messages) {
        ChatResponse response = context.getModel().chatModel().call(new Prompt(messages, buildOptions(context)));
        return extractResponseText(response);
    }

    private OpenAiChatOptions buildOptions(AgentCallContext context) {
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder().model(context.getModel().modelName());
        if (context.getModel().options() != null && context.getModel().options().getTemperature() != null) {
            builder.temperature(context.getModel().options().getTemperature());
        }
        if (context.getModel().options() != null && context.getModel().options().getMaxTokens() != null) {
            builder.maxTokens(context.getModel().options().getMaxTokens());
        }
        return builder.build();
    }

    private void publish(AgentCallContext context,
                         Consumer<AgentEvent> publisher,
                         AgentHookChain hookChain,
                         AgentEventType type,
                         Map<String, Object> payload) {
        AgentEvent event = AgentEvent.of(context.getRunId(), context.getThreadId(), type, payload);
        hookChain.publish(context, event);
        publisher.accept(event);
    }

    private List<Message> toSpringMessages(List<AgentMessage> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        return history.stream()
                .filter(item -> item.getRole() == AgentRole.USER || item.getRole() == AgentRole.ASSISTANT || item.getRole() == AgentRole.SYSTEM || item.getRole() == AgentRole.TOOL)
                .map(item -> (Message) new UserMessage(item.getRole() + ": " + item.textContent()))
                .toList();
    }

    private Map<String, Object> statusPayload(String status, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", status);
        payload.put("message", message);
        return payload;
    }

    private Map<String, Object> toolPayload(String toolName, Map<String, Object> arguments, String result) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("toolName", toolName);
        payload.put("arguments", arguments == null ? Map.of() : arguments);
        if (result != null) {
            payload.put("result", result);
        }
        return payload;
    }

    private String extractResponseText(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        return response.getResult().getOutput().getText();
    }

    private String stripCodeFence(String raw) {
        return raw.replace("```json", "").replace("```JSON", "").replace("```", "");
    }

    private String buildFinalSystemPrompt(AgentCallContext context) {
        if (!StringUtils.hasText(context.getMountedSkillPrompt())) {
            return "请基于用户问题、历史上下文和工具结果，用自然、可信的中文回答。";
        }
        return "请基于用户问题、历史上下文和工具结果，用自然、可信的中文回答。\n\n按需参考已加载技能内容：\n" + context.getMountedSkillPrompt();
    }

    private boolean isExecutableSkill(AiSkill skill) {
        if (skill == null || !Boolean.TRUE.equals(skill.getEnabled())) {
            return false;
        }
        String type = defaultIfBlank(skill.getSkillType(), "PROMPT").toUpperCase();
        return ("TOOL".equals(type) || "MIXED".equals(type)) && StringUtils.hasText(skill.getEntryCommand());
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private static class AgentExecutionState {
        private int turnIndex;
        private final StringBuilder scratchpad = new StringBuilder();
    }

    private static class WaitingPermissionException extends RuntimeException {
        private WaitingPermissionException(String message) {
            super(message);
        }
    }
}
