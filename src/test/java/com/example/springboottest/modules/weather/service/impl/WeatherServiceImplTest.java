package com.example.springboottest.modules.weather.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.example.springboottest.modules.weather.dto.WeatherRequest;
import com.example.springboottest.modules.weather.dto.WeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    private WeatherServiceImpl weatherService;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherServiceImpl(restTemplate);
    }

    @Test
    void shouldParseWttrWeatherResponse() {
        String responseBody = """
                {
                  "current_condition": [
                    {
                      "temp_C": "23",
                      "FeelsLikeC": "25",
                      "humidity": "60",
                      "pressure": "1008",
                      "windspeedKmph": "11",
                      "winddir16Point": "SE",
                      "weatherDesc": [{"value": "Partly cloudy"}]
                    }
                  ],
                  "nearest_area": [
                    {
                      "areaName": [{"value": "Hangzhou"}],
                      "region": [{"value": "Zhejiang"}],
                      "country": [{"value": "China"}]
                    }
                  ]
                }
                """;

        when(restTemplate.exchange(
                eq("https://wttr.in/Hangzhou?format=j1&lang=zh-cn"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok(responseBody));

        WeatherResponse result = weatherService.getWeatherByCity("Hangzhou");

        assertEquals("Hangzhou", result.getCity());
        assertEquals("China / Zhejiang", result.getCountry());
        assertEquals(23.0, result.getTemperature());
        assertEquals(25.0, result.getFeelsLike());
        assertEquals("Partly cloudy", result.getDescription());
        assertEquals(60, result.getHumidity());
        assertEquals(1008.0, result.getPressure());
        assertEquals(11.0, result.getWindSpeed());
        assertEquals(135, result.getWindDirection());
        assertEquals("Hangzhou", result.getQueryCity());
        assertEquals(Boolean.TRUE, result.getLocationMatched());
        assertNotNull(result.getQueryTime());
    }

    @Test
    void shouldSupportRequestDto() {
        String responseBody = """
                {
                  "current_condition": [
                    {
                      "temp_C": "18",
                      "FeelsLikeC": "19",
                      "humidity": "81",
                      "pressure": "1016",
                      "windspeedKmph": "6",
                      "winddir16Point": "N",
                      "weatherDesc": [{"value": "Sunny"}]
                    }
                  ],
                  "nearest_area": [
                    {
                      "areaName": [{"value": "Beijing"}],
                      "region": [{"value": "Beijing"}],
                      "country": [{"value": "China"}]
                    }
                  ]
                }
                """;

        when(restTemplate.exchange(
                eq("https://wttr.in/Beijing?format=j1&lang=zh-cn"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok(responseBody));

        WeatherRequest request = new WeatherRequest();
        request.setCity("北京");

        WeatherResponse result = weatherService.getWeatherByCity(request);

        assertEquals("Beijing", result.getCity());
        assertEquals("北京", result.getRequestedCity());
        assertEquals("Beijing", result.getQueryCity());
        assertEquals("Sunny", result.getMain());
        assertEquals(Boolean.TRUE, result.getLocationMatched());
        assertEquals(0, result.getWindDirection());
    }

    @Test
    void shouldThrowWhenUpstreamStatusIsNotSuccess() {
        when(restTemplate.exchange(
                eq("https://wttr.in/London?format=j1&lang=zh-cn"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("", HttpStatusCode.valueOf(502)));

        try {
            weatherService.getWeatherByCity("London");
        } catch (RuntimeException ex) {
            assertEquals(true, ex.getMessage().contains("获取天气信息失败"));
        }
    }

    @Test
    void shouldParseHelperResponseDirectly() {
        JSONObject json = JSONObject.parseObject("""
                {
                  "current_condition": [
                    {
                      "temp_C": "30",
                      "FeelsLikeC": "33",
                      "humidity": "70",
                      "pressure": "1001",
                      "windspeedKmph": "14",
                      "winddir16Point": "WNW",
                      "weatherDesc": [{"value": "Light rain"}]
                    }
                  ],
                  "nearest_area": [
                    {
                      "areaName": [{"value": "Shenzhen"}],
                      "region": [{"value": "Guangdong"}],
                      "country": [{"value": "China"}]
                    }
                  ]
                }
                """);

        WeatherResponse response = weatherService.parseWeatherResponse(json, "深圳", "Shenzhen");

        assertEquals("Shenzhen", response.getCity());
        assertEquals("深圳", response.getRequestedCity());
        assertEquals("Shenzhen", response.getQueryCity());
        assertEquals(Boolean.TRUE, response.getLocationMatched());
        assertEquals(293, response.getWindDirection());
        assertEquals("Light rain", response.getDescription());
    }

    @Test
    void shouldMarkLocationMismatchWhenResolvedCityIsWrong() {
        JSONObject json = JSONObject.parseObject("""
                {
                  "current_condition": [
                    {
                      "temp_C": "16",
                      "FeelsLikeC": "15",
                      "humidity": "66",
                      "pressure": "1009",
                      "windspeedKmph": "9",
                      "winddir16Point": "W",
                      "weatherDesc": [{"value": "Cloudy"}]
                    }
                  ],
                  "nearest_area": [
                    {
                      "areaName": [{"value": "Bolbosi"}],
                      "region": [{"value": "Mehedinti"}],
                      "country": [{"value": "Romania"}]
                    }
                  ]
                }
                """);

        WeatherResponse response = weatherService.parseWeatherResponse(json, "成都", "Chengdu");

        assertEquals("Bolbosi", response.getCity());
        assertEquals(Boolean.FALSE, response.getLocationMatched());
    }
}
