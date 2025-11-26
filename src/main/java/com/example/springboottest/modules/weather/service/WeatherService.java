package com.example.springboottest.modules.weather.service;

import com.example.springboottest.modules.weather.dto.WeatherRequest;
import com.example.springboottest.modules.weather.dto.WeatherResponse;

/**
 * 天气服务接口
 */
public interface WeatherService {
    WeatherResponse getWeatherByCity(WeatherRequest weatherRequest);
    WeatherResponse getWeatherByCity(String cityName);
}
