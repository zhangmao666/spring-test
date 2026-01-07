package com.example.springboottest.modules.stock.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboottest.entity.DTO.AiChatRequest;
import com.example.springboottest.entity.DTO.AiChatResponse;
import com.example.springboottest.modules.ai.service.AiChatService;
import com.example.springboottest.modules.stock.entity.FundNews;
import com.example.springboottest.modules.stock.entity.FundRanking;
import com.example.springboottest.modules.stock.repository.FundNewsRepository;
import com.example.springboottest.modules.stock.repository.FundRankingRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 基金数据服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FundDataService {

    private final AiChatService aiChatService;
    private final FundRankingRepository fundRankingRepository;
    private final FundNewsRepository fundNewsRepository;
    private final ObjectMapper objectMapper;

    /**
     * 更新前一天的基金数据
     */
    @Transactional
    public void updateFundData() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("开始获取并处理基金数据，目标日期: {}", yesterday);

        // 1. 获取并更新排行榜
        updateRanking(yesterday);

        // 2. 获取并更新资讯
        updateNews(yesterday);
        
        log.info("基金数据更新任务完成");
    }

    private void updateRanking(LocalDate date) {
        String prompt = "任务：获取 " + date + " 的热门开放式基金排行数据（前15名）。\n" +
                "必须包含以下字段的丰富数据：\n" +
                "1. fundCode (代码), fundName (名称), netValue (最新净值), changePercent (日涨跌%)\n" +
                "2. oneMonthReturn (近1月收益%), oneYearReturn (近1年收益%), ytd_return (今年来收益%)\n" +
                "3. maxDrawdown (近1年最大回撤%, 填负数如-15.2)\n" +
                "4. managerName (经理名), managerYears (从业年限, 整数)\n" +
                "5. starRating (晨星评级, 1-5整数)\n" +
                "6. fundType (类型: 股票型|混合型|债券型|指数型)\n" +
                "7. sector (主题: 消费|科技|医疗|新能源|金融|制造)\n" +
                "约束：\n" +
                "1. 严禁任何文字说明，仅返回纯净 JSON 数组。\n" +
                "2. 数据要真实模拟当前市场风格（如科技/半导体近日活跃）。\n" +
                "3. 严禁使用 Markdown 代码块。";

        AiChatRequest request = new AiChatRequest();
        request.setMessage(prompt);
        request.setUseWebSearch(true);
        // 如果使用了 Gemini 模型，建议降低随机性
        request.setTemperature(0.1);

        try {
            log.info("正在调用AI获取排行数据...");
            AiChatResponse response = aiChatService.chat(request).get();
            if (response.isSuccess()) {
                String jsonStr = extractJson(response.getMessage());
                try {
                    List<FundRanking> rankings = objectMapper.readValue(jsonStr, new TypeReference<List<FundRanking>>() {});
                    
                    // 清理旧数据并保存新数据
                    fundRankingRepository.delete(new LambdaQueryWrapper<FundRanking>().eq(FundRanking::getUpdateDate, date));
                    for (FundRanking r : rankings) {
                        r.setUpdateDate(date);
                        fundRankingRepository.insert(r);
                    }
                    log.info("排行数据保存成功，共 {} 条", rankings.size());
                } catch (Exception e) {
                    log.error("解析基金排行JSON失败. 原始消息内容: {}", response.getMessage());
                    throw e;
                }
            } else {
                log.error("AI服务返回错误: {}", response.getError());
            }
        } catch (Exception e) {
            log.error("AI获取基金排行失败: {}", e.getMessage());
        }
    }

    private void updateNews(LocalDate date) {
        String prompt = "任务：整理 " + date + " 基金行业的5条核心资讯。\n" +
                "约束：\n" +
                "1. 必须包含字段：title (字符串), summary (字符串且禁止包含Markdown符号), url (字符串), publishDate (ISO格式: yyyy-MM-ddTHH:mm:ss)\n" +
                "2. 严禁任何文字说明、前言、包裹符号、Markdown代码快或分析。\n" +
                "3. 严禁输出任何非 JSON 字符。\n" +
                "4. 仅返回一个纯净的 JSON 数组。\n" +
                "输出格式要求严格，确保 Jackson 可以直接读取。";

        AiChatRequest request = new AiChatRequest();
        request.setMessage(prompt);
        request.setUseWebSearch(true);
        request.setTemperature(0.2);

        try {
            log.info("正在调用AI获取资讯数据...");
            AiChatResponse response = aiChatService.chat(request).get();
            if (response.isSuccess()) {
                String jsonStr = extractJson(response.getMessage());
                try {
                    List<FundNews> newsList = objectMapper.readValue(jsonStr, new TypeReference<List<FundNews>>() {});
                    
                    // 清理旧数据并保存新数据
                    fundNewsRepository.delete(new LambdaQueryWrapper<FundNews>().eq(FundNews::getUpdateDate, date));
                    for (FundNews n : newsList) {
                        n.setUpdateDate(date);
                        fundNewsRepository.insert(n);
                    }
                    log.info("资讯数据保存成功，共 {} 条", newsList.size());
                } catch (Exception e) {
                    log.error("解析基金资讯JSON失败. 原始消息内容: {}", response.getMessage());
                    throw e;
                }
            }
        } catch (Exception e) {
            log.error("AI获取基金资讯失败: {}", e.getMessage());
        }
    }

    private String extractJson(String content) {
        if (content == null || content.isEmpty()) return "[]";
        
        // 1. 尝试匹配 Markdown JSON 代码块
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

        // 2. 尝试寻找最外层的 [ ]
        int firstBracket = content.indexOf("[");
        int lastBracket = content.lastIndexOf("]");
        if (firstBracket != -1 && lastBracket != -1 && lastBracket > firstBracket) {
            return content.substring(firstBracket, lastBracket + 1).trim();
        }

        return content.trim();
    }

    public List<FundRanking> getLatestRankings() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return fundRankingRepository.selectList(new LambdaQueryWrapper<FundRanking>()
                .eq(FundRanking::getUpdateDate, yesterday));
    }

    public List<FundNews> getLatestNews() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return fundNewsRepository.selectList(new LambdaQueryWrapper<FundNews>()
                .eq(FundNews::getUpdateDate, yesterday));
    }
}
