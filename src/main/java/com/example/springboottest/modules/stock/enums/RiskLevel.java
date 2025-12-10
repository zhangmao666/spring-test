package com.example.springboottest.modules.stock.enums;

import lombok.Getter;

@Getter
public enum RiskLevel {

    LOW("LOW", "低风险", "适合稳健投资者"),
    MEDIUM("MEDIUM", "中等风险", "需谨慎关注"),
    HIGH("HIGH", "高风险", "不建议投资");

    private final String code;
    private final String name;
    private final String suggestion;

    RiskLevel(String code, String name, String suggestion) {
        this.code = code;
        this.name = name;
        this.suggestion = suggestion;
    }

    public static RiskLevel fromCode(String code) {
        for (RiskLevel level : values()) {
            if (level.code.equalsIgnoreCase(code)) {
                return level;
            }
        }
        return MEDIUM;
    }
}
