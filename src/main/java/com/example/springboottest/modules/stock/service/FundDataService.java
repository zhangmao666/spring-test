package com.example.springboottest.modules.stock.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboottest.modules.stock.entity.FundNews;
import com.example.springboottest.modules.stock.entity.FundRanking;
import com.example.springboottest.modules.stock.repository.FundNewsRepository;
import com.example.springboottest.modules.stock.repository.FundRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基金数据服务类
 * 使用天天基金(Eastmoney)真实API获取排行数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FundDataService {

    private final FundRankingRepository fundRankingRepository;
    private final FundNewsRepository fundNewsRepository;
    private final FundTrendService fundTrendService;
    private final OkHttpClient okHttpClient;

    /**
     * 天天基金排行API基础地址
     * 参数说明:
     *   op=ph: 排行操作
     *   dt=kf: 开放式基金
     *   ft=all/gp/hh/zq/zs: 基金类型 (全部/股票型/混合型/债券型/指数型)
     *   sc=1nzf/rzdf/zzf/1yzf/6yzf/jnzf: 排序字段 (近1年/日涨跌/近1周/近1月/近6月/今年来)
     *   st=desc/asc: 排序方向
     *   pi=1: 页码
     *   pn=30: 每页数量
     *   dx=1: 含衍生品
     */
    private static final String EASTMONEY_RANK_API =
            "https://fund.eastmoney.com/data/rankhandler.aspx?op=ph&dt=kf&ft=%s&rs=&gs=0&sc=%s&st=desc&pi=1&pn=%d&dx=1";

    /**
     * 更新基金排行数据和历史净值数据
     */
    @Transactional
    public void updateFundData() {
        LocalDate today = LocalDate.now();
        log.info("开始更新基金排行数据，日期: {}", today);

        try {
            // 获取并保存排行数据
            List<FundRanking> rankings = fetchRankingFromEastmoney("all", "1nzf", 30);

            if (rankings.isEmpty()) {
                log.warn("未获取到排行数据");
                return;
            }

            // 先删除今天的数据，再插入
            fundRankingRepository.delete(new LambdaQueryWrapper<FundRanking>()
                    .eq(FundRanking::getUpdateDate, today));

            for (FundRanking r : rankings) {
                r.setUpdateDate(today);
                fundRankingRepository.insert(r);
            }

            log.info("基金排行数据保存成功，共 {} 条", rankings.size());

            // 更新前30名基金的历史净值数据（近一年）
            log.info("开始更新基金历史净值数据");
            int successCount = 0;
            for (FundRanking ranking : rankings) {
                try {
                    fundTrendService.fetchAndSaveFundHistory(ranking.getFundCode(), 365);
                    successCount++;
                    log.info("更新基金 {} ({}) 历史数据成功", ranking.getFundCode(), ranking.getFundName());
                    // 避免请求过快，休眠500ms
                    Thread.sleep(500);
                } catch (Exception e) {
                    log.error("更新基金 {} 历史数据失败: {}", ranking.getFundCode(), e.getMessage());
                }
            }
            log.info("基金历史净值数据更新完成，成功 {} 条", successCount);

        } catch (Exception e) {
            log.error("更新基金排行数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从天天基金API获取排行数据
     *
     * @param fundType 基金类型: all/gp/hh/zq/zs
     * @param sortBy   排序字段: 1nzf(近1年), rzdf(日涨跌), 1yzf(近1月), 6yzf(近6月), jnzf(今年来)
     * @param pageSize 获取数量
     */
    public List<FundRanking> fetchRankingFromEastmoney(String fundType, String sortBy, int pageSize) {
        List<FundRanking> result = new ArrayList<>();

        String url = String.format(EASTMONEY_RANK_API, fundType, sortBy, pageSize);
        log.info("请求天天基金排行API: {}", url);

        Request request = new Request.Builder()
                .url(url)
                .header("Referer", "https://fund.eastmoney.com/data/fundranking.html")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("天天基金API请求失败: HTTP {}", response.code());
                return result;
            }

            String body = response.body().string();
            log.debug("API原始响应长度: {}", body.length());

            // 解析JavaScript格式响应: var rankData = {datas:["...","...",...],allRecords:...}
            List<String> dataStrings = extractDataStrings(body);
            log.info("解析到 {} 条基金数据", dataStrings.size());

            for (String dataStr : dataStrings) {
                try {
                    FundRanking ranking = parseRankingData(dataStr);
                    if (ranking != null) {
                        result.add(ranking);
                    }
                } catch (Exception e) {
                    log.warn("解析单条基金数据失败，跳过: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("调用天天基金API异常: {}", e.getMessage(), e);
        }

        return result;
    }

    /**
     * 从响应体中提取数据字符串列表
     * 响应格式: var rankData = {datas:["022364,永赢...,","000001,华夏..."],allRecords:19260,...}
     */
    private List<String> extractDataStrings(String responseBody) {
        List<String> dataList = new ArrayList<>();

        // 用正则提取 datas 数组中的每个字符串
        Pattern pattern = Pattern.compile("\"([^\"]+)\"");
        int datasStart = responseBody.indexOf("datas:[");
        if (datasStart == -1) {
            log.error("响应中未找到 datas 字段");
            return dataList;
        }

        int datasEnd = responseBody.indexOf("]", datasStart);
        if (datasEnd == -1) {
            log.error("响应中 datas 数组格式异常");
            return dataList;
        }

        String datasSection = responseBody.substring(datasStart + 7, datasEnd);
        Matcher matcher = pattern.matcher(datasSection);
        while (matcher.find()) {
            dataList.add(matcher.group(1));
        }

        return dataList;
    }

    /**
     * 解析单条基金数据
     *
     * 字段索引映射（天天基金API返回的逗号分隔数据）:
     *   0: 基金代码
     *   1: 基金名称
     *   2: 拼音简称
     *   3: 净值日期
     *   4: 单位净值
     *   5: 累计净值
     *   6: 日涨跌幅(%)
     *   7: 近1周(%)
     *   8: 近1月(%)
     *   9: 近3月(%)
     *  10: 近6月(%)
     *  11: 近1年(%)
     *  12-13: (可能为空)
     *  14: 今年来(%)
     *  15: 成立来(%)
     */
    private FundRanking parseRankingData(String dataStr) {
        String[] fields = dataStr.split(",", -1);

        if (fields.length < 16) {
            log.warn("数据字段不足, 实际: {}, 数据: {}", fields.length, dataStr.substring(0, Math.min(100, dataStr.length())));
            return null;
        }

        String fundCode = fields[0].trim();
        String fundName = fields[1].trim();

        if (fundCode.isEmpty() || fundName.isEmpty()) {
            return null;
        }

        // 推断基金类型
        String fundType = inferFundType(fundName);

        return FundRanking.builder()
                .fundCode(fundCode)
                .fundName(fundName)
                .netValue(parseDouble(fields[4]))
                .accumulatedValue(parseDouble(fields[5]))
                .changePercent(parseDouble(fields[6]))
                .oneWeekReturn(parseDouble(fields[7]))
                .oneMonthReturn(parseDouble(fields[8]))
                .threeMonthReturn(parseDouble(fields[9]))
                .sixMonthReturn(parseDouble(fields[10]))
                .oneYearReturn(parseDouble(fields[11]))
                .ytdReturn(parseDouble(fields[14]))
                .sinceInceptionReturn(parseDouble(fields[15]))
                .fundType(fundType)
                .build();
    }

    /**
     * 根据基金名称推断基金类型
     */
    private String inferFundType(String fundName) {
        if (fundName.contains("指数") || fundName.contains("ETF") || fundName.contains("LOF")) {
            return "指数型";
        } else if (fundName.contains("债") || fundName.contains("利率") || fundName.contains("信用")) {
            return "债券型";
        } else if (fundName.contains("混合") || fundName.contains("配置") || fundName.contains("平衡")) {
            return "混合型";
        } else if (fundName.contains("股票") || fundName.contains("成长") || fundName.contains("价值") ||
                   fundName.contains("优选") || fundName.contains("精选") || fundName.contains("主题")) {
            return "股票型";
        } else if (fundName.contains("货币") || fundName.contains("现金")) {
            return "货币型";
        } else if (fundName.contains("QDII") || fundName.contains("美元") || fundName.contains("美国")) {
            return "QDII";
        }
        return "混合型"; // 默认
    }

    /**
     * 安全解析 Double，空值返回 null
     */
    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取最新排行数据
     * 优先取今天的数据，如果没有则取最近一次的数据
     */
    public List<FundRanking> getLatestRankings() {
        // 先尝试取今天的数据
        LocalDate today = LocalDate.now();
        List<FundRanking> rankings = fundRankingRepository.selectList(
                new LambdaQueryWrapper<FundRanking>()
                        .eq(FundRanking::getUpdateDate, today)
                        .orderByDesc(FundRanking::getOneYearReturn)
        );

        if (!rankings.isEmpty()) {
            return rankings;
        }

        // 如果今天没有数据，尝试获取昨天的
        LocalDate yesterday = LocalDate.now().minusDays(1);
        rankings = fundRankingRepository.selectList(
                new LambdaQueryWrapper<FundRanking>()
                        .eq(FundRanking::getUpdateDate, yesterday)
                        .orderByDesc(FundRanking::getOneYearReturn)
        );

        if (!rankings.isEmpty()) {
            return rankings;
        }

        // 如果最近两天都没有，取数据库中最新的一批
        rankings = fundRankingRepository.selectList(
                new LambdaQueryWrapper<FundRanking>()
                        .orderByDesc(FundRanking::getUpdateDate)
                        .orderByDesc(FundRanking::getOneYearReturn)
                        .last("LIMIT 30")
        );

        return rankings;
    }

    /**
     * 获取最新资讯数据
     */
    public List<FundNews> getLatestNews() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<FundNews> news = fundNewsRepository.selectList(
                new LambdaQueryWrapper<FundNews>()
                        .eq(FundNews::getUpdateDate, yesterday));

        if (news.isEmpty()) {
            // 取最新的资讯
            news = fundNewsRepository.selectList(
                    new LambdaQueryWrapper<FundNews>()
                            .orderByDesc(FundNews::getPublishDate)
                            .last("LIMIT 10"));
        }

        return news;
    }
}
