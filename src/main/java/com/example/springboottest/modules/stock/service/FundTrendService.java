package com.example.springboottest.modules.stock.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboottest.modules.stock.dto.FundTrendDTO;
import com.example.springboottest.modules.stock.entity.FundHistory;
import com.example.springboottest.modules.stock.repository.FundHistoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基金走势服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FundTrendService {

    private final FundHistoryRepository fundHistoryRepository;
    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    /**
     * 获取基金走势数据
     * @param fundCode 基金代码
     * @param days 天数（默认90天）
     */
    public FundTrendDTO getFundTrend(String fundCode, Integer days) {
        if (days == null || days <= 0) {
            days = 90;
        }

        // 先从数据库查询
        LocalDate startDate = LocalDate.now().minusDays(days);
        List<FundHistory> historyList = fundHistoryRepository.selectList(
                new LambdaQueryWrapper<FundHistory>()
                        .eq(FundHistory::getFundCode, fundCode)
                        .ge(FundHistory::getTradeDate, startDate)
                        .orderByAsc(FundHistory::getTradeDate)
        );

        // 如果数据库没有数据或数据不足，从API获取
        if (historyList.isEmpty() || historyList.size() < days / 2) {
            log.info("数据库数据不足，从API获取基金走势: {}", fundCode);
            fetchAndSaveFundHistory(fundCode, days);

            // 重新查询
            historyList = fundHistoryRepository.selectList(
                    new LambdaQueryWrapper<FundHistory>()
                            .eq(FundHistory::getFundCode, fundCode)
                            .ge(FundHistory::getTradeDate, startDate)
                            .orderByAsc(FundHistory::getTradeDate)
            );
        }

        // 转换为DTO
        List<FundTrendDTO.TrendPoint> trendData = historyList.stream()
                .map(h -> FundTrendDTO.TrendPoint.builder()
                        .date(h.getTradeDate())
                        .netValue(h.getNetValue())
                        .accumulatedValue(h.getAccumulatedValue())
                        .changePercent(h.getChangePercent())
                        .build())
                .collect(Collectors.toList());

        String fundName = historyList.isEmpty() ? "" : historyList.get(0).getFundName();

        return FundTrendDTO.builder()
                .fundCode(fundCode)
                .fundName(fundName)
                .trendData(trendData)
                .build();
    }

    /**
     * 从天天基金API获取历史数据并保存（支持分页获取）
     */
    @Transactional
    public void fetchAndSaveFundHistory(String fundCode, Integer days) {
        try {
            // API每页最多返回20条，需要分页获取
            int pageSize = 20;
            int totalPages = (days + pageSize - 1) / pageSize; // 向上取整

            List<FundHistory> allHistoryList = new ArrayList<>();
            String fundName = fundCode;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // 分页获取数据
            for (int pageIndex = 1; pageIndex <= totalPages; pageIndex++) {
                String url = String.format(
                        "https://api.fund.eastmoney.com/f10/lsjz?callback=jQuery&fundCode=%s&pageIndex=%d&pageSize=%d",
                        fundCode, pageIndex, pageSize
                );

                Request request = new Request.Builder()
                        .url(url)
                        .header("Referer", "https://fundf10.eastmoney.com/")
                        .build();

                try (Response response = okHttpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        log.error("获取基金历史数据失败: HTTP {}, pageIndex={}", response.code(), pageIndex);
                        continue;
                    }

                    String body = response.body().string();
                    // 去除JSONP的callback包装: jQuery({...})
                    if (body.startsWith("jQuery(") && body.endsWith(")")) {
                        body = body.substring(7, body.length() - 1);
                    }
                    JsonNode root = objectMapper.readTree(body);

                    // 解析响应
                    if (root.has("Data") && root.get("Data").has("LSJZList")) {
                        JsonNode dataNode = root.get("Data");
                        JsonNode dataList = dataNode.get("LSJZList");

                        // 第一页时获取基金名称
                        if (pageIndex == 1) {
                            if (dataNode.has("SHORTNAME") && dataNode.get("SHORTNAME") != null) {
                                fundName = dataNode.get("SHORTNAME").asText(fundCode);
                            } else if (dataNode.has("FundName") && dataNode.get("FundName") != null) {
                                fundName = dataNode.get("FundName").asText(fundCode);
                            }
                            log.info("解析基金名称: fundCode={}, fundName={}", fundCode, fundName);
                        }

                        // 解析数据
                        for (JsonNode item : dataList) {
                            try {
                                double netValue = item.has("DWJZ") && item.get("DWJZ") != null
                                        ? item.get("DWJZ").asDouble(0.0) : 0.0;
                                double accValue = item.has("LJJZ") && item.get("LJJZ") != null
                                        ? item.get("LJJZ").asDouble(0.0) : netValue;
                                double changePercent = 0.0;
                                if (item.has("JZZZL") && item.get("JZZZL") != null
                                        && !item.get("JZZZL").asText().isEmpty()) {
                                    try {
                                        changePercent = Double.parseDouble(item.get("JZZZL").asText());
                                    } catch (NumberFormatException ignored) {}
                                }
                                String dateStr = item.has("FSRQ") && item.get("FSRQ") != null
                                        ? item.get("FSRQ").asText() : null;
                                if (dateStr == null || dateStr.isEmpty() || netValue <= 0) continue;

                                FundHistory history = FundHistory.builder()
                                        .fundCode(fundCode)
                                        .fundName(fundName)
                                        .netValue(netValue)
                                        .accumulatedValue(accValue)
                                        .changePercent(changePercent)
                                        .tradeDate(LocalDate.parse(dateStr, formatter))
                                        .createTime(LocalDate.now())
                                        .build();
                                allHistoryList.add(history);
                            } catch (Exception e) {
                                log.warn("解析单条基金数据失败，跳过: {}", e.getMessage());
                            }
                        }

                        // 如果当前页数据不足20条，说明已经是最后一页
                        if (dataList.size() < pageSize) {
                            break;
                        }
                    }
                }

                // 避免请求过快，每页之间休眠200ms
                if (pageIndex < totalPages) {
                    Thread.sleep(200);
                }
            }

            if (!allHistoryList.isEmpty()) {
                // 批量保存（先删除旧数据）
                fundHistoryRepository.delete(new LambdaQueryWrapper<FundHistory>()
                        .eq(FundHistory::getFundCode, fundCode));

                allHistoryList.forEach(fundHistoryRepository::insert);
                log.info("保存基金历史数据成功: {} 条", allHistoryList.size());
            } else {
                log.warn("未获取到基金 {} 的历史数据", fundCode);
            }
        } catch (Exception e) {
            log.error("获取基金历史数据异常: {}", e.getMessage(), e);
        }
    }
}
