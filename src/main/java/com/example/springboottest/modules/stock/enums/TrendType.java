package com.example.springboottest.modules.stock.enums;

import lombok.Getter;

@Getter
public enum TrendType {

    UP("UP", "上涨趋势", "看涨"),
    DOWN("DOWN", "下跌趋势", "看跌"),
    STABLE("STABLE", "平稳震荡", "中性");

    private final String code;
    private final String description;
    private final String sentiment;

    TrendType(String code, String description, String sentiment) {
        this.code = code;
        this.description = description;
        this.sentiment = sentiment;
    }

    public static TrendType fromCode(String code) {
        for (TrendType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return STABLE;
    }
}
