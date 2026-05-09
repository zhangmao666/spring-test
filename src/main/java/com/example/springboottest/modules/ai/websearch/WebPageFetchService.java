package com.example.springboottest.modules.ai.websearch;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebPageFetchService {

    private static final int MAX_RESPONSE_BYTES = 512 * 1024;
    private static final int MAX_VISIBLE_TEXT_LENGTH = 1500;
    private static final Set<String> DISALLOWED_HOST_SUFFIXES = new HashSet<>(Arrays.asList(
            ".local", ".internal", ".lan", ".home", ".corp"
    ));

    private final OkHttpClient okHttpClient;
    private final WebSearchProperties webSearchProperties;

    public FetchResult fetch(String url) {
        if (!isAllowedUrl(url)) {
            return FetchResult.rejected("URL is not allowed");
        }

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "AI-world/1.0")
                .get()
                .build();

        try (Response response = okHttpClient.newBuilder()
                .callTimeout(webSearchProperties.getSearxng().getTimeoutMs(), TimeUnit.MILLISECONDS)
                .build()
                .newCall(request)
                .execute()) {
            if (!response.isSuccessful()) {
                return FetchResult.failed("Fetch failed: " + response.code());
            }
            String contentType = response.header("Content-Type", "");
            if (!isAllowedContentType(contentType)) {
                return FetchResult.failed("Unsupported content type: " + contentType);
            }
            if (response.body() == null) {
                return FetchResult.failed("Empty response body");
            }
            String body = readBody(response.body().byteStream());
            String extracted = extractVisibleText(body, contentType);
            if (!StringUtils.hasText(extracted)) {
                return FetchResult.failed("No readable content extracted");
            }
            return FetchResult.success(truncate(extracted, MAX_VISIBLE_TEXT_LENGTH));
        } catch (Exception ex) {
            log.debug("Failed to fetch page: url={}, error={}", url, ex.getMessage());
            return FetchResult.failed(ex.getMessage());
        }
    }

    boolean isAllowedUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                return false;
            }
            if (!StringUtils.hasText(host)) {
                return false;
            }
            String lowerHost = host.toLowerCase(Locale.ROOT);
            if ("localhost".equals(lowerHost) || lowerHost.endsWith(".localhost")) {
                return false;
            }
            for (String suffix : DISALLOWED_HOST_SUFFIXES) {
                if (lowerHost.endsWith(suffix)) {
                    return false;
                }
            }
            if (isPrivateIpLiteral(lowerHost)) {
                return false;
            }
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                if (address.isAnyLocalAddress()
                        || address.isLoopbackAddress()
                        || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress()) {
                    return false;
                }
            }
            return true;
        } catch (URISyntaxException | IOException ex) {
            return false;
        }
    }

    private boolean isAllowedContentType(String contentType) {
        String normalized = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        return normalized.contains("text/html") || normalized.contains("text/plain");
    }

    private String readBody(InputStream inputStream) throws IOException {
        try (InputStream in = inputStream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int total = 0;
            int read;
            while ((read = in.read(buffer)) != -1) {
                int writable = Math.min(read, MAX_RESPONSE_BYTES - total);
                if (writable > 0) {
                    out.write(buffer, 0, writable);
                    total += writable;
                }
                if (total >= MAX_RESPONSE_BYTES) {
                    break;
                }
            }
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    private String extractVisibleText(String body, String contentType) {
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).contains("text/plain")) {
            return body;
        }
        Document document = Jsoup.parse(body);
        document.select("script,style,nav,footer,header,aside,noscript,form,svg,iframe").remove();
        Element mainContent = firstNonNull(document.selectFirst("article"), document.selectFirst("main"), document.body());
        return mainContent == null ? "" : mainContent.text();
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private boolean isPrivateIpLiteral(String host) {
        if (host.startsWith("127.") || host.startsWith("10.") || host.startsWith("192.168.")) {
            return true;
        }
        if (host.startsWith("169.254.") || host.startsWith("0.")) {
            return true;
        }
        return host.matches("^172\\.(1[6-9]|2\\d|3[0-1])\\..*")
                || "::1".equals(host)
                || host.startsWith("fe80:")
                || host.startsWith("fc")
                || host.startsWith("fd");
    }

    @Builder
    public record FetchResult(
            boolean success,
            boolean allowed,
            String content,
            String reason
    ) {
        public static FetchResult success(String content) {
            return FetchResult.builder().success(true).allowed(true).content(content).build();
        }

        public static FetchResult failed(String reason) {
            return FetchResult.builder().success(false).allowed(true).reason(reason).build();
        }

        public static FetchResult rejected(String reason) {
            return FetchResult.builder().success(false).allowed(false).reason(reason).build();
        }
    }
}
