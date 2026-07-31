package com.example.springboottest.modules.ai.agent.service;

import com.example.springboottest.modules.ai.agent.entity.AgentContextSnapshot;
import com.example.springboottest.modules.ai.agent.mapper.AgentContextSnapshotMapper;
import com.example.springboottest.modules.ai.agent.message.AgentMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentContextCompactService {

    private static final int DEFAULT_CHAR_BUDGET = 24000;
    private static final int HARD_CHAR_BUDGET = 36000;

    private final AgentContextSnapshotMapper snapshotMapper;

    public boolean shouldCompact(List<AgentMessage> messages, String scratchpad) {
        return estimateChars(messages, scratchpad) > DEFAULT_CHAR_BUDGET;
    }

    public AgentContextSnapshot compact(String runId,
                                        String conversationId,
                                        List<AgentMessage> messages,
                                        String scratchpad,
                                        String level) {
        String summary = buildSummary(messages, scratchpad);
        int count = snapshotMapper.countByRunId(runId);
        AgentContextSnapshot snapshot = AgentContextSnapshot.builder()
                .runId(runId)
                .conversationId(conversationId)
                .snapshotIndex(count + 1)
                .compactLevel(defaultIfBlank(level, "AUTO_COMPACT"))
                .estimatedTokens(Math.max(1, estimateChars(messages, scratchpad) / 4))
                .charBudget(DEFAULT_CHAR_BUDGET)
                .summary(summary)
                .sourceBoundary("messages=" + (messages == null ? 0 : messages.size()))
                .build();
        snapshotMapper.insert(snapshot);
        return snapshot;
    }

    public AgentContextSnapshot latest(String runId) {
        return snapshotMapper.findLatestByRunId(runId);
    }

    public int estimateChars(List<AgentMessage> messages, String scratchpad) {
        int total = StringUtils.hasText(scratchpad) ? scratchpad.length() : 0;
        if (messages != null) {
            for (AgentMessage message : messages) {
                total += message.textContent().length();
            }
        }
        return total;
    }

    public String trimToolResult(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= 4000 ? trimmed : trimmed.substring(0, 4000) + "\n...[tool output truncated]";
    }

    private String buildSummary(List<AgentMessage> messages, String scratchpad) {
        StringBuilder builder = new StringBuilder();
        builder.append("当前会话已自动压缩。保留最近对话、工具结果摘要与任务状态。\n");
        if (messages != null && !messages.isEmpty()) {
            int from = Math.max(0, messages.size() - 6);
            builder.append("最近消息：\n");
            for (AgentMessage item : messages.subList(from, messages.size())) {
                builder.append("- ").append(item.getRole()).append(": ")
                        .append(limit(item.textContent(), 500)).append("\n");
            }
        }
        if (StringUtils.hasText(scratchpad)) {
            builder.append("执行摘要：\n").append(limit(scratchpad, 3000));
        }
        return limit(builder.toString(), HARD_CHAR_BUDGET);
    }

    private String limit(String value, int max) {
        if (!StringUtils.hasText(value) || value.length() <= max) {
            return value == null ? "" : value;
        }
        return value.substring(0, max) + "...";
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
