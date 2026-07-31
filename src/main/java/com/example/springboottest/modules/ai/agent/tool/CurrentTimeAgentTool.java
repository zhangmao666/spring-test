package com.example.springboottest.modules.ai.agent.tool;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class CurrentTimeAgentTool implements AgentToolExecutor {

    @Override
    public AgentToolDefinition definition() {
        return AgentToolDefinition.builder()
                .id("current_time")
                .name("get_current_time")
                .description("获取当前时间，可选 timezone 字段，例如 Asia/Shanghai。")
                .parametersSchema(AgentToolSchemas.object(
                        Map.of("timezone", AgentToolSchemas.stringProperty("时区，例如 Asia/Shanghai")),
                        List.of()
                ))
                .build();
    }

    @Override
    public AgentToolResult execute(Map<String, Object> arguments) {
        String timezone = value(arguments, "timezone", "Asia/Shanghai");
        try {
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            return AgentToolResult.success("""
                    当前时间：%s
                    时区：%s
                    星期：%s
                    """.formatted(
                    now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    zoneId.getId(),
                    now.getDayOfWeek()
            ).trim());
        } catch (Exception e) {
            return AgentToolResult.error("时间查询失败：" + (StringUtils.hasText(e.getMessage()) ? e.getMessage() : "未知错误"));
        }
    }

    private String value(Map<String, Object> arguments, String key, String fallback) {
        Object value = arguments == null ? null : arguments.get(key);
        return StringUtils.hasText(value == null ? null : String.valueOf(value)) ? String.valueOf(value).trim() : fallback;
    }
}
