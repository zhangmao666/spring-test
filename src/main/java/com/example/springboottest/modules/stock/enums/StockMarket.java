package com.example.springboottest.modules.stock.enums;

import lombok.Getter;

@Getter
public enum StockMarket {

    SH("SH", "上海证券交易所", "sh"),
    SZ("SZ", "深圳证券交易所", "sz");

    private final String code;
    private final String name;
    private final String prefix;

    StockMarket(String code, String name, String prefix) {
        this.code = code;
        this.name = name;
        this.prefix = prefix;
    }

    public static StockMarket fromCode(String code) {
        for (StockMarket market : values()) {
            if (market.code.equalsIgnoreCase(code)) {
                return market;
            }
        }
        throw new IllegalArgumentException("无效的市场代码: " + code);
    }

    public static String getApiCode(String stockCode) {
        if (stockCode.startsWith("sh") || stockCode.startsWith("SH")) {
            return SH.prefix + stockCode.substring(2);
        } else if (stockCode.startsWith("sz") || stockCode.startsWith("SZ")) {
            return SZ.prefix + stockCode.substring(2);
        } else if (stockCode.startsWith("6")) {
            return SH.prefix + stockCode;
        } else if (stockCode.startsWith("0") || stockCode.startsWith("3")) {
            return SZ.prefix + stockCode;
        }
        throw new IllegalArgumentException("无法识别的股票代码: " + stockCode);
    }
}
