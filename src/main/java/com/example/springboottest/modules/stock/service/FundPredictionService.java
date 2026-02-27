package com.example.springboottest.modules.stock.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboottest.entity.DTO.AiChatRequest;
import com.example.springboottest.entity.DTO.AiChatResponse;
import com.example.springboottest.modules.ai.service.AiChatService;
import com.example.springboottest.modules.stock.dto.FundPredictionDTO;
import com.example.springboottest.modules.stock.entity.FundHistory;
import com.example.springboottest.modules.stock.repository.FundHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基金AI预测服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FundPredictionService {

    private final AiChatService aiChatService;
    private final FundHistoryRepository fundHistoryRepository;
    private final ObjectMapper objectMapper;

    /**
     * 预测基金走势
     */
    public FundPredictionDTO predictFundTrend(String fundCode) {
        try {
            // 1. 获取最近30天的历史数据
            LocalDate startDate = LocalDate.now().minusDays(30);
            List<FundHistory> historyList = fundHistoryRepository.selectList(
                    new LambdaQueryWrapper<FundHistory>()
                            .eq(FundHistory::getFundCode, fundCode)
                            .ge(FundHistory::getTradeDate, startDate)
                            .orderByDesc(FundHistory::getTradeDate)
                            .last("LIMIT 30")
            );

            if (historyList.isEmpty()) {
                log.warn("基金 {} 没有历史数据，无法预测", fundCode);
                return FundPredictionDTO.builder()
                        .fundCode(fundCode)
                        .trendAnalysis("暂无历史数据")
                        .predictedDirection("UNKNOWN")
                        .confidence(0)
                        .riskLevel("UNKNOWN")
                        .recommendation("请先获取基金历史数据")
                        .build();
            }

            String fundName = historyList.get(0).getFundName();

            // 2. 构建历史数据摘要
            String historySummary = buildHistorySummary(historyList);

            // 3. 调用AI进行预测
            String prompt = buildPredictionPrompt(fundCode, fundName, historySummary);

            AiChatRequest request = new AiChatRequest();
            request.setMessage(prompt);
            request.setTemperature(0.3);
            request.setMaxTokens(1000);

            log.info("正在调用AI预测基金走势: {}", fundCode);
            AiChatResponse response = aiChatService.chat(request).get();

            if (response.isSuccess()) {
                // 4. 解析AI响应
                return parseAiPrediction(fundCode, fundName, response.getMessage());
            } else {
                log.error("AI预测失败: {}", response.getError());
                return buildErrorPrediction(fundCode, fundName, "AI服务调用失败");
            }

        } catch (Exception e) {
            log.error("预测基金走势异常: {}", e.getMessage(), e);
            return buildErrorPrediction(fundCode, "", "预测服务异常: " + e.getMessage());
        }
    }

    /**
     * 构建历史数据摘要
     */
    private String buildHistorySummary(List<FundHistory> historyList) {
        StringBuilder summary = new StringBuilder();
        summary.append("最近30个交易日净值数据：\n");

        // 倒序排列（从旧到新）
        List<FundHistory> sortedList = historyList.stream()
                .sorted((a, b) -> a.getTradeDate().compareTo(b.getTradeDate()))
                .collect(Collectors.toList());

        for (FundHistory history : sortedList) {
            summary.append(String.format("%s: 净值=%.4f, 涨跌幅=%.2f%%\n",
                    history.getTradeDate(),
                    history.getNetValue(),
                    history.getChangePercent()));
        }

        // 计算统计数据
        double avgChange = sortedList.stream()
                .mapToDouble(FundHistory::getChangePercent)
                .average()
                .orElse(0.0);

        double maxValue = sortedList.stream()
                .mapToDouble(FundHistory::getNetValue)
                .max()
                .orElse(0.0);

        double minValue = sortedList.stream()
                .mapToDouble(FundHistory::getNetValue)
                .min()
                .orElse(0.0);

        double volatility = (maxValue - minValue) / minValue * 100;

        summary.append(String.format("\n统计数据：\n"));
        summary.append(String.format("- 平均日涨跌幅: %.2f%%\n", avgChange));
        summary.append(String.format("- 期间最高净值: %.4f\n", maxValue));
        summary.append(String.format("- 期间最低净值: %.4f\n", minValue));
        summary.append(String.format("- 波动率: %.2f%%\n", volatility));

        return summary.toString();
    }

    /**
     * 构建AI预测提示词
     */
    private String buildPredictionPrompt(String fundCode, String fundName, String historySummary) {
        return String.format("""
                你是一位专业的基金分析师，请基于以下历史数据对基金进行走势预测和分析。

                基金信息：
                - 基金代码: %s
                - 基金名称: %s

                %s

                请按照以下JSON格式返回分析结果（不要使用markdown代码块，直接返回JSON）：
                {
                  "trendAnalysis": "对近期走势的简要分析（50字以内）",
                  "predictedDirection": "预测方向（UP/DOWN/STABLE）",
                  "confidence": 置信度（0-100的整数）,
                  "predictedChange": 预测涨跌幅（百分比，可为负数）,
                  "riskLevel": "风险等级（LOW/MEDIUM/HIGH）",
                  "recommendation": "投资建议（100字以内）",
                  "detailedAnalysis": "详细分析（包括技术面、波动性、风险提示等，200字以内）"
                }

                注意：
                1. 预测要基于历史数据的趋势和波动性
                2. 置信度要根据数据的稳定性和趋势明确性来判断
                3. 风险等级要考虑波动率和回撤情况
                4. 投资建议要客观、谨慎，提醒投资风险
                """, fundCode, fundName, historySummary);
    }

    /**
     * 解析AI预测结果
     */
    private FundPredictionDTO parseAiPrediction(String fundCode, String fundName, String aiResponse) {
        try {
            // 提取JSON
            String jsonStr = extractJson(aiResponse);

            // 解析JSON
            var jsonNode = objectMapper.readTree(jsonStr);

            return FundPredictionDTO.builder()
                    .fundCode(fundCode)
                    .fundName(fundName)
                    .trendAnalysis(jsonNode.get("trendAnalysis").asText())
                    .predictedDirection(jsonNode.get("predictedDirection").asText())
                    .confidence(jsonNode.get("confidence").asInt())
                    .predictedChange(jsonNode.get("predictedChange").asDouble())
                    .riskLevel(jsonNode.get("riskLevel").asText())
                    .recommendation(jsonNode.get("recommendation").asText())
                    .detailedAnalysis(jsonNode.get("detailedAnalysis").asText())
                    .build();

        } catch (Exception e) {
            log.error("解析AI预测结果失败: {}", e.getMessage());
            // 如果解析失败，返回原始文本
            return FundPredictionDTO.builder()
                    .fundCode(fundCode)
                    .fundName(fundName)
                    .trendAnalysis("AI分析")
                    .predictedDirection("UNKNOWN")
                    .confidence(50)
                    .predictedChange(0.0)
                    .riskLevel("MEDIUM")
                    .recommendation(aiResponse)
                    .detailedAnalysis(aiResponse)
                    .build();
        }
    }

    /**
     * 提取JSON字符串
     */
    private String extractJson(String content) {
        if (content == null || content.isEmpty()) {
            return "{}";
        }

        // 移除markdown代码块
        if (content.contains("```json")) {
            int start = content.indexOf("```json") + 7;
            int end = content.indexOf("```", start);
            if (end > start) {
                return content.substring(start, end).trim();
            }
        } else if (content.contains("```")) {
            int start = content.indexOf("```") + 3;
            int end = content.indexOf("```", start);
            if (end > start) {
                return content.substring(start, end).trim();
            }
        }

        // 查找JSON对象
        int firstBrace = content.indexOf("{");
        int lastBrace = content.lastIndexOf("}");
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return content.substring(firstBrace, lastBrace + 1).trim();
        }

        return content.trim();
    }

    /**
     * 构建错误预测结果
     */
    private FundPredictionDTO buildErrorPrediction(String fundCode, String fundName, String errorMsg) {
        return FundPredictionDTO.builder()
                .fundCode(fundCode)
                .fundName(fundName)
                .trendAnalysis("预测失败")
                .predictedDirection("UNKNOWN")
                .confidence(0)
                .predictedChange(0.0)
                .riskLevel("UNKNOWN")
                .recommendation(errorMsg)
                .detailedAnalysis(errorMsg)
                .build();
    }
}
