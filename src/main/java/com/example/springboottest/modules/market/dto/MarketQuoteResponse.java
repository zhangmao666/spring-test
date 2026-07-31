package com.example.springboottest.modules.market.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MarketQuoteResponse {

    String assetType;
    String query;
    String code;
    String symbol;
    String name;
    String market;
    String currency;
    Double price;
    Double secondaryPrice;
    Double change;
    Double changePercent;
    Double open;
    Double high;
    Double low;
    Double previousClose;
    Double estimatedNetValue;
    Double latestNetValue;
    String latestNetValueDate;
    String estimatedTime;
    Double marketCap;
    Double volume;
    Double turnover;
    String sourceName;
    String sourceUrl;
    String note;
}
