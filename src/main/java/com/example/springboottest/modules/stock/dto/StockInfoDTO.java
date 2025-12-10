package com.example.springboottest.modules.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockInfoDTO {

    private Long id;

    private String stockCode;

    private String stockName;

    private String market;

    private String industry;

    private Integer status;
}
