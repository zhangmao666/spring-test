package com.example.springboottest.modules.ai.skills;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

import java.util.Map;

/**
 * 工具调用计时包装器
 * 包装任意 ToolCallback，记录每次工具调用的耗时，用于诊断 Agent 性能瓶颈
 */
@Slf4j
public class TimingToolCallback implements ToolCallback {

    private final ToolCallback delegate;

    public TimingToolCallback(ToolCallback delegate) {
        this.delegate = delegate;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        // 统一转发到带 ToolContext 的版本，避免某些工具（如 SkillsTool）
        // 底层方法要求 ToolContext 参数时抛出 IllegalArgumentException
        return call(toolInput, new ToolContext(Map.of()));
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        String toolName = delegate.getToolDefinition().name();
        log.info("⏱️ [工具调用开始] {} | 输入: {}", toolName, truncate(toolInput, 200));
        long start = System.currentTimeMillis();
        try {
            String result = delegate.call(toolInput, toolContext);
            long elapsed = System.currentTimeMillis() - start;
            log.info("⏱️ [工具调用完成] {} | 耗时: {}ms | 输出: {}", toolName, elapsed, truncate(result, 300));
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("⏱️ [工具调用失败] {} | 耗时: {}ms | 错误: {}", toolName, elapsed, e.getMessage());
            throw e;
        }
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "null";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...(truncated)";
    }

    /**
     * 批量包装工具回调数组
     */
    public static ToolCallback[] wrapAll(ToolCallback... callbacks) {
        ToolCallback[] wrapped = new ToolCallback[callbacks.length];
        for (int i = 0; i < callbacks.length; i++) {
            wrapped[i] = new TimingToolCallback(callbacks[i]);
        }
        return wrapped;
    }
}
