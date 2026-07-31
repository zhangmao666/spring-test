package com.example.springboottest.modules.weather.service;

import com.example.springboottest.modules.weather.dto.WeatherForecastResponse;
import com.example.springboottest.modules.weather.dto.WeatherRequest;
import com.example.springboottest.modules.weather.dto.WeatherResponse;

/**
 * 天气服务接口
 */
public interface WeatherService {
    WeatherResponse getWeatherByCity(WeatherRequest weatherRequest);
    WeatherResponse getWeatherByCity(String cityName);

    /**
     * 查询最近 3 天的天气数据
     *
     * @param weatherRequest 请求参数
     * @return 3 天天气预报
     */
    WeatherForecastResponse getWeatherForecast(WeatherRequest weatherRequest);

    /**
     * 查询最近 3 天的天气数据
     *
     * @param cityName 城市名称
     * @return 3 天天气预报
     */
    WeatherForecastResponse getWeatherForecast(String cityName);
}
