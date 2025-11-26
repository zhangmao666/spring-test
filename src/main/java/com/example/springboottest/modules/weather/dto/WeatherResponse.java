package com.example.springboottest.modules.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 天气查询响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    private String city;
    private String country;
    private Double temperature;
    private Double feelsLike;
    private String description;
    private String main;
    private Integer humidity;
    private Double pressure;
    private Double windSpeed;
    private Integer windDirection;
    private LocalDateTime queryTime;
}
