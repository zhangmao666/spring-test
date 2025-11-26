package com.example.springboottest.modules.weather.controller;

import com.alibaba.fastjson2.JSONObject;
import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.weather.dto.WeatherRequest;
import com.example.springboottest.modules.weather.dto.WeatherResponse;
import com.example.springboottest.modules.weather.service.WeatherService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 天气查询控制器
 */
@Slf4j
@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
@Validated
public class WeatherController {
    
    private final WeatherService weatherService;
    
    @PostMapping("/query")
    public ApiResponse<WeatherResponse> getWeatherByCity(@Valid @RequestBody WeatherRequest weatherRequest) {
        try {
            String json = "{\"city\": \"成都\", \"country\": \"CN\", \"temperature\": 21.0, \"feelsLike\": 21.5, \"description\": \"晴\", \"main\": \"Clear\", \"humidity\": 60, \"pressure\": 1013.0, \"windSpeed\": 2.5, \"windDirection\": 150, \"queryTime\": \"2025-11-04T14:00:00\"}";
            WeatherResponse weatherResponse = JSONObject.parseObject(json, WeatherResponse.class);
            return ApiResponse.success("查询成功", weatherResponse);
        } catch (Exception e) {
            log.error("查询城市 {} 天气信息失败: {}", weatherRequest.getCity(), e.getMessage());
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/city/{cityName}")
    public ApiResponse<WeatherResponse> getWeatherByCityName(@PathVariable @NotBlank(message = "城市名称不能为空") String cityName) {
        try {
            WeatherResponse weatherResponse = weatherService.getWeatherByCity(cityName);
            return ApiResponse.success("查询成功", weatherResponse);
        } catch (Exception e) {
            log.error("查询城市 {} 天气信息失败: {}", cityName, e.getMessage());
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ApiResponse<WeatherResponse> getWeatherByParams(@RequestParam @NotBlank(message = "城市名称不能为空") String city, @RequestParam(defaultValue = "CN") String countryCode) {
        try {
            WeatherRequest request = new WeatherRequest();
            request.setCity(city);
            request.setCountryCode(countryCode);
            WeatherResponse weatherResponse = weatherService.getWeatherByCity(request);
            return ApiResponse.success("查询成功", weatherResponse);
        } catch (Exception e) {
            log.error("查询城市 {} 天气信息失败: {}", city, e.getMessage());
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/cities")
    public ApiResponse<String[]> getSupportedCities() {
        String[] supportedCities = {"北京", "上海", "广州", "深圳", "杭州", "成都", "西安", "武汉", "南京", "天津"};
        return ApiResponse.success("获取成功", supportedCities);
    }

    @GetMapping("/hasSun")
    public ApiResponse<String> hasSun(@RequestParam String city) {
        String content = city + "今天有太阳！";
        return ApiResponse.success("获取成功", content);
    }
}
