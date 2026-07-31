package com.example.springboottest.modules.weather.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.springboottest.modules.weather.dto.WeatherForecastResponse;
import com.example.springboottest.modules.weather.dto.WeatherRequest;
import com.example.springboottest.modules.weather.dto.WeatherResponse;
import com.example.springboottest.modules.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 天气服务实现类
 * 使用 wttr.in 免费天气接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    static final String WTTR_API_URL = "https://wttr.in/%s?format=j1&lang=zh-cn";
    static final String WTTR_DEFAULT_API_URL = "https://wttr.in/?format=j1&lang=zh-cn";

    private static final Map<String, String> CITY_QUERY_ALIASES = createCityQueryAliases();

    private final RestTemplate restTemplate;

    @Override
    public WeatherResponse getWeatherByCity(WeatherRequest weatherRequest) {
        if (weatherRequest == null || !StringUtils.hasText(weatherRequest.getCity())) {
            throw new RuntimeException("城市名称不能为空");
        }
        return fetchWeather(weatherRequest.getCity());
    }

    @Override
    public WeatherResponse getWeatherByCity(String cityName) {
        if (!StringUtils.hasText(cityName)) {
            throw new RuntimeException("城市名称不能为空");
        }
        return fetchWeather(cityName);
    }

    @Override
    public WeatherForecastResponse getWeatherForecast(WeatherRequest weatherRequest) {
        if (weatherRequest == null || !StringUtils.hasText(weatherRequest.getCity())) {
            throw new RuntimeException("城市名称不能为空");
        }
        return fetchForecast(weatherRequest.getCity());
    }

    @Override
    public WeatherForecastResponse getWeatherForecast(String cityName) {
        if (!StringUtils.hasText(cityName)) {
            throw new RuntimeException("城市名称不能为空");
        }
        return fetchForecast(cityName);
    }

    private WeatherResponse fetchWeather(String cityName) {
        String normalizedCity = cityName.trim();
        String queryCity = normalizeQueryCity(normalizedCity);
        String encodedCity = UriUtils.encodePathSegment(queryCity, StandardCharsets.UTF_8);
        String url = StringUtils.hasText(encodedCity)
                ? WTTR_API_URL.formatted(encodedCity)
                : WTTR_DEFAULT_API_URL;

        try {
            log.info("查询 wttr 天气信息, requestedCity={}, queryCity={}, url={}", normalizedCity, queryCity, url);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "Mozilla/5.0 (compatible; AI-world-weather/1.0)");

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                throw new RuntimeException("天气服务返回异常: HTTP " + response.getStatusCode().value());
            }

            JSONObject json = JSONObject.parseObject(response.getBody());
            return parseWeatherResponse(json, normalizedCity, queryCity);
        } catch (Exception e) {
            log.error("获取城市 {} 天气信息失败: {}", normalizedCity, e.getMessage(), e);
            throw new RuntimeException("获取天气信息失败: " + e.getMessage(), e);
        }
    }

    WeatherResponse parseWeatherResponse(JSONObject json, String requestedCity, String queryCity) {
        if (json == null) {
            throw new RuntimeException("天气数据为空");
        }

        JSONObject current = firstObject(json.getJSONArray("current_condition"));
        JSONObject nearestArea = firstObject(json.getJSONArray("nearest_area"));
        if (current == null) {
            throw new RuntimeException("天气数据缺少 current_condition");
        }

        String city = readNestedValue(nearestArea, "areaName");
        String country = readNestedValue(nearestArea, "country");
        String region = readNestedValue(nearestArea, "region");
        String weatherDesc = readNestedValue(current, "weatherDesc");

        WeatherResponse response = new WeatherResponse();
        response.setRequestedCity(requestedCity);
        response.setQueryCity(queryCity);
        response.setCity(StringUtils.hasText(city) ? city : requestedCity);
        response.setCountry(buildCountry(country, region));
        response.setTemperature(parseDouble(current.getString("temp_C")));
        response.setFeelsLike(parseDouble(current.getString("FeelsLikeC")));
        response.setDescription(StringUtils.hasText(weatherDesc) ? weatherDesc : "未知");
        response.setMain(StringUtils.hasText(weatherDesc) ? weatherDesc : "未知");
        response.setHumidity(parseInteger(current.getString("humidity")));
        response.setPressure(parseDouble(current.getString("pressure")));
        response.setWindSpeed(parseDouble(current.getString("windspeedKmph")));
        response.setWindDirection(parseWindDirection(current.getString("winddir16Point")));
        response.setLocationMatched(isLocationMatched(requestedCity, response.getCity(), country));
        response.setQueryTime(LocalDateTime.now());
        return response;
    }

    private WeatherForecastResponse fetchForecast(String cityName) {
        String normalizedCity = cityName.trim();
        String queryCity = normalizeQueryCity(normalizedCity);
        String encodedCity = UriUtils.encodePathSegment(queryCity, StandardCharsets.UTF_8);
        String url = StringUtils.hasText(encodedCity)
                ? WTTR_API_URL.formatted(encodedCity)
                : WTTR_DEFAULT_API_URL;

        try {
            log.info("查询 wttr 3 天天气, requestedCity={}, queryCity={}, url={}", normalizedCity, queryCity, url);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "Mozilla/5.0 (compatible; AI-world-weather/1.0)");

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                throw new RuntimeException("天气服务返回异常: HTTP " + response.getStatusCode().value());
            }

            JSONObject json = JSONObject.parseObject(response.getBody());
            return parseForecastResponse(json, normalizedCity, queryCity);
        } catch (Exception e) {
            log.error("获取城市 {} 3 天天气信息失败: {}", normalizedCity, e.getMessage(), e);
            throw new RuntimeException("获取 3 天天气信息失败: " + e.getMessage(), e);
        }
    }

    WeatherForecastResponse parseForecastResponse(JSONObject json, String requestedCity, String queryCity) {
        if (json == null) {
            throw new RuntimeException("天气数据为空");
        }

        JSONObject current = firstObject(json.getJSONArray("current_condition"));
        JSONObject nearestArea = firstObject(json.getJSONArray("nearest_area"));
        JSONArray weatherArray = json.getJSONArray("weather");

        if (weatherArray == null || weatherArray.isEmpty()) {
            throw new RuntimeException("天气数据缺少 weather 数组");
        }

        String city = readNestedValue(nearestArea, "areaName");
        String country = readNestedValue(nearestArea, "country");
        String region = readNestedValue(nearestArea, "region");
        String currentDesc = readNestedValue(current, "weatherDesc");

        List<WeatherForecastResponse.DailyWeather> forecast = new ArrayList<>();
        int dayCount = Math.min(3, weatherArray.size());
        for (int i = 0; i < dayCount; i++) {
            forecast.add(parseDailyWeather(weatherArray.getJSONObject(i)));
        }

        return WeatherForecastResponse.builder()
                .requestedCity(requestedCity)
                .queryCity(queryCity)
                .city(StringUtils.hasText(city) ? city : requestedCity)
                .country(buildCountry(country, region))
                .currentTemperature(parseDouble(current == null ? null : current.getString("temp_C")))
                .currentDescription(StringUtils.hasText(currentDesc) ? currentDesc : "未知")
                .queryTime(LocalDateTime.now().toString())
                .forecast(forecast)
                .build();
    }

    private WeatherForecastResponse.DailyWeather parseDailyWeather(JSONObject dailyJson) {
        if (dailyJson == null) {
            return null;
        }

        JSONArray hourlyArray = dailyJson.getJSONArray("hourly");
        List<WeatherForecastResponse.HourlyWeather> hourlyList = new ArrayList<>();
        if (hourlyArray != null) {
            for (int i = 0; i < hourlyArray.size(); i++) {
                hourlyList.add(parseHourlyWeather(hourlyArray.getJSONObject(i)));
            }
        }

        // 取正午时段(1200)的天气描述作为当日总览,无则用第一个小时
        String dayDescription = "";
        if (hourlyArray != null && !hourlyArray.isEmpty()) {
            JSONObject noon = findHourlyByTime(hourlyArray, "1200");
            if (noon == null) {
                noon = hourlyArray.getJSONObject(0);
            }
            dayDescription = readNestedValue(noon, "weatherDesc");
        }

        return WeatherForecastResponse.DailyWeather.builder()
                .date(dailyJson.getString("date"))
                .minTemperature(parseDouble(dailyJson.getString("mintempC")))
                .maxTemperature(parseDouble(dailyJson.getString("maxtempC")))
                .description(StringUtils.hasText(dayDescription) ? dayDescription : "未知")
                .sunrise(extractAstronomyField(dailyJson, 0))
                .sunset(extractAstronomyField(dailyJson, 1))
                .uvIndex(parseDouble(dailyJson.getString("uvIndex")))
                .hourly(hourlyList)
                .build();
    }

    private JSONObject findHourlyByTime(JSONArray hourlyArray, String time) {
        for (int i = 0; i < hourlyArray.size(); i++) {
            JSONObject item = hourlyArray.getJSONObject(i);
            if (item != null && time.equals(item.getString("time"))) {
                return item;
            }
        }
        return null;
    }

    private WeatherForecastResponse.HourlyWeather parseHourlyWeather(JSONObject hourlyJson) {
        if (hourlyJson == null) {
            return null;
        }
        String desc = readNestedValue(hourlyJson, "weatherDesc");

        return WeatherForecastResponse.HourlyWeather.builder()
                .time(hourlyJson.getString("time"))
                .temperature(parseDouble(hourlyJson.getString("tempC")))
                .feelsLike(parseDouble(hourlyJson.getString("FeelsLikeC")))
                .description(StringUtils.hasText(desc) ? desc : "未知")
                .humidity(parseInteger(hourlyJson.getString("humidity")))
                .windSpeed(parseDouble(hourlyJson.getString("windspeedKmph")))
                .windDirection(parseWindDirection(hourlyJson.getString("winddir16Point")))
                .precipitation(parseDouble(hourlyJson.getString("precipMM")))
                .build();
    }

    private String extractAstronomyField(JSONObject dailyJson, int index) {
        if (dailyJson == null) {
            return "";
        }
        JSONArray astronomyArray = dailyJson.getJSONArray("astronomy");
        if (astronomyArray == null || astronomyArray.size() <= index) {
            return "";
        }
        JSONObject entry = astronomyArray.getJSONObject(index);
        if (entry == null) {
            return "";
        }
        return entry.getString("value");
    }

    private String normalizeQueryCity(String cityName) {
        String compactName = cityName.replace("市", "").trim();
        return CITY_QUERY_ALIASES.getOrDefault(compactName, cityName.trim());
    }

    private JSONObject firstObject(JSONArray array) {
        if (array == null || array.isEmpty()) {
            return null;
        }
        return array.getJSONObject(0);
    }

    private String readNestedValue(JSONObject parent, String fieldName) {
        if (parent == null || !parent.containsKey(fieldName)) {
            return "";
        }
        JSONArray array = parent.getJSONArray(fieldName);
        if (array == null || array.isEmpty()) {
            return "";
        }
        JSONObject first = array.getJSONObject(0);
        return first == null ? "" : first.getString("value");
    }

    private Double parseDouble(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseWindDirection(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "N" -> 0;
            case "NNE" -> 23;
            case "NE" -> 45;
            case "ENE" -> 68;
            case "E" -> 90;
            case "ESE" -> 113;
            case "SE" -> 135;
            case "SSE" -> 158;
            case "S" -> 180;
            case "SSW" -> 203;
            case "SW" -> 225;
            case "WSW" -> 248;
            case "W" -> 270;
            case "WNW" -> 293;
            case "NW" -> 315;
            case "NNW" -> 338;
            default -> null;
        };
    }

    private String buildCountry(String country, String region) {
        if (StringUtils.hasText(country) && StringUtils.hasText(region)) {
            return country + " / " + region;
        }
        if (StringUtils.hasText(country)) {
            return country;
        }
        return region;
    }

    private boolean isLocationMatched(String requestedCity, String resolvedCity, String country) {
        if (!StringUtils.hasText(requestedCity) || !StringUtils.hasText(resolvedCity)) {
            return false;
        }

        String normalizedRequested = requestedCity.replace("市", "").trim().toLowerCase(Locale.ROOT);
        String normalizedResolved = resolvedCity.trim().toLowerCase(Locale.ROOT);
        if (normalizedRequested.equals(normalizedResolved)) {
            return true;
        }

        String alias = CITY_QUERY_ALIASES.getOrDefault(requestedCity.replace("市", "").trim(), "");
        if (StringUtils.hasText(alias) && alias.equalsIgnoreCase(resolvedCity)) {
            return true;
        }

        return containsChinese(normalizedRequested) && StringUtils.hasText(country) && country.toLowerCase(Locale.ROOT).contains("china");
    }

    private boolean containsChinese(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.UnicodeScript.of(text.charAt(i)) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, String> createCityQueryAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        aliases.put("北京", "Beijing");
        aliases.put("上海", "Shanghai");
        aliases.put("广州", "Guangzhou");
        aliases.put("深圳", "Shenzhen");
        aliases.put("杭州", "Hangzhou");
        aliases.put("成都", "Chengdu");
        aliases.put("西安", "Xi'an");
        aliases.put("武汉", "Wuhan");
        aliases.put("南京", "Nanjing");
        aliases.put("天津", "Tianjin");
        aliases.put("重庆", "Chongqing");
        aliases.put("苏州", "Suzhou");
        aliases.put("长沙", "Changsha");
        aliases.put("郑州", "Zhengzhou");
        aliases.put("青岛", "Qingdao");
        aliases.put("厦门", "Xiamen");
        return Map.copyOf(aliases);
    }
}
