package com.example.springboottest.modules.ai.websearch;

import com.example.springboottest.modules.ai.dto.SearchStatus;
import com.example.springboottest.modules.ai.dto.WebSearchSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSearchService {

    private static final int MAX_QUERY_LENGTH = 200;
    private static final int MAX_CONTEXT_LENGTH = 4500;
    private static final int MAX_SNIPPET_LENGTH = 1500;
    private static final List<String> NEWS_HINTS = List.of(
            "最近", "最新", "今天", "今日", "近期", "当前", "目前", "近况", "动态", "进展",
            "新闻", "热点", "局势", "冲突", "战争", "股价", "汇率", "政策", "消息", "局面",
            "latest", "recent", "current", "news", "headline", "headlines", "update", "updates"
    );
    private static final List<String> QUESTION_NOISE = List.of(
            "请问", "麻烦", "帮我", "告诉我", "一下", "如何", "怎么样", "怎么", "是什么", "什么情况", "吗", "呢", "呀"
    );
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern PUNCTUATION = Pattern.compile("[\\p{Punct}\\p{IsPunctuation}，。！？；：、“”‘’（）()【】\\[\\]<>《》]+");

    private final WebSearchProperties webSearchProperties;
    private final SearxngClient searxngClient;
    private final WebPageFetchService webPageFetchService;

    public WebSearchContext prepareContext(boolean useWebSearch, String userMessage) {
        if (!useWebSearch) {
            return emptyContext();
        }

        SearchPlan searchPlan = buildSearchPlan(userMessage);
        String query = searchPlan.query();
        if (!webSearchProperties.isWebSearchEnabled()) {
            return buildFailure(query, SearchStatus.FALLBACK_ERROR, "Web search is not configured");
        }

        try {
            List<SearxngClient.SearxngResult> rawResults = searxngClient.search(query, searchPlan.categories(), searchPlan.timeRange());
            List<SearxngClient.SearxngResult> deduplicated = deduplicate(rawResults);
            if (deduplicated.isEmpty()) {
                return WebSearchContext.builder()
                        .requested(true)
                        .success(false)
                        .searchQuery(query)
                        .searchStatus(SearchStatus.FALLBACK_NO_RESULT)
                        .sources(List.of())
                        .build();
            }

            List<WebSearchSource> sources = collectSources(deduplicated);
            if (sources.isEmpty()) {
                return WebSearchContext.builder()
                        .requested(true)
                        .success(false)
                        .searchQuery(query)
                        .searchStatus(SearchStatus.FALLBACK_NO_RESULT)
                        .sources(List.of())
                        .build();
            }

            return WebSearchContext.builder()
                    .requested(true)
                    .success(true)
                    .searchQuery(query)
                    .searchStatus(SearchStatus.SUCCESS)
                    .sources(sources)
                    .promptContext(buildPromptContext(query, sources))
                    .build();
        } catch (Exception ex) {
            log.warn("Web search failed: {}", ex.getMessage());
            return buildFailure(query, SearchStatus.FALLBACK_ERROR, ex.getMessage());
        }
    }

    public boolean isEnabled() {
        return webSearchProperties.isWebSearchEnabled();
    }

    private WebSearchContext emptyContext() {
        return WebSearchContext.builder()
                .requested(false)
                .success(false)
                .searchStatus(SearchStatus.NOT_REQUESTED)
                .sources(List.of())
                .build();
    }

    private WebSearchContext buildFailure(String query, SearchStatus status, String errorMessage) {
        return WebSearchContext.builder()
                .requested(true)
                .success(false)
                .abortChat(!webSearchProperties.isFallbackToChatWithoutSearch())
                .errorMessage(errorMessage)
                .searchQuery(query)
                .searchStatus(status)
                .sources(List.of())
                .build();
    }

    private String normalizeQuery(String userMessage) {
        String trimmed = StringUtils.hasText(userMessage) ? userMessage.trim() : "";
        if (trimmed.length() <= MAX_QUERY_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_QUERY_LENGTH);
    }

    static String buildSearchQuery(String userMessage) {
        String normalized = WHITESPACE.matcher(PUNCTUATION.matcher(StringUtils.hasText(userMessage) ? userMessage.trim() : "")
                .replaceAll(" ")).replaceAll(" ").trim();
        if (!StringUtils.hasText(normalized)) {
            return "";
        }
        if (!looksLikeNewsSearch(normalized)) {
            return normalized;
        }

        String rewritten = normalized;
        for (String noise : QUESTION_NOISE) {
            rewritten = rewritten.replace(noise, " ");
        }
        rewritten = rewritten.replace("最近", " ");
        rewritten = rewritten.replace("最新", " ");
        rewritten = rewritten.replace("今天", " ");
        rewritten = rewritten.replace("今日", " ");
        rewritten = rewritten.replace("近期", " ");
        rewritten = rewritten.replace("当前", " ");
        rewritten = rewritten.replace("目前", " ");
        rewritten = rewritten.replace("近况", " ");
        rewritten = WHITESPACE.matcher(rewritten).replaceAll(" ").trim();
        if (!StringUtils.hasText(rewritten)) {
            rewritten = normalized;
        }

        if (containsChinese(rewritten) && !rewritten.contains(" ")) {
            rewritten = rewritten.replace("局势", " 局势 ")
                    .replace("新闻", " 新闻 ")
                    .replace("动态", " 动态 ")
                    .replace("进展", " 进展 ")
                    .replace("冲突", " 冲突 ")
                    .replace("战争", " 战争 ")
                    .replace("消息", " 消息 ");
            rewritten = WHITESPACE.matcher(rewritten).replaceAll(" ").trim();
        }

        if (containsChinese(rewritten) && !rewritten.contains("最新") && !rewritten.contains("新闻")) {
            rewritten = rewritten + " 最新 新闻";
        }
        return normalizeLength(rewritten);
    }

    static String inferSearchCategories(String userMessage) {
        return looksLikeNewsSearch(userMessage) ? "news,general" : null;
    }

    static String inferTimeRange(String userMessage) {
        String normalized = StringUtils.hasText(userMessage) ? userMessage : "";
        if (normalized.contains("今天") || normalized.contains("今日") || normalized.contains("刚刚")) {
            return "day";
        }
        return looksLikeNewsSearch(normalized) ? "month" : null;
    }

    private static boolean looksLikeNewsSearch(String userMessage) {
        if (!StringUtils.hasText(userMessage)) {
            return false;
        }
        return NEWS_HINTS.stream().anyMatch(userMessage::contains);
    }

    private static String normalizeLength(String query) {
        if (!StringUtils.hasText(query) || query.length() <= MAX_QUERY_LENGTH) {
            return query;
        }
        return query.substring(0, MAX_QUERY_LENGTH);
    }

    private static boolean containsChinese(String value) {
        return StringUtils.hasText(value) && value.codePoints().anyMatch(codePoint -> Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN);
    }

    private SearchPlan buildSearchPlan(String userMessage) {
        return new SearchPlan(
                buildSearchQuery(userMessage),
                inferSearchCategories(userMessage),
                inferTimeRange(userMessage)
        );
    }

    private List<SearxngClient.SearxngResult> deduplicate(List<SearxngClient.SearxngResult> results) {
        Map<String, SearxngClient.SearxngResult> deduplicated = new LinkedHashMap<>();
        int limit = Math.max(1, webSearchProperties.getSearxng().getMaxResults());
        for (SearxngClient.SearxngResult result : results) {
            if (deduplicated.size() >= limit) {
                break;
            }
            String key = normalizeUrl(result.url());
            if (!StringUtils.hasText(key) || deduplicated.containsKey(key)) {
                continue;
            }
            deduplicated.put(key, result);
        }
        return new ArrayList<>(deduplicated.values());
    }

    private List<WebSearchSource> collectSources(List<SearxngClient.SearxngResult> results) {
        List<WebSearchSource> sources = new ArrayList<>();
        int fetchTopN = Math.max(0, webSearchProperties.getSearxng().getFetchTopN());
        for (int i = 0; i < results.size(); i++) {
            SearxngClient.SearxngResult result = results.get(i);
            String snippet = truncate(result.content(), MAX_SNIPPET_LENGTH);
            boolean fetched = false;
            if (i < fetchTopN) {
                WebPageFetchService.FetchResult fetchResult = webPageFetchService.fetch(result.url());
                if (fetchResult.success() && StringUtils.hasText(fetchResult.content())) {
                    snippet = fetchResult.content();
                    fetched = true;
                }
            }
            if (!StringUtils.hasText(snippet)) {
                continue;
            }
            sources.add(WebSearchSource.builder()
                    .title(defaultIfBlank(result.title(), result.url()))
                    .url(result.url())
                    .domain(extractDomain(result.url()))
                    .snippet(snippet)
                    .fetched(fetched)
                    .build());
        }
        return sources;
    }

    private String buildPromptContext(String query, List<WebSearchSource> sources) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are given real-time web search context.\n");
        builder.append("Use the sources below as your primary basis for factual claims.\n");
        builder.append("If the sources are insufficient, say so clearly.\n");
        builder.append("Do not invent unsupported latest facts.\n\n");
        builder.append("Search query: ").append(query).append("\n\n");
        for (int i = 0; i < sources.size(); i++) {
            WebSearchSource source = sources.get(i);
            builder.append("Source ").append(i + 1).append(":\n");
            builder.append("Title: ").append(defaultIfBlank(source.getTitle(), "Untitled")).append("\n");
            builder.append("URL: ").append(defaultIfBlank(source.getUrl(), "")).append("\n");
            builder.append("Snippet: ").append(defaultIfBlank(source.getSnippet(), "")).append("\n\n");
            if (builder.length() >= MAX_CONTEXT_LENGTH) {
                break;
            }
        }
        return builder.length() > MAX_CONTEXT_LENGTH ? builder.substring(0, MAX_CONTEXT_LENGTH) : builder.toString();
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private String normalizeUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        return url.trim().toLowerCase(Locale.ROOT);
    }

    private String extractDomain(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception ex) {
            return null;
        }
    }

    private record SearchPlan(
            String query,
            String categories,
            String timeRange
    ) {
    }
}
