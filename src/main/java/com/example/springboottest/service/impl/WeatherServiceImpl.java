package com.example.springboottest.service.impl;

import com.example.springboottest.annotation.Loggale;
import com.example.springboottest.modules.weather.dto.WeatherRequest;
import com.example.springboottest.modules.weather.dto.WeatherResponse;
import com.example.springboottest.modules.weather.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 天气服务实现类
 * 注意：这里使用模拟数据，实际生产环境中应该调用真实的天气API服务
 */
@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {
    
    private final Random random = new Random();
    
    /**
     * 模拟的城市天气数据
     */
    private final Map<String, String[]> cityWeatherMap = new HashMap<String, String[]>() {{
        put("北京", new String[]{"晴", "多云", "阴", "小雨", "大雨", "雪"});
        put("上海", new String[]{"晴", "多云", "阴", "小雨", "雾霾"});
        put("广州", new String[]{"晴", "多云", "阴", "小雨", "大雨", "雷阵雨"});
        put("深圳", new String[]{"晴", "多云", "阴", "小雨", "大雨", "雷阵雨"});
        put("杭州", new String[]{"晴", "多云", "阴", "小雨", "大雨"});
        put("成都", new String[]{"多云", "阴", "小雨", "雾", "霾"});
        put("西安", new String[]{"晴", "多云", "阴", "小雨", "沙尘"});
        put("武汉", new String[]{"晴", "多云", "阴", "小雨", "大雨", "雷阵雨"});
        put("南京", new String[]{"晴", "多云", "阴", "小雨", "大雨"});
        put("天津", new String[]{"晴", "多云", "阴", "小雨", "雾霾"});
    }};
    
    @Override
    public WeatherResponse getWeatherByCity(WeatherRequest weatherRequest) {
        log.info("查询城市 {} 的天气信息", weatherRequest.getCity());
        return generateMockWeatherData(weatherRequest.getCity(), weatherRequest.getCountryCode());
    }

    @Override
    public WeatherResponse getWeatherByCity(String cityName) {
        log.info("查询城市 {} 的天气信息", cityName);
        return generateMockWeatherData(cityName, "CN");
    }
    
    /**
     * 生成模拟天气数据
     * 
     * @param cityName 城市名称
     * @param countryCode 国家代码
     * @return 天气响应数据
     */
    @Loggale("查询城市天气")
    private WeatherResponse generateMockWeatherData(String cityName, String countryCode) {
        // 检查城市是否支持
        if (!cityWeatherMap.containsKey(cityName)) {
            throw new RuntimeException("暂不支持查询城市: " + cityName + " 的天气信息");
        }
        
        String[] weatherOptions = cityWeatherMap.get(cityName);
        String weather = weatherOptions[random.nextInt(weatherOptions.length)];
        
        WeatherResponse response = new WeatherResponse();
        response.setCity(cityName);
        response.setCountry("中国");
        response.setMain(weather);
        response.setDescription(getWeatherDescription(weather));
        response.setTemperature(generateRandomTemperature());
        response.setFeelsLike(response.getTemperature() + random.nextDouble() * 4 - 2); // 体感温度差异-2到+2度
        response.setHumidity(40 + random.nextInt(40)); // 湿度40-80%
        response.setPressure(1000.0 + random.nextDouble() * 50); // 气压1000-1050hPa
        response.setWindSpeed(random.nextDouble() * 10); // 风速0-10m/s
        response.setWindDirection(random.nextInt(360)); // 风向0-360度
        response.setQueryTime(LocalDateTime.now());
        
        log.info("为城市 {} 生成天气数据: {}", cityName, weather);
        return response;
    }
    
    /**
     * 根据天气状况生成详细描述
     */
    private String getWeatherDescription(String weather) {
        Map<String, String> descriptionMap = new HashMap<String, String>() {{
            put("晴", "阳光明媚，天气晴朗");
            put("多云", "多云天气，部分阳光");
            put("阴", "阴天，云层较厚");
            put("小雨", "小雨，请携带雨具");
            put("大雨", "大雨，注意出行安全");
            put("雷阵雨", "雷阵雨，注意避雷");
            put("雪", "下雪天气，注意保暖");
            put("雾霾", "雾霾天气，减少户外活动");
            put("雾", "大雾天气，注意交通安全");
            put("霾", "霾，空气质量较差");
            put("沙尘", "沙尘天气，注意防护");
        }};
        
        return descriptionMap.getOrDefault(weather, "天气状况: " + weather);
    }
    
    /**
     * 生成随机温度（根据季节调整范围）
     */
    private Double generateRandomTemperature() {
        // 这里简化处理，实际可以根据季节、地区等因素调整
        int baseTemp = 15; // 基础温度
        int variance = 20;  // 温度变化范围
        return baseTemp + random.nextDouble() * variance - 5; // 10-25度左右
    }
}