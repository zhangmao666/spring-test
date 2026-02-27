package com.example.springboottest.modules.news.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboottest.modules.news.entity.DailyNews;
import com.example.springboottest.modules.news.entity.NewsSubscriber;
import com.example.springboottest.modules.news.repository.DailyNewsRepository;
import com.example.springboottest.modules.news.repository.NewsSubscriberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 每日资讯推送服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyNewsPushService {

    private final JavaMailSender mailSender;
    private final DailyNewsRepository dailyNewsRepository;
    private final NewsSubscriberRepository subscriberRepository;
    private final ObjectMapper objectMapper;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    /**
     * 获取今日热点资讯（从聚合数据API）
     */
    public List<DailyNews> fetchTodayNews() {
        List<DailyNews> newsList = new ArrayList<>();

        try {
            // 使用聚合数据的新闻头条API（需要申请API Key）
            // 这里使用免费的新闻API示例，你需要替换成实际的API
            String url = "https://v.juhe.cn/toutiao/index?type=top&key=YOUR_API_KEY";

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("获取新闻失败: HTTP {}", response.code());
                    return newsList;
                }

                String body = response.body().string();
                JsonNode root = objectMapper.readTree(body);

                if (root.has("result") && root.get("result").has("data")) {
                    JsonNode dataList = root.get("result").get("data");

                    for (JsonNode item : dataList) {
                        DailyNews news = DailyNews.builder()
                                .title(item.get("title").asText())
                                .content(item.has("content") ? item.get("content").asText() : "")
                                .source(item.has("author_name") ? item.get("author_name").asText() : "未知")
                                .url(item.has("url") ? item.get("url").asText() : "")
                                .category(item.has("category") ? item.get("category").asText() : "综合")
                                .publishTime(LocalDateTime.now())
                                .createTime(LocalDateTime.now())
                                .build();
                        newsList.add(news);
                    }
                }
            }

            // 保存到数据库
            if (!newsList.isEmpty()) {
                newsList.forEach(dailyNewsRepository::insert);
                log.info("获取并保存今日资讯成功: {} 条", newsList.size());
            }

        } catch (Exception e) {
            log.error("获取今日资讯失败: {}", e.getMessage(), e);
        }

        return newsList;
    }

    /**
     * 推送今日资讯到所有订阅用户
     */
    public void pushNewsToSubscribers() {
        log.info("开始推送每日资讯");

        // 获取今日资讯
        List<DailyNews> newsList = getTodayNews();

        if (newsList.isEmpty()) {
            log.warn("今日暂无资讯可推送");
            return;
        }

        // 获取所有活跃订阅用户
        List<NewsSubscriber> subscribers = subscriberRepository.selectList(
                new LambdaQueryWrapper<NewsSubscriber>()
                        .eq(NewsSubscriber::getIsActive, true)
        );

        if (subscribers.isEmpty()) {
            log.warn("暂无订阅用户");
            return;
        }

        // 生成邮件内容
        String emailContent = buildEmailContent(newsList);

        // 发送邮件给每个订阅用户
        int successCount = 0;
        for (NewsSubscriber subscriber : subscribers) {
            try {
                sendEmail(subscriber.getEmail(), subscriber.getUsername(), emailContent);
                successCount++;
                log.info("推送资讯成功: {}", subscriber.getEmail());
            } catch (Exception e) {
                log.error("推送资讯失败: {}, 错误: {}", subscriber.getEmail(), e.getMessage());
            }
        }

        log.info("每日资讯推送完成，成功 {}/{} 个用户", successCount, subscribers.size());
    }

    /**
     * 获取今日资讯（优先从数据库，没有则从API获取）
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

        // 如果数据库没有今日资讯，从API获取
        if (newsList.isEmpty()) {
            newsList = fetchTodayNews();
        }

        return newsList;
    }

    /**
     * 构建邮件HTML内容
     */
    private String buildEmailContent(List<DailyNews> newsList) {
        StringBuilder html = new StringBuilder();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));

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
            html.append("<div class='news-title'>").append(i + 1).append(". ").append(news.getTitle()).append("</div>");
            html.append("<div class='news-meta'>");
            html.append("来源: ").append(news.getSource());
            html.append(" | 分类: ").append(news.getCategory());
            html.append("</div>");
            if (news.getContent() != null && !news.getContent().isEmpty()) {
                html.append("<div class='news-content'>").append(news.getContent()).append("</div>");
            }
            if (news.getUrl() != null && !news.getUrl().isEmpty()) {
                html.append("<div style='margin-top: 10px;'>");
                html.append("<a href='").append(news.getUrl()).append("' style='color: #667eea;'>查看详情 →</a>");
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
     * 发送邮件
     */
    private void sendEmail(String toEmail, String username, String content) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("📰 每日热点资讯 - " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        helper.setText(content, true);

        mailSender.send(message);
    }

    /**
     * 添加订阅用户
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
        log.info("添加订阅用户成功: {}", email);
    }
}
