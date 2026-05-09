package com.example.springboottest.modules.news.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboottest.modules.news.entity.DailyNews;
import com.example.springboottest.modules.news.entity.NewsSubscriber;
import com.example.springboottest.modules.news.repository.DailyNewsRepository;
import com.example.springboottest.modules.news.repository.NewsSubscriberRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Daily news push service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyNewsPushService {

    private static final int MAX_NEWS_PER_SOURCE = 8;
    private static final int MAX_TOTAL_NEWS = 50;
    private static final String RSS_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36";

    private static final DateTimeFormatter EMAIL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter EMAIL_CN_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    private static final List<RssSource> RSS_SOURCES = List.of(
            new RssSource("Google News", "https://news.google.com/rss/headlines/section/topic/NATION?hl=zh-CN&gl=CN&ceid=CN:zh-Hans", "国内"),
            new RssSource("Google News", "https://news.google.com/rss/headlines/section/topic/WORLD?hl=zh-CN&gl=CN&ceid=CN:zh-Hans", "国际"),
            new RssSource("Google News", "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=zh-CN&gl=CN&ceid=CN:zh-Hans", "科技"),
            new RssSource("Google News", "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=zh-CN&gl=CN&ceid=CN:zh-Hans", "财经"),
            new RssSource("Google News", "https://news.google.com/rss/headlines/section/topic/SPORTS?hl=zh-CN&gl=CN&ceid=CN:zh-Hans", "体育"),
            new RssSource("Google News", "https://news.google.com/rss/headlines/section/topic/ENTERTAINMENT?hl=zh-CN&gl=CN&ceid=CN:zh-Hans", "娱乐")
    );

    private final JavaMailSender mailSender;
    private final DailyNewsRepository dailyNewsRepository;
    private final NewsSubscriberRepository subscriberRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    /**
     * Fetch latest news from free RSS sources.
     */
    public List<DailyNews> fetchTodayNews() {
        List<DailyNews> mergedNews = new ArrayList<>();
        Set<String> seenTitles = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();

        for (RssSource source : RSS_SOURCES) {
            List<DailyNews> sourceNews = fetchFromRssSource(source, now);
            for (DailyNews news : sourceNews) {
                String normalizedTitle = normalizeTitle(news.getTitle());
                if (normalizedTitle.isEmpty() || seenTitles.contains(normalizedTitle)) {
                    continue;
                }
                seenTitles.add(normalizedTitle);
                mergedNews.add(news);
                if (mergedNews.size() >= MAX_TOTAL_NEWS) {
                    break;
                }
            }
            if (mergedNews.size() >= MAX_TOTAL_NEWS) {
                break;
            }
        }

        if (mergedNews.isEmpty()) {
            log.warn("No news fetched from free RSS sources");
            return mergedNews;
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        Set<String> existingTitles = dailyNewsRepository.selectList(
                        new LambdaQueryWrapper<DailyNews>()
                                .ge(DailyNews::getCreateTime, startOfDay)
                                .lt(DailyNews::getCreateTime, endOfDay)
                                .select(DailyNews::getTitle)
                ).stream()
                .map(DailyNews::getTitle)
                .map(this::normalizeTitle)
                .filter(title -> !title.isEmpty())
                .collect(Collectors.toSet());

        List<DailyNews> toInsert = mergedNews.stream()
                .filter(news -> !existingTitles.contains(normalizeTitle(news.getTitle())))
                .collect(Collectors.toList());

        if (!toInsert.isEmpty()) {
            toInsert.forEach(dailyNewsRepository::insert);
        }

        log.info("Fetched {} free-news items, inserted {} new records", mergedNews.size(), toInsert.size());
        return mergedNews;
    }

    /**
     * Push today's news to all subscribers.
     */
    public void pushNewsToSubscribers() {
        log.info("Start pushing daily news");

        List<DailyNews> newsList = getTodayNews();
        if (newsList.isEmpty()) {
            log.warn("No news available for today");
            return;
        }

        List<NewsSubscriber> subscribers = subscriberRepository.selectList(
                new LambdaQueryWrapper<NewsSubscriber>()
                        .eq(NewsSubscriber::getIsActive, true)
        );

        if (subscribers.isEmpty()) {
            log.warn("No active subscribers");
            return;
        }

        String emailContent = buildEmailContent(newsList);
        int successCount = 0;

        for (NewsSubscriber subscriber : subscribers) {
            try {
                sendEmail(subscriber.getEmail(), subscriber.getUsername(), emailContent);
                successCount++;
                log.info("Daily news sent: {}", subscriber.getEmail());
            } catch (Exception e) {
                log.error("Daily news send failed: {}, reason: {}", subscriber.getEmail(), e.getMessage());
            }
        }

        log.info("Daily news push completed: success {}/{}", successCount, subscribers.size());
    }

    /**
     * Build preview HTML without sending emails.
     */
    public String previewTodayNewsHtml(int limit) {
        List<DailyNews> newsList = getTodayNews();
        if (newsList.isEmpty()) {
            newsList = fetchTodayNews();
        }
        if (newsList.isEmpty()) {
            return "<p>今日暂无可预览的新闻内容。</p>";
        }
        return buildEmailContent(newsList.stream().limit(Math.max(1, limit)).toList());
    }

    /**
     * Get today's news. Fetch from RSS when DB has no records.
     */
    private List<DailyNews> getTodayNews() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        List<DailyNews> newsList = dailyNewsRepository.selectList(
                new LambdaQueryWrapper<DailyNews>()
                        .ge(DailyNews::getCreateTime, startOfDay)
                        .lt(DailyNews::getCreateTime, endOfDay)
                        .orderByDesc(DailyNews::getPublishTime)
                        .last("LIMIT 10")
        );

        if (newsList.isEmpty()) {
            newsList = fetchTodayNews();
        }

        return newsList;
    }

    /**
     * Build HTML email content.
     */
    private String buildEmailContent(List<DailyNews> newsList) {
        StringBuilder html = new StringBuilder();
        String today = LocalDate.now().format(EMAIL_CN_DATE_FORMAT);

        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 800px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }");
        html.append(".news-item { background: #f9f9f9; margin: 15px 0; padding: 20px; border-left: 4px solid #667eea; border-radius: 5px; }");
        html.append(".news-title { font-size: 18px; font-weight: bold; color: #333; margin-bottom: 10px; }");
        html.append(".news-meta { font-size: 12px; color: #999; margin-bottom: 10px; }");
        html.append(".news-content { font-size: 14px; color: #666; line-height: 1.8; }");
        html.append(".footer { text-align: center; padding: 20px; color: #999; font-size: 12px; }");
        html.append("</style></head><body>");

        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>📰 每日热点资讯</h1>");
        html.append("<p>").append(today).append("</p>");
        html.append("</div>");

        for (int i = 0; i < newsList.size(); i++) {
            DailyNews news = newsList.get(i);
            html.append("<div class='news-item'>");
            html.append("<div class='news-title'>").append(i + 1).append(". ").append(escapeHtml(news.getTitle())).append("</div>");
            html.append("<div class='news-meta'>");
            html.append("来源: ").append(escapeHtml(news.getSource()));
            html.append(" | 分类: ").append(escapeHtml(news.getCategory()));
            html.append("</div>");
            if (news.getContent() != null && !news.getContent().isEmpty()) {
                html.append("<div class='news-content'>").append(escapeHtml(news.getContent())).append("</div>");
            }
            if (news.getUrl() != null && !news.getUrl().isEmpty()) {
                html.append("<div style='margin-top: 10px;'>");
                html.append("<a href='").append(escapeHtml(news.getUrl())).append("' style='color: #667eea;'>查看详情 →</a>");
                html.append("</div>");
            }
            html.append("</div>");
        }

        html.append("<div class='footer'>");
        html.append("<p>本邮件由系统自动发送，请勿回复</p>");
        html.append("<p>如需取消订阅，请联系管理员</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Send email.
     */
    private void sendEmail(String toEmail, String username, String content) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("📰 每日热点资讯 - " + LocalDate.now().format(EMAIL_DATE_FORMAT));
        helper.setText(content, true);

        mailSender.send(message);
    }

    /**
     * Add a subscriber.
     */
    public void addSubscriber(String email, String username) {
        NewsSubscriber subscriber = NewsSubscriber.builder()
                .email(email)
                .username(username)
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        subscriberRepository.insert(subscriber);
        log.info("Subscriber added: {}", email);
    }

    private List<DailyNews> fetchFromRssSource(RssSource source, LocalDateTime now) {
        Request request = new Request.Builder()
                .url(source.url())
                .addHeader("User-Agent", RSS_USER_AGENT)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.warn("RSS request failed: source={}, url={}, code={}", source.sourceName(), source.url(), response.code());
                return List.of();
            }

            String xml = response.body().string();
            return parseRss(xml, source, now);
        } catch (Exception e) {
            log.warn("RSS fetch failed: source={}, url={}, reason={}", source.sourceName(), source.url(), e.getMessage());
            return List.of();
        }
    }

    private List<DailyNews> parseRss(String xml, RssSource source, LocalDateTime now) throws Exception {
        List<DailyNews> newsList = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        setFactoryFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setFactoryFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
        setFactoryFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        NodeList items = document.getElementsByTagName("item");
        int count = Math.min(items.getLength(), MAX_NEWS_PER_SOURCE);

        for (int i = 0; i < count; i++) {
            Element item = (Element) items.item(i);

            String title = textOf(item, "title");
            if (title.isBlank()) {
                continue;
            }

            String description = normalizeDescription(textOf(item, "description"));
            String link = textOf(item, "link");
            String pubDate = textOf(item, "pubDate");
            LocalDateTime publishTime = parsePublishTime(pubDate, now);

            DailyNews news = DailyNews.builder()
                    .title(title)
                    .content(description)
                    .source(source.sourceName())
                    .url(link)
                    .category(source.category())
                    .publishTime(publishTime)
                    .createTime(now)
                    .build();

            newsList.add(news);
        }

        return newsList;
    }

    private void setFactoryFeature(DocumentBuilderFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
        } catch (Exception ignored) {
            // Ignore unsupported feature on some JDK parsers.
        }
    }

    private String textOf(Element element, String tagName) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list.getLength() == 0 || list.item(0) == null) {
            return "";
        }
        return Objects.toString(list.item(0).getTextContent(), "").trim();
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return "";
        }

        String plain = description
                .replaceAll("<[^>]*>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("\\s+", " ")
                .trim();

        if (plain.length() > 280) {
            return plain.substring(0, 280) + "...";
        }
        return plain;
    }

    private LocalDateTime parsePublishTime(String rawDate, LocalDateTime fallback) {
        if (rawDate == null || rawDate.isBlank()) {
            return fallback;
        }

        try {
            return ZonedDateTime.parse(rawDate, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // Continue with fallback formats.
        }

        try {
            return LocalDateTime.parse(rawDate, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            // Keep fallback value.
        }

        return fallback;
    }

    private String normalizeTitle(String title) {
        if (title == null) {
            return "";
        }
        return title.trim().toLowerCase();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private record RssSource(String sourceName, String url, String category) {
    }
}
