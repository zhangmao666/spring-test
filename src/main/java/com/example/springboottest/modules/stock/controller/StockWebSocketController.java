package com.example.springboottest.modules.stock.controller;

import com.example.springboottest.modules.stock.dto.StockAnalysisDTO;
import com.example.springboottest.modules.stock.dto.StockQuoteDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StockWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public void pushQuote(String stockCode, StockQuoteDTO quote) {
        try {
            messagingTemplate.convertAndSend("/topic/stock/" + stockCode, quote);
            log.debug("推送股票行情: {} - {}", stockCode, quote.getCurrentPrice());
        } catch (Exception e) {
            log.error("推送股票行情失败: {}", stockCode, e);
        }
    }

    public void pushAnalysis(String stockCode, StockAnalysisDTO analysis) {
        try {
            messagingTemplate.convertAndSend("/topic/analysis/" + stockCode, analysis);
            log.info("推送AI分析: {} - {}", stockCode, analysis.getTrendType());
        } catch (Exception e) {
            log.error("推送AI分析失败: {}", stockCode, e);
        }
    }

    public void pushAlert(String userId, String message) {
        try {
            messagingTemplate.convertAndSend("/queue/alert/" + userId, message);
            log.info("推送价格提醒: userId={}", userId);
        } catch (Exception e) {
            log.error("推送价格提醒失败: userId={}", userId, e);
        }
    }
}
