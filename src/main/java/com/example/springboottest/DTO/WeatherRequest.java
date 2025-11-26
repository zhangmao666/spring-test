package com.example.springboottest.DTO;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 天气查询请求DTO
 */
@Data
public class WeatherRequest {
    
    /**
     * 城市名称
     */
    @NotBlank(message = "城市名称不能为空")
    private String city;
    
    /**
     * 国家代码（可选，默认为CN）
     */
    private String countryCode = "CN";
}