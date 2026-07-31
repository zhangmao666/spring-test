package com.example.springboottest.modules.ai.agent.tool;

import com.example.springboottest.modules.news.dto.HotNewsItemResponse;
import com.example.springboottest.modules.news.dto.HotNewsResponse;
import com.example.springboottest.modules.news.service.HotboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HotNewsAgentTool implements AgentToolExecutor {

    private final HotboardService hotboardService;

    @Override
    public AgentToolDefinition definition() {
        return AgentToolDefinition.builder()
                .id("hot_news")
                .name("get_hot_news")
                .description("获取热点新闻列表，可选 platform 和 limit 字段。")
                .parametersSchema(AgentToolSchemas.object(
                        Map.of(
                                "platform", AgentToolSchemas.stringProperty("热点平台，例如 thepaper、toutiao、tencent-news"),
                                "limit", AgentToolSchemas.integerProperty("返回条数，建议 1 到 10")
                        ),
                        List.of()
                ))
                .build();
    }

    @Override
    public AgentToolResult execute(Map<String, Object> arguments) {
        String platform = value(arguments, "platform", HotboardService.DEFAULT_PLATFORM);
        int limit = intValue(arguments, "limit", 5);
        limit = Math.max(1, Math.min(limit, 10));
        try {
            HotNewsResponse response = hotboardService.getHotNews(platform);
            List<HotNewsItemResponse> items = response.getItems() == null
                    ? Collections.emptyList()
                    : response.getItems().stream().limit(limit).toList();
            StringBuilder builder = new StringBuilder();
            builder.append("平台：").append(fallback(response.getPlatformName(), platform)).append("\n");
            builder.append("更新时间：").append(fallback(response.getUpdateTime(), "未知")).append("\n");
            if (items.isEmpty()) {
                builder.append("当前没有可用热点。");
            } else {
                for (HotNewsItemResponse item : items) {
                    builder.append(item.getRank() == null ? "-" : item.getRank())
                            .append(". ")
                            .append(fallback(item.getTitle(), "未命名热点"));
                    if (StringUtils.hasText(item.getHotValue())) {
                        builder.append("（热度：").append(item.getHotValue()).append("）");
                    }
                    if (StringUtils.hasText(item.getUrl())) {
                        builder.append(" 链接：").append(item.getUrl());
                    }
                    builder.append("\n");
                }
            }
            return AgentToolResult.success(builder.toString().trim());
        } catch (Exception e) {
            return AgentToolResult.error("热点查询失败：" + fallback(e.getMessage(), "未知错误"));
        }
    }

    private String value(Map<String, Object> arguments, String key, String fallback) {
        Object value = arguments == null ? null : arguments.get(key);
        return StringUtils.hasText(value == null ? null : String.valueOf(value)) ? String.valueOf(value).trim() : fallback;
    }

    private int intValue(Map<String, Object> arguments, String key, int fallback) {
        Object value = arguments == null ? null : arguments.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? fallback : Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String fallback(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
