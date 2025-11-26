package com.example.springboottest.DTO;

import lombok.Data;

/**
 * 二维码生成请求DTO
 */
@Data
public class QrCodeRequest {
    
    /**
     * 二维码内容
     */
    private String content;
    
    /**
     * 图片宽度（像素）
     */
    private Integer width = 300;
    
    /**
     * 图片高度（像素）
     */
    private Integer height = 300;
}