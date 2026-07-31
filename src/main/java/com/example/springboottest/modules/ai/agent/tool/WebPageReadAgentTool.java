package com.example.springboottest.modules.ai.agent.tool;

import com.example.springboottest.modules.ai.websearch.WebPageFetchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebPageReadAgentTool implements AgentToolExecutor {

    private final WebPageFetchService webPageFetchService;

    @Override
    public AgentToolDefinition definition() {
        return AgentToolDefinition.builder()
                .id("webpage_read")
                .name("read_webpage")
                .description("读取公开网页正文摘要，输入需要包含 url 字段。适合总结网页、提取要点或快速理解文章。")
                .parametersSchema(AgentToolSchemas.object(
                        Map.of("url", AgentToolSchemas.stringProperty("公开网页链接，必须为 http 或 https")),
                        List.of("url")
                ))
                .build();
    }

    @Override
    public AgentToolResult execute(Map<String, Object> arguments) {
        String url = value(arguments, "url", "");
        try {
            WebPageFetchService.FetchResult result = webPageFetchService.fetch(url);
            if (!result.allowed()) {
                return AgentToolResult.error("网页读取失败：URL 不被允许，可能是内网、本地地址或非 http(s) 链接。");
            }
            if (!result.success()) {
                return AgentToolResult.error("网页读取失败：" + fallback(result.reason(), "未知错误"));
            }
            return AgentToolResult.success("""
                    网页链接：%s
                    域名：%s
                    正文摘录：
                    %s
                    """.formatted(url, extractDomain(url), fallback(result.content(), "未提取到可读正文")).trim());
        } catch (Exception e) {
            return AgentToolResult.error("网页读取失败：" + fallback(e.getMessage(), "未知错误"));
        }
    }

    private String value(Map<String, Object> arguments, String key, String fallback) {
        Object value = arguments == null ? null : arguments.get(key);
        return StringUtils.hasText(value == null ? null : String.valueOf(value)) ? String.valueOf(value).trim() : fallback;
    }

    private String fallback(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String extractDomain(String url) {
        try {
            return fallback(new URI(url.trim()).getHost(), "未知");
        } catch (Exception e) {
            return "未知";
        }
    }
}
