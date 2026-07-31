package com.example.springboottest.modules.market.service;

import com.example.springboottest.modules.market.dto.MarketQuoteResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketQuoteService {

    private static final String ASSET_TYPE_STOCK = "stock";
    private static final String ASSET_TYPE_FUND = "fund";
    private static final String ASSET_TYPE_CRYPTO = "crypto";

    private static final String EASTMONEY_SEARCH_URL = "https://searchapi.eastmoney.com/api/suggest/get";
    private static final String EASTMONEY_STOCK_QUOTE_URL = "https://push2.eastmoney.com/api/qt/stock/get";
    private static final String EASTMONEY_FUND_QUOTE_URL = "https://fundmobapi.eastmoney.com/FundMNewApi/FundMNFInfo";
    private static final String COINGECKO_SEARCH_URL = "https://api.coingecko.com/api/v3/search";
    private static final String COINGECKO_SIMPLE_PRICE_URL = "https://api.coingecko.com/api/v3/simple/price";

    private static final String EASTMONEY_STOCK_FIELDS = "f43,f44,f45,f46,f47,f48,f57,f58,f60,f169,f170,f116";

    private static final Map<String, String> CRYPTO_ALIASES = createCryptoAliases();

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MarketQuoteResponse getQuote(String assetType, String query) {
        String normalizedAssetType = normalizeAssetType(assetType);
        String normalizedQuery = normalizeQuery(query);

        return switch (normalizedAssetType) {
            case ASSET_TYPE_STOCK -> getStockQuote(normalizedQuery);
            case ASSET_TYPE_FUND -> getFundQuote(normalizedQuery);
            case ASSET_TYPE_CRYPTO -> getCryptoQuote(normalizedQuery);
            default -> throw new IllegalArgumentException("不支持的资产类型: " + normalizedAssetType);
        };
    }

    static String normalizeAssetType(String assetType) {
        String normalized = StringUtils.hasText(assetType) ? assetType.trim().toLowerCase(Locale.ROOT) : ASSET_TYPE_STOCK;
        return switch (normalized) {
            case "stock", "stocks", "股票", "equity", "hk", "us", "cn", "a-share", "a股", "港股", "美股" -> ASSET_TYPE_STOCK;
            case "fund", "funds", "基金", "etf" -> ASSET_TYPE_FUND;
            case "crypto", "coin", "coins", "加密货币", "数字货币", "virtual-currency" -> ASSET_TYPE_CRYPTO;
            default -> normalized;
        };
    }

    private MarketQuoteResponse getStockQuote(String query) {
        URI searchUri = UriComponentsBuilder.fromUriString(EASTMONEY_SEARCH_URL)
                .queryParam("input", query)
                .queryParam("type", "14")
                .queryParam("count", "5")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        JsonNode searchRoot = parseJson(exchange(searchUri));
        JsonNode firstCandidate = searchRoot.path("QuotationCodeTable").path("Data").path(0);
        if (firstCandidate.isMissingNode() || firstCandidate.isNull()) {
            throw new IllegalArgumentException("未找到股票或 ETF: " + query);
        }

        String secId = text(firstCandidate, "QuoteID");
        if (!StringUtils.hasText(secId)) {
            String marketNumber = text(firstCandidate, "MktNum");
            String code = text(firstCandidate, "Code");
            if (!StringUtils.hasText(marketNumber) || !StringUtils.hasText(code)) {
                throw new IllegalStateException("股票搜索结果缺少 secid");
            }
            secId = marketNumber + "." + code;
        }

        URI quoteUri = UriComponentsBuilder.fromUriString(EASTMONEY_STOCK_QUOTE_URL)
                .queryParam("secid", secId)
                .queryParam("fields", EASTMONEY_STOCK_FIELDS)
                .build(true)
                .toUri();

        JsonNode quoteRoot = parseJson(exchange(quoteUri));
        JsonNode data = quoteRoot.path("data");
        if (data.isMissingNode() || data.isNull()) {
            throw new IllegalStateException("未获取到股票行情数据");
        }

        return MarketQuoteResponse.builder()
                .assetType(ASSET_TYPE_STOCK)
                .query(query)
                .code(defaultIfBlank(text(data, "f57"), text(firstCandidate, "Code")))
                .name(defaultIfBlank(text(data, "f58"), text(firstCandidate, "Name")))
                .market(defaultIfBlank(text(firstCandidate, "SecurityTypeName"), inferStockMarket(secId)))
                .currency(inferStockCurrency(secId))
                .price(scaledNumber(data, "f43", 100))
                .change(scaledNumber(data, "f169", 100))
                .changePercent(scaledNumber(data, "f170", 100))
                .open(scaledNumber(data, "f46", 100))
                .high(scaledNumber(data, "f44", 100))
                .low(scaledNumber(data, "f45", 100))
                .previousClose(scaledNumber(data, "f60", 100))
                .volume(number(data, "f47"))
                .turnover(number(data, "f48"))
                .marketCap(number(data, "f116"))
                .sourceName("东方财富")
                .sourceUrl("https://quote.eastmoney.com/" + defaultIfBlank(text(firstCandidate, "Code"), text(data, "f57")) + ".html")
                .build();
    }

    private MarketQuoteResponse getFundQuote(String query) {
        if (!query.matches("\\d{6}")) {
            throw new IllegalArgumentException("基金工具目前需要 6 位基金代码，例如 161725");
        }

        URI uri = UriComponentsBuilder.fromUriString(EASTMONEY_FUND_QUOTE_URL)
                .queryParam("pageIndex", "1")
                .queryParam("pageSize", "1")
                .queryParam("appType", "ttjj")
                .queryParam("product", "EFund")
                .queryParam("plat", "Android")
                .queryParam("deviceid", "ai-world-agent")
                .queryParam("Version", "1")
                .queryParam("Fcodes", query)
                .build(true)
                .toUri();

        JsonNode root = parseJson(exchange(uri));
        JsonNode first = root.path("Datas").path(0);
        if (first.isMissingNode() || first.isNull()) {
            throw new IllegalArgumentException("未找到基金代码: " + query);
        }

        return MarketQuoteResponse.builder()
                .assetType(ASSET_TYPE_FUND)
                .query(query)
                .code(defaultIfBlank(text(first, "FCODE"), query))
                .name(defaultIfBlank(text(first, "SHORTNAME"), "未知基金"))
                .market(defaultIfBlank(text(first, "FTYPE"), "基金"))
                .currency("CNY")
                .latestNetValue(decimal(text(first, "DWJZ")))
                .latestNetValueDate(defaultIfBlank(text(first, "PDATE"), text(first, "FSRQ")))
                .estimatedNetValue(decimal(text(first, "GSZ")))
                .changePercent(firstNonNullDecimal(text(first, "GSZZL"), text(first, "NAVCHGRT")))
                .estimatedTime(defaultIfBlank(text(first, "GZTIME"), text(first, "PDATE")))
                .sourceName("天天基金")
                .sourceUrl("https://fund.eastmoney.com/" + defaultIfBlank(text(first, "FCODE"), query) + ".html")
                .note("基金估算仅供参考，实际涨跌以净值披露为准。")
                .build();
    }

    private MarketQuoteResponse getCryptoQuote(String query) {
        String coinId = resolveCryptoId(query);
        URI priceUri = UriComponentsBuilder.fromUriString(COINGECKO_SIMPLE_PRICE_URL)
                .queryParam("ids", coinId)
                .queryParam("vs_currencies", "usd,cny")
                .queryParam("include_market_cap", "true")
                .queryParam("include_24hr_vol", "true")
                .queryParam("include_24hr_change", "true")
                .queryParam("include_last_updated_at", "true")
                .build(true)
                .toUri();

        JsonNode root = parseJson(exchange(priceUri));
        JsonNode coinNode = root.path(coinId);
        if (coinNode.isMissingNode() || coinNode.isNull()) {
            throw new IllegalStateException("未获取到加密货币行情数据");
        }

        return MarketQuoteResponse.builder()
                .assetType(ASSET_TYPE_CRYPTO)
                .query(query)
                .symbol(resolveCryptoSymbol(query, coinId))
                .name(resolveCryptoName(query, coinId))
                .market("Crypto")
                .currency("USD/CNY")
                .price(decimalNode(coinNode, "usd"))
                .secondaryPrice(decimalNode(coinNode, "cny"))
                .changePercent(decimalNode(coinNode, "usd_24h_change"))
                .marketCap(decimalNode(coinNode, "usd_market_cap"))
                .volume(decimalNode(coinNode, "usd_24h_vol"))
                .sourceName("CoinGecko")
                .sourceUrl("https://www.coingecko.com/en/coins/" + coinId)
                .note("价格以美元计价，同时附带人民币换算参考。")
                .build();
    }

    private String resolveCryptoId(String query) {
        String aliasId = CRYPTO_ALIASES.get(query.trim().toLowerCase(Locale.ROOT));
        if (StringUtils.hasText(aliasId)) {
            return aliasId;
        }

        URI searchUri = UriComponentsBuilder.fromUriString(COINGECKO_SEARCH_URL)
                .queryParam("query", query)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        JsonNode root = parseJson(exchange(searchUri));
        JsonNode first = root.path("coins").path(0);
        if (first.isMissingNode() || first.isNull()) {
            throw new IllegalArgumentException("未找到加密货币: " + query);
        }
        String id = text(first, "id");
        if (!StringUtils.hasText(id)) {
            throw new IllegalStateException("加密货币搜索结果缺少 id");
        }
        return id;
    }

    private String resolveCryptoSymbol(String query, String coinId) {
        for (Map.Entry<String, String> entry : CRYPTO_ALIASES.entrySet()) {
            if (entry.getValue().equals(coinId) && entry.getKey().matches("[a-z0-9-]+")) {
                return entry.getKey().toUpperCase(Locale.ROOT);
            }
        }
        return query.trim().toUpperCase(Locale.ROOT);
    }

    private String resolveCryptoName(String query, String coinId) {
        for (Map.Entry<String, String> entry : CRYPTO_ALIASES.entrySet()) {
            if (entry.getValue().equals(coinId) && entry.getKey().matches(".*[\\u4e00-\\u9fa5].*")) {
                return entry.getKey();
            }
        }
        return StringUtils.hasText(query) ? query.trim() : coinId;
    }

    private String exchange(URI uri) {
        try {
            log.info("Fetching market data from {}", uri);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(defaultHeaders()), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                throw new IllegalStateException("上游返回异常: HTTP " + response.getStatusCode().value());
            }
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("获取行情失败: " + e.getMessage(), e);
        }
    }

    private JsonNode parseJson(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            throw new IllegalStateException("行情返回解析失败", e);
        }
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE + ", text/plain"));
        headers.setAcceptCharset(java.util.List.of(StandardCharsets.UTF_8));
        headers.set("User-Agent", "Mozilla/5.0 (compatible; AI-world-market/1.0)");
        headers.set("Referer", "https://www.eastmoney.com/");
        return headers;
    }

    private String normalizeQuery(String query) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("查询内容不能为空");
        }
        return query.trim();
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || fieldName == null) {
            return "";
        }
        JsonNode field = node.path(fieldName);
        if (field.isMissingNode() || field.isNull()) {
            return "";
        }
        return field.asText("");
    }

    private Double number(JsonNode node, String fieldName) {
        if (node == null) {
            return null;
        }
        JsonNode field = node.path(fieldName);
        if (field.isMissingNode() || field.isNull()) {
            return null;
        }
        if (field.isNumber()) {
            return field.doubleValue();
        }
        return decimal(field.asText(""));
    }

    private Double decimalNode(JsonNode node, String fieldName) {
        if (node == null) {
            return null;
        }
        JsonNode field = node.path(fieldName);
        if (field.isMissingNode() || field.isNull()) {
            return null;
        }
        return field.isNumber() ? field.doubleValue() : decimal(field.asText(""));
    }

    private Double scaledNumber(JsonNode node, String fieldName, double divisor) {
        Double value = number(node, fieldName);
        return value == null ? null : value / divisor;
    }

    private Double decimal(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double firstNonNullDecimal(String... candidates) {
        if (candidates == null) {
            return null;
        }
        for (String candidate : candidates) {
            Double value = decimal(candidate);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String inferStockMarket(String secId) {
        if (!StringUtils.hasText(secId)) {
            return "股票";
        }
        if (secId.startsWith("116.") || secId.startsWith("106.")) {
            return "港股";
        }
        if (secId.startsWith("105.") || secId.startsWith("107.")) {
            return "美股";
        }
        if (secId.startsWith("1.")) {
            return "沪市";
        }
        if (secId.startsWith("0.")) {
            return "深市";
        }
        return "股票";
    }

    private String inferStockCurrency(String secId) {
        String market = inferStockMarket(secId);
        return switch (market) {
            case "港股" -> "HKD";
            case "美股" -> "USD";
            default -> "CNY";
        };
    }

    private static Map<String, String> createCryptoAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        aliases.put("btc", "bitcoin");
        aliases.put("bitcoin", "bitcoin");
        aliases.put("比特币", "bitcoin");
        aliases.put("eth", "ethereum");
        aliases.put("ethereum", "ethereum");
        aliases.put("以太坊", "ethereum");
        aliases.put("sol", "solana");
        aliases.put("solana", "solana");
        aliases.put("bnb", "binancecoin");
        aliases.put("xrp", "ripple");
        aliases.put("doge", "dogecoin");
        aliases.put("dogecoin", "dogecoin");
        aliases.put("狗狗币", "dogecoin");
        aliases.put("usdt", "tether");
        aliases.put("tether", "tether");
        return Map.copyOf(aliases);
    }
}
