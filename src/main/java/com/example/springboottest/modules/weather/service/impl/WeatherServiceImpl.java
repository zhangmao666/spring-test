package com.example.springboottest.modules.weather.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.example.springboottest.modules.weather.dto.WeatherRequest;
import com.example.springboottest.modules.weather.dto.WeatherResponse;
import com.example.springboottest.modules.weather.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 天气服务实现类
 * 使用 sojson 免费天气API
 */
@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {
    
    private static final String WEATHER_API_URL = "http://t.weather.sojson.com/api/weather/city/";
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 城市名称到城市ID的映射
     */
    private static final Map<String, String> CITY_ID_MAP = new HashMap<String, String>() {{
        put("北京", "101010100");
        put("上海", "101020100");
        put("广州", "101280101");
        put("深圳", "101280601");
        put("杭州", "101210101");
        put("成都", "101270101");
        put("西安", "101110101");
        put("武汉", "101200101");
        put("南京", "101190101");
        put("天津", "101030100");
        put("重庆", "101040100");
        put("苏州", "101190401");
        put("长沙", "101250101");
        put("郑州", "101180101");
        put("青岛", "101120201");
        put("厦门", "101230201");
        put("昆明", "101290101");
        put("大连", "101070201");
        put("济南", "101120101");
        put("沈阳", "101070101");
        put("哈尔滨", "101050101");
        put("长春", "101060101");
        put("福州", "101230101");
        put("贵阳", "101260101");
        put("太原", "101100101");
        put("石家庄", "101090101");
        put("合肥", "101220101");
        put("南昌", "101240101");
        put("兰州", "101160101");
        put("海口", "101310101");
        put("南宁", "101300101");
        put("拉萨", "101140101");
        put("银川", "101170101");
        put("西宁", "101150101");
        put("呼和浩特", "101080101");
        put("乌鲁木齐", "101130101");
    }};
    
    @Override
    public WeatherResponse getWeatherByCity(WeatherRequest weatherRequest) {
        log.info("查询城市 {} 的天气信息", weatherRequest.getCity());
        return fetchWeatherFromApi(weatherRequest.getCity());
    }

    @Override
    public WeatherResponse getWeatherByCity(String cityName) {
        log.info("查询城市 {} 的天气信息", cityName);
        return fetchWeatherFromApi(cityName);
    }
    
    /**
     * 从真实API获取天气数据
     */
    private WeatherResponse fetchWeatherFromApi(String cityName) {
        String cityId = CITY_ID_MAP.get(cityName);
        if (cityId == null) {
            throw new RuntimeException("暂不支持查询城市: " + cityName + " 的天气信息");
        }
        
        try {
            String url = WEATHER_API_URL + cityId;
            log.info("请求天气API: {}", url);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();
            
            JSONObject json = JSONObject.parseObject(body);
            
            if (json.getInteger("status") != 200) {
                throw new RuntimeException("天气API返回错误: " + json.getString("message"));
            }
            
            return parseWeatherResponse(json, cityName);
            
        } catch (Exception e) {
            log.error("获取天气信息失败: {}", e.getMessage());
            throw new RuntimeException("获取天气信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析天气API响应
     */
    private WeatherResponse parseWeatherResponse(JSONObject json, String cityName) {
        JSONObject cityInfo = json.getJSONObject("cityInfo");
        JSONObject data = json.getJSONObject("data");
        
        // 获取今天的预报
        JSONObject forecast = data.getJSONArray("forecast").getJSONObject(0);
        
        WeatherResponse response = new WeatherResponse();
        response.setCity(cityInfo.getString("city").replace("市", ""));
        response.setCountry("中国");
        
        // 解析温度
        String wendu = data.getString("wendu");
        response.setTemperature(Double.parseDouble(wendu));
        
        // 解析高低温计算体感温度
        String highStr = forecast.getString("high").replaceAll("[^0-9.-]", "");
        String lowStr = forecast.getString("low").replaceAll("[^0-9.-]", "");
        double high = Double.parseDouble(highStr);
        double low = Double.parseDouble(lowStr);
        response.setFeelsLike((high + low) / 2);
        
        // 天气类型和描述
        String weatherType = forecast.getString("type");
        response.setMain(weatherType);
        response.setDescription(forecast.getString("notice"));
        
        // 湿度
        String shidu = data.getString("shidu").replace("%", "");
        response.setHumidity(Integer.parseInt(shidu));
        
        // 气压（API未提供，使用标准气压）
        response.setPressure(1013.0);
        
        // 风速（解析风力等级转换为大致风速）
        String fl = forecast.getString("fl");
        response.setWindSpeed(parseWindSpeed(fl));
        
        // 风向（解析风向转换为角度）
        String fx = forecast.getString("fx");
        response.setWindDirection(parseWindDirection(fx));
        
        response.setQueryTime(LocalDateTime.now());
        
        log.info("成功获取城市 {} 天气: {}，温度: {}°C", cityName, weatherType, wendu);
        return response;
    }
    
    /**
     * 解析风力等级为风速
     */
    private Double parseWindSpeed(String fl) {
        if (fl == null) return 0.0;
        if (fl.contains("3-4")) return 5.0;
        if (fl.contains("4-5")) return 8.0;
        if (fl.contains("5-6")) return 11.0;
        if (fl.contains("<3") || fl.contains("微风")) return 2.0;
        return 3.0;
    }
    
    /**
     * 解析风向为角度
     */
    private Integer parseWindDirection(String fx) {
        if (fx == null) return 0;
        if (fx.contains("北")) {
            if (fx.contains("东")) return 45;
            if (fx.contains("西")) return 315;
            return 0;
        }
        if (fx.contains("南")) {
            if (fx.contains("东")) return 135;
            if (fx.contains("西")) return 225;
            return 180;
        }
        if (fx.contains("东")) return 90;
        if (fx.contains("西")) return 270;
        return 0;
    }
}
