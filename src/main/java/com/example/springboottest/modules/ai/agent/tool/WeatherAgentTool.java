package com.example.springboottest.modules.ai.agent.tool;

import com.example.springboottest.modules.weather.dto.WeatherResponse;
import com.example.springboottest.modules.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WeatherAgentTool implements AgentToolExecutor {

    private final WeatherService weatherService;

    @Override
    public AgentToolDefinition definition() {
        return AgentToolDefinition.builder()
                .id("weather")
                .name("get_weather")
                .description("获取指定城市的天气信息，输入需要包含 city 字段。")
                .parametersSchema(AgentToolSchemas.object(
                        Map.of("city", AgentToolSchemas.stringProperty("城市名称，例如 上海、北京、东京")),
                        List.of("city")
                ))
                .build();
    }

    @Override
    public AgentToolResult execute(Map<String, Object> arguments) {
        String city = value(arguments, "city", "上海");
        try {
            WeatherResponse response = weatherService.getWeatherByCity(city);
            if (Boolean.FALSE.equals(response.getLocationMatched())) {
                return AgentToolResult.success("""
                        天气工具定位失败：未能可靠定位到你请求的城市。
                        请求城市：%s
                        实际定位：%s
                        国家/地区：%s
                        """.formatted(city, fallback(response.getCity(), "未知"), fallback(response.getCountry(), "未知")).trim());
            }
            return AgentToolResult.success("""
                    请求城市：%s
                    查询城市：%s
                    城市：%s
                    国家/地区：%s
                    天气：%s
                    温度：%s°C
                    体感温度：%s°C
                    湿度：%s%%
                    风速：%s km/h
                    查询时间：%s
                    """.formatted(
                    fallback(response.getRequestedCity(), city),
                    fallback(response.getQueryCity(), city),
                    fallback(response.getCity(), city),
                    fallback(response.getCountry(), "未知"),
                    fallback(response.getDescription(), "未知"),
                    formatNumber(response.getTemperature()),
                    formatNumber(response.getFeelsLike()),
                    response.getHumidity() == null ? "未知" : response.getHumidity(),
                    formatNumber(response.getWindSpeed()),
                    response.getQueryTime() == null ? "未知" : response.getQueryTime().toString()
            ).trim());
        } catch (Exception e) {
            return AgentToolResult.error("天气查询失败：" + fallback(e.getMessage(), "未知错误"));
        }
    }

    private String value(Map<String, Object> arguments, String key, String fallback) {
        Object value = arguments == null ? null : arguments.get(key);
        return StringUtils.hasText(value == null ? null : String.valueOf(value)) ? String.valueOf(value).trim() : fallback;
    }

    private String fallback(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String formatNumber(Double value) {
        return value == null ? "未知" : String.format("%.1f", value);
    }
}
