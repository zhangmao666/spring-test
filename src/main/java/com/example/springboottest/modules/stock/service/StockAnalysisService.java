package com.example.springboottest.modules.stock.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.example.springboottest.config.AiProperties;
import com.example.springboottest.modules.stock.config.StockProperties;
import com.example.springboottest.modules.stock.dto.StockAnalysisDTO;
import com.example.springboottest.modules.stock.entity.Stock;
import com.example.springboottest.modules.stock.entity.StockAnalysis;
import com.example.springboottest.modules.stock.entity.StockQuote;
import com.example.springboottest.modules.stock.enums.RiskLevel;
import com.example.springboottest.modules.stock.enums.TrendType;
import com.example.springboottest.modules.stock.repository.StockAnalysisRepository;
import com.example.springboottest.modules.stock.repository.StockQuoteRepository;
import com.example.springboottest.modules.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockAnalysisService {

    private final StockRepository stockRepository;
    private final StockQuoteRepository stockQuoteRepository;
    private final StockAnalysisRepository stockAnalysisRepository;
    private final StockProperties stockProperties;
    private final AiProperties aiProperties;

    @Cacheable(value = "stockAnalysis", key = "#stockId", unless = "#result == null")
    public StockAnalysisDTO getLatestAnalysis(Long stockId) {
        return stockAnalysisRepository.findLatestByStockId(stockId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public CompletableFuture<StockAnalysisDTO> analyzeStock(Long stockId) {
        if (!stockProperties.getAnalysis().getEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                Stock stock = stockRepository.selectById(stockId);
                if (stock == null) {
                    throw new RuntimeException("股票不存在: " + stockId);
                }

                List<StockQuote> recentQuotes = stockQuoteRepository.findRecentByStockId(stockId, 20);
                if (recentQuotes.isEmpty()) {
                    throw new RuntimeException("没有足够的行情数据进行分析");
                }

                String prompt = buildAnalysisPrompt(stock, recentQuotes);
                String analysisContent = callQwenApi(prompt);

                return processAnalysisResult(stockId, stock, analysisContent);
            } catch (Exception e) {
                log.error("AI分析失败", e);
                throw new RuntimeException("AI分析失败: " + e.getMessage(), e);
            }
        });
    }

    private String callQwenApi(String prompt) throws NoApiKeyException, ApiException, InputRequiredException {
        Generation gen = new Generation();

        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一位专业的股票分析师，擅长技术分析和基本面分析。")
                .build();

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build();

        GenerationParam param = GenerationParam.builder()
                .apiKey(aiProperties.getQwen().getApiKey())
                .model(aiProperties.getQwen().getModel())
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .temperature(Float.valueOf(String.valueOf(aiProperties.getQwen().getTemperature())))
                .maxTokens(aiProperties.getQwen().getMaxTokens())
                .build();

        GenerationResult result = gen.call(param);

        if (result == null || result.getOutput() == null || result.getOutput().getChoices() == null || result.getOutput().getChoices().isEmpty()) {
            throw new RuntimeException("Qwen API返回结果为空");
        }

        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }

    private String buildAnalysisPrompt(Stock stock, List<StockQuote> quotes) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下股票的最近走势数据，给出趋势判断、价格预测和投资建议：\n\n");
        prompt.append(String.format("股票名称：%s (%s)\n", stock.getStockName(), stock.getStockCode()));
        prompt.append(String.format("所属行业：%s\n\n", stock.getIndustry()));
        prompt.append("最近20个交易日数据：\n");

        for (int i = quotes.size() - 1; i >= 0; i--) {
            StockQuote quote = quotes.get(i);
            prompt.append(String.format("日期：%s，价格：%.2f，涨跌幅：%.2f%%，成交量：%d\n",
                    quote.getQuoteTime().toLocalDate(),
                    quote.getCurrentPrice(),
                    quote.getChangePercent(),
                    quote.getVolume()));
        }

        prompt.append("\n请从以下几个方面分析（格式严格按照示例）：\n");
        prompt.append("1. 趋势判断：[UP/DOWN/STABLE]其中之一\n");
        prompt.append("2. 置信度：[0-100的数字]\n");
        prompt.append("3. 预测价格：[具体数字，保留2位小数]\n");
        prompt.append("4. 风险等级：[LOW/MEDIUM/HIGH]其中之一\n");
        prompt.append("5. 分析说明：[详细的分析内容，包括支撑位、阻力位、操作建议等]\n");

        return prompt.toString();
    }

    @Transactional
    private StockAnalysisDTO processAnalysisResult(Long stockId, Stock stock, String content) {
        TrendType trendType = extractTrendType(content);
        BigDecimal confidenceScore = extractConfidenceScore(content);
        BigDecimal predictedPrice = extractPredictedPrice(content);
        RiskLevel riskLevel = extractRiskLevel(content);

        StockAnalysis analysis = StockAnalysis.builder()
                .stockId(stockId)
                .analysisTime(LocalDateTime.now())
                .analysisContent(content)
                .trendType(trendType.getCode())
                .confidenceScore(confidenceScore)
                .predictedPrice(predictedPrice)
                .riskLevel(riskLevel.getCode())
                .aiProvider("qwen")
                .status(1)
                .build();

        stockAnalysisRepository.insert(analysis);
        log.info("保存AI分析结果: {} - {}", stock.getStockCode(), trendType.getDescription());

        return convertToDTO(analysis, stock);
    }

    private TrendType extractTrendType(String content) {
        Pattern pattern = Pattern.compile("趋势判断[：:】]?\\s*\\[?(UP|DOWN|STABLE)\\]?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return TrendType.fromCode(matcher.group(1).toUpperCase());
        }
        return TrendType.STABLE;
    }

    private BigDecimal extractConfidenceScore(String content) {
        Pattern pattern = Pattern.compile("置信度[：:】]?\\s*\\[?(\\d+\\.?\\d*)\\]?");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return new BigDecimal("50");
    }

    private BigDecimal extractPredictedPrice(String content) {
        Pattern pattern = Pattern.compile("预测价格[：:】]?\\s*\\[?(\\d+\\.?\\d*)\\]?");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return BigDecimal.ZERO;
    }

    private RiskLevel extractRiskLevel(String content) {
        Pattern pattern = Pattern.compile("风险等级[：:】]?\\s*\\[?(LOW|MEDIUM|HIGH)\\]?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return RiskLevel.fromCode(matcher.group(1).toUpperCase());
        }
        return RiskLevel.MEDIUM;
    }

    private StockAnalysisDTO convertToDTO(StockAnalysis analysis) {
        Stock stock = stockRepository.selectById(analysis.getStockId());
        return convertToDTO(analysis, stock);
    }

    private StockAnalysisDTO convertToDTO(StockAnalysis analysis, Stock stock) {
        return StockAnalysisDTO.builder()
                .id(analysis.getId())
                .stockId(analysis.getStockId())
                .stockCode(stock != null ? stock.getStockCode() : null)
                .stockName(stock != null ? stock.getStockName() : null)
                .analysisTime(analysis.getAnalysisTime())
                .analysisContent(analysis.getAnalysisContent())
                .trendType(analysis.getTrendType())
                .confidenceScore(analysis.getConfidenceScore())
                .predictedPrice(analysis.getPredictedPrice())
                .riskLevel(analysis.getRiskLevel())
                .aiProvider(analysis.getAiProvider())
                .build();
    }
}
