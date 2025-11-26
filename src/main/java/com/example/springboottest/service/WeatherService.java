package com.example.springboottest.service;

import com.example.springboottest.DTO.WeatherRequest;
import com.example.springboottest.DTO.WeatherResponse;

/**
 * 天气服务接口
 */
public interface WeatherService {
    
    /**
     * 根据城市查询天气信息
     * 
     * @param weatherRequest 天气查询请求
     * @return 天气信息
     */
    WeatherResponse getWeatherByCity(WeatherRequest weatherRequest);
    
    /**
     * 根据城市名称查询天气信息
     * 
     * @param cityName 城市名称
     * @return 天气信息
     */
    WeatherResponse getWeatherByCity(String cityName);
}