package com.example.springboottest.modules.ai.agent.tool;

import com.example.springboottest.modules.market.dto.MarketQuoteResponse;
import com.example.springboottest.modules.market.service.MarketQuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MarketQuoteAgentTool implements AgentToolExecutor {

    private final MarketQuoteService marketQuoteService;

    @Override
    public AgentToolDefinition definition() {
        return AgentToolDefinition.builder()
                .id("market_quote")
                .name("get_market_quote")
                .description("查询股票、基金或加密货币行情。需要 query 字段，可选 assetType 字段，值可为 stock、fund、crypto。")
                .parametersSchema(AgentToolSchemas.object(
                        Map.of(
                                "query", AgentToolSchemas.stringProperty("股票代码/名称、基金代码或加密货币名称/符号"),
                                "assetType", AgentToolSchemas.stringProperty("资产类型，可选 stock、fund、crypto")
                        ),
                        List.of("query")
                ))
                .build();
    }

    @Override
    public AgentToolResult execute(Map<String, Object> arguments) {
        String query = value(arguments, "query", "");
        String assetType = value(arguments, "assetType", null);
        try {
            MarketQuoteResponse response = marketQuoteService.getQuote(assetType, query);
            String resolvedAssetType = fallback(response.getAssetType(), "stock");
            return AgentToolResult.success(switch (resolvedAssetType) {
                case "fund" -> formatFundQuote(response);
                case "crypto" -> formatCryptoQuote(response);
                default -> formatStockQuote(response);
            });
        } catch (Exception e) {
            return AgentToolResult.error("行情查询失败：" + fallback(e.getMessage(), "未知错误"));
        }
    }

    private String formatStockQuote(MarketQuoteResponse response) {
        return """
                资产类型：股票
                查询内容：%s
                名称：%s
                代码：%s
                市场：%s
                币种：%s
                最新价：%s
                涨跌额：%s
                涨跌幅：%s
                数据源：%s
                详情页：%s
                """.formatted(
                fallback(response.getQuery(), "未知"),
                fallback(response.getName(), "未知"),
                fallback(response.getCode(), "未知"),
                fallback(response.getMarket(), "未知"),
                fallback(response.getCurrency(), "未知"),
                money(response.getPrice(), 2),
                money(response.getChange(), 2),
                percent(response.getChangePercent()),
                fallback(response.getSourceName(), "未知"),
                fallback(response.getSourceUrl(), "未知")
        ).trim();
    }

    private String formatFundQuote(MarketQuoteResponse response) {
        return """
                资产类型：基金
                查询内容：%s
                名称：%s
                代码：%s
                最新单位净值：%s
                净值日期：%s
                估算净值：%s
                估算涨跌幅：%s
                提示：%s
                数据源：%s
                详情页：%s
                """.formatted(
                fallback(response.getQuery(), "未知"),
                fallback(response.getName(), "未知"),
                fallback(response.getCode(), "未知"),
                money(response.getLatestNetValue(), 4),
                fallback(response.getLatestNetValueDate(), "未知"),
                money(response.getEstimatedNetValue(), 4),
                percent(response.getChangePercent()),
                fallback(response.getNote(), "仅供参考"),
                fallback(response.getSourceName(), "未知"),
                fallback(response.getSourceUrl(), "未知")
        ).trim();
    }

    private String formatCryptoQuote(MarketQuoteResponse response) {
        return """
                资产类型：加密货币
                查询内容：%s
                名称：%s
                符号：%s
                最新价（USD）：%s
                最新价（CNY）：%s
                24小时涨跌幅：%s
                提示：%s
                数据源：%s
                详情页：%s
                """.formatted(
                fallback(response.getQuery(), "未知"),
                fallback(response.getName(), "未知"),
                fallback(response.getSymbol(), "未知"),
                money(response.getPrice(), 4),
                money(response.getSecondaryPrice(), 4),
                percent(response.getChangePercent()),
                fallback(response.getNote(), "仅供参考"),
                fallback(response.getSourceName(), "未知"),
                fallback(response.getSourceUrl(), "未知")
        ).trim();
    }

    private String value(Map<String, Object> arguments, String key, String fallback) {
        Object value = arguments == null ? null : arguments.get(key);
        return StringUtils.hasText(value == null ? null : String.valueOf(value)) ? String.valueOf(value).trim() : fallback;
    }

    private String fallback(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String money(Double value, int scale) {
        return value == null ? "未知" : String.format("%." + scale + "f", value);
    }

    private String percent(Double value) {
        return value == null ? "未知" : String.format("%.2f%%", value);
    }
}
