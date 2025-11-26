package com.example.springboottest.common.dto;

import lombok.Data;

/**
 * 二维码生成请求DTO
 */
@Data
public class QrCodeRequest {
    private String content;
    private Integer width = 300;
    private Integer height = 300;
}
