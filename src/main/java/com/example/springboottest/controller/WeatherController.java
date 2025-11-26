package com.example.springboottest.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.springboottest.DTO.ApiResponse;
import com.example.springboottest.DTO.WeatherRequest;
import com.example.springboottest.DTO.WeatherResponse;
import com.example.springboottest.annotation.Loggale;
import com.example.springboottest.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

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
    
    /**
     * 根据城市查询天气信息（POST请求）
     * 
     * @param weatherRequest 天气查询请求
     * @return 天气信息响应
     */
//    @PostMapping("/query")
//    public ApiResponse<WeatherResponse> getWeatherByCity(@Valid @RequestBody WeatherRequest weatherRequest) {
//        try {
//            WeatherResponse weatherResponse = weatherService.getWeatherByCity(weatherRequest);
//            return ApiResponse.success("查询成功", weatherResponse);
//        } catch (Exception e) {
//            log.error("查询城市 {} 天气信息失败: {}", weatherRequest.getCity(), e.getMessage());
//            return ApiResponse.error("查询失败: " + e.getMessage());
//        }
//    }

    @PostMapping("/query")
    public ApiResponse<WeatherResponse> getWeatherByCity(@Valid @RequestBody WeatherRequest weatherRequest) {
        try {
            String json ="  {\n" +
                    "    \"city\": \"成都\",\n" +
                    "    \"country\": \"CN\",\n" +
                    "    \"temperature\": 21.0,\n" +
                    "    \"feelsLike\": 21.5,\n" +
                    "    \"description\": \"晴\",\n" +
                    "    \"main\": \"Clear\",\n" +
                    "    \"humidity\": 60,\n" +
                    "    \"pressure\": 1013.0,\n" +
                    "    \"windSpeed\": 2.5,\n" +
                    "    \"windDirection\": 150,\n" +
                    "    \"queryTime\": \"2025-11-04T14:00:00\"\n" +
                    "  }";
            WeatherResponse weatherResponse = JSONObject.parseObject(json, WeatherResponse.class);
            return ApiResponse.success("查询成功", weatherResponse);
        } catch (Exception e) {
            log.error("查询城市 {} 天气信息失败: {}", weatherRequest.getCity(), e.getMessage());
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

//    @PostMapping("/query")
//    public ApiResponse<JSONArray> getWeatherByCity(@Valid @RequestBody WeatherRequest weatherRequest) {
//        try {
//            String array ="[\n" +
//                    "  {\n" +
//                    "    \"city\": \"成都\",\n" +
//                    "    \"country\": \"CN\",\n" +
//                    "    \"temperature\": 22.5,\n" +
//                    "    \"feelsLike\": 23.1,\n" +
//                    "    \"description\": \"多云\",\n" +
//                    "    \"main\": \"Clouds\",\n" +
//                    "    \"humidity\": 65,\n" +
//                    "    \"pressure\": 1012.3,\n" +
//                    "    \"windSpeed\": 2.1,\n" +
//                    "    \"windDirection\": 180,\n" +
//                    "    \"queryTime\": \"2025-11-01T14:00:00\"\n" +
//                    "  },\n" +
//                    "  {\n" +
//                    "    \"city\": \"成都\",\n" +
//                    "    \"country\": \"CN\",\n" +
//                    "    \"temperature\": 20.8,\n" +
//                    "    \"feelsLike\": 21.0,\n" +
//                    "    \"description\": \"小雨\",\n" +
//                    "    \"main\": \"Rain\",\n" +
//                    "    \"humidity\": 78,\n" +
//                    "    \"pressure\": 1010.5,\n" +
//                    "    \"windSpeed\": 1.8,\n" +
//                    "    \"windDirection\": 210,\n" +
//                    "    \"queryTime\": \"2025-11-02T14:00:00\"\n" +
//                    "  },\n" +
//                    "  {\n" +
//                    "    \"city\": \"成都\",\n" +
//                    "    \"country\": \"CN\",\n" +
//                    "    \"temperature\": 19.2,\n" +
//                    "    \"feelsLike\": 18.7,\n" +
//                    "    \"description\": \"阴\",\n" +
//                    "    \"main\": \"Clouds\",\n" +
//                    "    \"humidity\": 82,\n" +
//                    "    \"pressure\": 1009.8,\n" +
//                    "    \"windSpeed\": 1.2,\n" +
//                    "    \"windDirection\": 200,\n" +
//                    "    \"queryTime\": \"2025-11-03T14:00:00\"\n" +
//                    "  },\n" +
//                    "  {\n" +
//                    "    \"city\": \"成都\",\n" +
//                    "    \"country\": \"CN\",\n" +
//                    "    \"temperature\": 21.0,\n" +
//                    "    \"feelsLike\": 21.5,\n" +
//                    "    \"description\": \"晴\",\n" +
//                    "    \"main\": \"Clear\",\n" +
//                    "    \"humidity\": 60,\n" +
//                    "    \"pressure\": 1013.0,\n" +
//                    "    \"windSpeed\": 2.5,\n" +
//                    "    \"windDirection\": 150,\n" +
//                    "    \"queryTime\": \"2025-11-04T14:00:00\"\n" +
//                    "  },\n" +
//                    "  {\n" +
//                    "    \"city\": \"成都\",\n" +
//                    "    \"country\": \"CN\",\n" +
//                    "    \"temperature\": 23.4,\n" +
//                    "    \"feelsLike\": 24.0,\n" +
//                    "    \"description\": \"晴\",\n" +
//                    "    \"main\": \"Clear\",\n" +
//                    "    \"humidity\": 55,\n" +
//                    "    \"pressure\": 1014.2,\n" +
//                    "    \"windSpeed\": 3.0,\n" +
//                    "    \"windDirection\": 160,\n" +
//                    "    \"queryTime\": \"2025-11-05T14:00:00\"\n" +
//                    "  },\n" +
//                    "  {\n" +
//                    "    \"city\": \"成都\",\n" +
//                    "    \"country\": \"CN\",\n" +
//                    "    \"temperature\": 22.1,\n" +
//                    "    \"feelsLike\": 22.8,\n" +
//                    "    \"description\": \"多云\",\n" +
//                    "    \"main\": \"Clouds\",\n" +
//                    "    \"humidity\": 68,\n" +
//                    "    \"pressure\": 1011.7,\n" +
//                    "    \"windSpeed\": 2.3,\n" +
//                    "    \"windDirection\": 170,\n" +
//                    "    \"queryTime\": \"2025-11-06T14:00:00\"\n" +
//                    "  },\n" +
//                    "  {\n" +
//                    "    \"city\": \"成都\",\n" +
//                    "    \"country\": \"CN\",\n" +
//                    "    \"temperature\": 18.9,\n" +
//                    "    \"feelsLike\": 18.2,\n" +
//                    "    \"description\": \"中雨\",\n" +
//                    "    \"main\": \"Rain\",\n" +
//                    "    \"humidity\": 88,\n" +
//                    "    \"pressure\": 1008.4,\n" +
//                    "    \"windSpeed\": 3.5,\n" +
//                    "    \"windDirection\": 220,\n" +
//                    "    \"queryTime\": \"2025-11-07T14:00:00\"\n" +
//                    "  }\n" +
//                    "]";
//            JSONArray jsonArray = JSONArray.parseArray(array);
//            return ApiResponse.success("查询成功", jsonArray);
//        } catch (Exception e) {
//            log.error("查询城市 {} 天气信息失败: {}", weatherRequest.getCity(), e.getMessage());
//            return ApiResponse.error("查询失败: " + e.getMessage());
//        }
//    }

    
    /**
     * 根据城市名称查询天气信息（GET请求）
     * 
     * @param cityName 城市名称
     * @return 天气信息响应
     */
    @GetMapping("/city/{cityName}")
    public ApiResponse<WeatherResponse> getWeatherByCityName(
            @PathVariable @NotBlank(message = "城市名称不能为空") String cityName) {
        try {
            WeatherResponse weatherResponse = weatherService.getWeatherByCity(cityName);
            return ApiResponse.success("查询成功", weatherResponse);
        } catch (Exception e) {
            log.error("查询城市 {} 天气信息失败: {}", cityName, e.getMessage());
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据查询参数获取天气信息（GET请求）
     * 
     * @param city 城市名称
     * @param countryCode 国家代码（可选）
     * @return 天气信息响应
     */
    @GetMapping
    public ApiResponse<WeatherResponse> getWeatherByParams(
            @RequestParam @NotBlank(message = "城市名称不能为空") String city,
            @RequestParam(defaultValue = "CN") String countryCode) {
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
    
    /**
     * 获取支持查询的城市列表
     * 
     * @return 支持的城市列表
     */
    @GetMapping("/cities")
    public ApiResponse<String[]> getSupportedCities() {
        String[] supportedCities = {
            "北京", "上海", "广州", "深圳", "杭州", 
            "成都", "西安", "武汉", "南京", "天津"
        };
        return ApiResponse.success("获取成功", supportedCities);
    }


    @GetMapping("/hasSun")
    public ApiResponse<String> hasSun(@RequestParam String city) {
        String content = city + "今天有太阳！";
        return ApiResponse.success("获取成功", content);
    }
}