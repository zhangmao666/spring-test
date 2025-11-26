package com.example.springboottest.DTO;

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
    
    /**
     * 城市名称
     */
    private String city;
    
    /**
     * 国家
     */
    private String country;
    
    /**
     * 当前温度（摄氏度）
     */
    private Double temperature;
    
    /**
     * 体感温度（摄氏度）
     */
    private Double feelsLike;
    
    /**
     * 天气描述
     */
    private String description;
    
    /**
     * 天气主要状况
     */
    private String main;
    
    /**
     * 湿度（百分比）
     */
    private Integer humidity;
    
    /**
     * 气压（hPa）
     */
    private Double pressure;
    
    /**
     * 风速（m/s）
     */
    private Double windSpeed;
    
    /**
     * 风向（度）
     */
    private Integer windDirection;
    
    /**
     * 查询时间
     */
    private LocalDateTime queryTime;
}