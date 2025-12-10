package com.example.springboottest.modules.stock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "stock")
public class StockProperties {

    private DataConfig data = new DataConfig();
    private WebSocketConfig websocket = new WebSocketConfig();
    private AnalysisConfig analysis = new AnalysisConfig();

    @Data
    public static class DataConfig {
        private String source = "sina";
        private Integer fetchInterval = 5;
        private TradingHours tradingHours = new TradingHours();
        private Boolean scheduleEnabled = true;
    }

    @Data
    public static class TradingHours {
        private String start = "09:30";
        private String end = "15:00";
    }

    @Data
    public static class WebSocketConfig {
        private String allowedOrigins = "*";
        private Integer messageSizeLimit = 128;
    }

    @Data
    public static class AnalysisConfig {
        private Integer interval = 15;
        private String provider = "qwen";
        private Integer historyWindow = 7;
        private Boolean enabled = true;
    }
}
