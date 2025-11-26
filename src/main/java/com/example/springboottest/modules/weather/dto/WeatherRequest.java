package com.example.springboottest.modules.weather.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 天气查询请求DTO
 */
@Data
public class WeatherRequest {
    @NotBlank(message = "城市名称不能为空")
    private String city;
    private String countryCode = "CN";
}
