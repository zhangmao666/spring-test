package com.example.springboottest.modules.stock.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboottest.modules.stock.dto.FundAnalysisDTO;
import com.example.springboottest.modules.stock.entity.FundHistory;
import com.example.springboottest.modules.stock.repository.FundHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 基金AI实时走势分析服务
 * 支持流式输出，实时展示AI分析过程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FundAnalysisService {

    private final FundHistoryRepository fundHistoryRepository;
    private final OpenAiChatModel defaultOpenAiChatModel;
    private final ObjectMapper objectMapper;
    private final FundTrendService fundTrendService;

    /**
     * 获取基金走势摘要（用于页面渲染基础信息）
     */
    public FundAnalysisDTO getTrendSummary(String fundCode, Integer days) {
        if (days == null || days <= 0) days = 90;

        LocalDate startDate = LocalDate.now().minusDays(days);
        List<FundHistory> historyList = fundHistoryRepository.selectList(
                new LambdaQueryWrapper<FundHistory>()
                        .eq(FundHistory::getFundCode, fundCode)
                        .ge(FundHistory::getTradeDate, startDate)
                        .orderByAsc(FundHistory::getTradeDate)
        );

        // 如果数据不足，尝试从API获取
        if (historyList.isEmpty() || historyList.size() < days / 3) {
            log.info("数据不足，从API获取基金 {} 历史数据", fundCode);
            fundTrendService.fetchAndSaveFundHistory(fundCode, days);

            historyList = fundHistoryRepository.selectList(
                    new LambdaQueryWrapper<FundHistory>()
                            .eq(FundHistory::getFundCode, fundCode)
                            .ge(FundHistory::getTradeDate, startDate)
                            .orderByAsc(FundHistory::getTradeDate)
            );
        }

        if (historyList.isEmpty()) {
            return FundAnalysisDTO.builder()
                    .fundCode(fundCode)
                    .fundName("未知基金")
                    .build();
        }

        String fundName = historyList.get(0).getFundName();

        // 构建走势摘要
        double avgChange = historyList.stream()
                .mapToDouble(FundHistory::getChangePercent)
                .average().orElse(0.0);
        double maxValue = historyList.stream()
                .mapToDouble(FundHistory::getNetValue)
                .max().orElse(0.0);
        double minValue = historyList.stream()
                .mapToDouble(FundHistory::getNetValue)
                .min().orElse(0.0);
        double volatility = minValue > 0 ? (maxValue - minValue) / minValue * 100 : 0;

        FundHistory latest = historyList.get(historyList.size() - 1);
        FundHistory first = historyList.get(0);
        double totalReturn = first.getNetValue() > 0
                ? (latest.getNetValue() - first.getNetValue()) / first.getNetValue() * 100 : 0;

        // 最近10个交易日的数据点
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<FundAnalysisDTO.DailyPoint> recentPoints = historyList.stream()
                .skip(Math.max(0, historyList.size() - 10))
                .map(h -> FundAnalysisDTO.DailyPoint.builder()
                        .date(h.getTradeDate().format(fmt))
                        .netValue(h.getNetValue())
                        .changePercent(h.getChangePercent())
                        .build())
                .collect(Collectors.toList());

        FundAnalysisDTO.TrendSummary summary = FundAnalysisDTO.TrendSummary.builder()
                .latestNetValue(latest.getNetValue())
                .latestChange(latest.getChangePercent())
                .avgChange(avgChange)
                .maxValue(maxValue)
                .minValue(minValue)
                .volatility(volatility)
                .totalReturn(totalReturn)
                .dataPoints(historyList.size())
                .startDate(first.getTradeDate())
                .endDate(latest.getTradeDate())
                .recentPoints(recentPoints)
                .build();

        return FundAnalysisDTO.builder()
                .fundCode(fundCode)
                .fundName(fundName)
                .trendSummary(summary)
                .build();
    }

    /**
     * 流式AI分析基金走势
     * 通过SSE实时推送分析内容
     */
    public void analyzeStream(String fundCode, Integer days, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            try {
                doStreamAnalysis(fundCode, days, emitter);
            } catch (Exception e) {
                log.error("流式分析失败: fundCode={}", fundCode, e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"error\":\"分析失败: " + e.getMessage() + "\"}"));
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error("发送错误消息失败", ex);
                }
            }
        });
    }

    /**
     * 执行流式分析
     */
    private void doStreamAnalysis(String fundCode, Integer days, SseEmitter emitter) {
        if (days == null || days <= 0) days = 90;

        // 1. 获取历史数据
        LocalDate startDate = LocalDate.now().minusDays(days);
        List<FundHistory> historyList = fundHistoryRepository.selectList(
                new LambdaQueryWrapper<FundHistory>()
                        .eq(FundHistory::getFundCode, fundCode)
                        .ge(FundHistory::getTradeDate, startDate)
                        .orderByAsc(FundHistory::getTradeDate)
        );

        if (historyList.isEmpty()) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"error\":\"暂无历史数据，请先在走势页面刷新数据\"}"));
                emitter.complete();
            } catch (Exception e) {
                log.error("发送消息失败", e);
            }
            return;
        }

        String fundName = historyList.get(0).getFundName();

        // 2. 构建分析提示词
        String historySummary = buildDetailedHistorySummary(historyList);
        String prompt = buildAnalysisPrompt(fundCode, fundName, historySummary, days);

        // 3. 发送开始事件
        try {
            emitter.send(SseEmitter.event()
                    .name("start")
                    .data("{\"fundCode\":\"" + fundCode + "\",\"fundName\":\"" + fundName + "\"}"));
        } catch (Exception e) {
            log.error("发送开始事件失败", e);
            return;
        }

        // 4. 调用AI流式接口
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(
                    "你是一位资深的基金投资分析师，擅长通过技术分析和数据解读来评估基金走势。" +
                    "请用专业但通俗易懂的语言进行分析，适当使用emoji让分析更生动。" +
                    "分析要有条理性，使用markdown格式组织内容。"
            ));
            messages.add(new UserMessage(prompt));

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .temperature(0.4)
                    .maxTokens(2000)
                    .build();

            Prompt aiPrompt = new Prompt(messages, options);
            Flux<ChatResponse> flux = defaultOpenAiChatModel.stream(aiPrompt);

            StringBuilder fullContent = new StringBuilder();

            flux.subscribe(
                    chatResponse -> {
                        try {
                            String content = chatResponse.getResult().getOutput().getText();
                            if (content != null && !content.isEmpty()) {
                                fullContent.append(content);
                                // 发送内容片段
                                Map<String, String> data = new HashMap<>();
                                data.put("content", content);
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(objectMapper.writeValueAsString(data)));
                            }
                        } catch (Exception e) {
                            log.error("发送流式消息失败", e);
                        }
                    },
                    error -> {
                        log.error("AI流式分析失败", error);
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data("{\"error\":\"AI分析中断: " + error.getMessage() + "\"}"));
                            emitter.completeWithError(error);
                        } catch (Exception ex) {
                            log.error("发送错误消息失败", ex);
                        }
                    },
                    () -> {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data("{\"status\":\"completed\",\"totalLength\":" + fullContent.length() + "}"));
                            emitter.complete();
                            log.info("基金 {} AI走势分析完成，内容长度: {}", fundCode, fullContent.length());
                        } catch (Exception e) {
                            log.error("完成流式响应失败", e);
                        }
                    }
            );
        } catch (Exception e) {
            log.error("AI流式分析异常", e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"error\":\"AI服务异常: " + e.getMessage() + "\"}"));
                emitter.completeWithError(e);
            } catch (Exception ex) {
                log.error("发送错误消息失败", ex);
            }
        }
    }

    /**
     * 构建详细的历史数据摘要
     */
    private String buildDetailedHistorySummary(List<FundHistory> historyList) {
        StringBuilder summary = new StringBuilder();

        // 基础统计数据
        double avgChange = historyList.stream()
                .mapToDouble(FundHistory::getChangePercent)
                .average().orElse(0.0);
        double maxValue = historyList.stream()
                .mapToDouble(FundHistory::getNetValue)
                .max().orElse(0.0);
        double minValue = historyList.stream()
                .mapToDouble(FundHistory::getNetValue)
                .min().orElse(0.0);
        double volatility = minValue > 0 ? (maxValue - minValue) / minValue * 100 : 0;

        // 计算最大回撤
        double maxDrawdown = 0;
        double peak = historyList.get(0).getNetValue();
        for (FundHistory h : historyList) {
            if (h.getNetValue() > peak) peak = h.getNetValue();
            double drawdown = (peak - h.getNetValue()) / peak * 100;
            if (drawdown > maxDrawdown) maxDrawdown = drawdown;
        }

        // 涨跌天数统计
        long upDays = historyList.stream().filter(h -> h.getChangePercent() > 0).count();
        long downDays = historyList.stream().filter(h -> h.getChangePercent() < 0).count();
        long flatDays = historyList.size() - upDays - downDays;

        // 连涨连跌分析
        int maxConsecutiveUp = 0, maxConsecutiveDown = 0;
        int currentUp = 0, currentDown = 0;
        for (FundHistory h : historyList) {
            if (h.getChangePercent() > 0) {
                currentUp++;
                currentDown = 0;
                maxConsecutiveUp = Math.max(maxConsecutiveUp, currentUp);
            } else if (h.getChangePercent() < 0) {
                currentDown++;
                currentUp = 0;
                maxConsecutiveDown = Math.max(maxConsecutiveDown, currentDown);
            } else {
                currentUp = 0;
                currentDown = 0;
            }
        }

        FundHistory first = historyList.get(0);
        FundHistory latest = historyList.get(historyList.size() - 1);
        double totalReturn = first.getNetValue() > 0
                ? (latest.getNetValue() - first.getNetValue()) / first.getNetValue() * 100 : 0;

        summary.append("===== 核心数据 =====\n");
        summary.append(String.format("基金名称: %s\n", first.getFundName()));
        summary.append(String.format("分析区间: %s 至 %s（共%d个交易日）\n",
                first.getTradeDate(), latest.getTradeDate(), historyList.size()));
        summary.append(String.format("最新净值: %.4f\n", latest.getNetValue()));
        summary.append(String.format("区间涨跌幅: %.2f%%\n", totalReturn));
        summary.append(String.format("平均日涨跌幅: %.4f%%\n", avgChange));
        summary.append(String.format("最高净值: %.4f\n", maxValue));
        summary.append(String.format("最低净值: %.4f\n", minValue));
        summary.append(String.format("振幅: %.2f%%\n", volatility));
        summary.append(String.format("最大回撤: %.2f%%\n", maxDrawdown));
        summary.append(String.format("上涨天数: %d，下跌天数: %d，持平: %d\n", upDays, downDays, flatDays));
        summary.append(String.format("最大连涨天数: %d，最大连跌天数: %d\n", maxConsecutiveUp, maxConsecutiveDown));

        // 最近的日线数据
        summary.append("\n===== 最近交易日明细 =====\n");
        int recentCount = Math.min(20, historyList.size());
        List<FundHistory> recentList = historyList.subList(historyList.size() - recentCount, historyList.size());
        for (FundHistory h : recentList) {
            summary.append(String.format("%s: 净值=%.4f, 涨跌幅=%+.2f%%\n",
                    h.getTradeDate(), h.getNetValue(), h.getChangePercent()));
        }

        return summary.toString();
    }

    /**
     * 构建分析提示词
     */
    private String buildAnalysisPrompt(String fundCode, String fundName, String historySummary, Integer days) {
        return String.format("""
                请对以下基金进行深度走势分析：
                
                基金代码：%s
                基金名称：%s
                分析周期：近%d天

                %s

                请从以下维度进行全面分析，使用markdown格式，让分析报告结构清晰：

                ## 📈 走势总览
                简要概述近期走势特征，包括总体趋势方向和幅度。

                ## 🔍 技术面分析
                1. **趋势判断**：根据净值变化判断当前处于上升/下降/震荡趋势
                2. **支撑与压力**：根据历史高低点分析关键价位
                3. **动量分析**：根据近期涨跌幅变化分析动能强弱
                4. **波动性评估**：根据振幅和日涨跌分布评估波动风险

                ## ⚠️ 风险提示
                根据最大回撤、波动率等指标评估当前风险水平，给出风险等级。

                ## 💡 操作建议
                基于以上分析，给出具体可操作的投资建议：
                - 短期（1-2周）策略
                - 中期（1-3月）策略
                - 适合的投资者类型

                ## 📊 关键指标打分
                对以下维度进行1-5分评分：
                - 收益能力：X/5
                - 风险控制：X/5
                - 趋势稳定性：X/5
                - 综合评价：X/5

                注意：
                1. 分析要基于真实数据，不要虚构数字
                2. 建议要客观谨慎，充分提示投资风险
                3. 记住：投资有风险，过往业绩不代表未来表现
                """, fundCode, fundName, days, historySummary);
    }
}
