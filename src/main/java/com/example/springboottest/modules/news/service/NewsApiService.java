package com.example.springboottest.modules.news.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class NewsApiService {

    private static final String API_KEY = "e563602155ff466f84794c76b19ba0ff";
    private static final String BASE_URL = "https://newsapi.org/v2";
    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public Map<String, Object> getTopHeadlines(String category, String q, int pageSize, int page) {
        StringBuilder url = new StringBuilder(BASE_URL + "/top-headlines?country=us");
        if (category != null && !category.isEmpty() && !category.equals("general")) {
            url.append("&category=").append(category);
        }
        if (q != null && !q.isEmpty()) {
            url.append("&q=").append(q);
        }
        url.append("&pageSize=").append(pageSize)
           .append("&page=").append(page)
           .append("&apiKey=").append(API_KEY);

        log.info("Fetching news from NewsAPI: category={}, q={}, page={}", category, q, page);
        try {
            return restTemplate.getForObject(url.toString(), Map.class);
        } catch (Exception e) {
            log.error("Failed to fetch news from NewsAPI: {}", e.getMessage());
            throw new RuntimeException("获取新闻失败: " + e.getMessage());
        }
    }
}
