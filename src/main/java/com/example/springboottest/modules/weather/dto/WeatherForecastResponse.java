package com.example.springboottest.modules.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 3 天天气预报响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherForecastResponse {

    /** 用户传入的城市名 */
    private String requestedCity;

    /** 实际查询使用的城市名 */
    private String queryCity;

    /** wttr.in 解析后返回的城市名 */
    private String city;

    /** 国家/地区 */
    private String country;

    /** 当前温度(单位:摄氏度) */
    private Double currentTemperature;

    /** 当前天气描述 */
    private String currentDescription;

    /** 数据查询时间 */
    private String queryTime;

    /** 最近 3 天的天气数据 */
    private List<DailyWeather> forecast;

    /**
     * 单日天气数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyWeather {

        /** 日期,例如 2026-06-17 */
        private String date;

        /** 最低温度(摄氏度) */
        private Double minTemperature;

        /** 最高温度(摄氏度) */
        private Double maxTemperature;

        /** 当日总体天气描述 */
        private String description;

        /** 日出时间 */
        private String sunrise;

        /** 日落时间 */
        private String sunset;

        /** 紫外线指数 */
        private Double uvIndex;

        /** 每小时天气数据 */
        private List<HourlyWeather> hourly;
    }

    /**
     * 每小时天气数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyWeather {

        /** 时间(24 小时制),例如 1200 表示 12:00 */
        private String time;

        /** 温度(摄氏度) */
        private Double temperature;

        /** 体感温度(摄氏度) */
        private Double feelsLike;

        /** 天气描述 */
        private String description;

        /** 湿度(%) */
        private Integer humidity;

        /** 风速(km/h) */
        private Double windSpeed;

        /** 风向(度) */
        private Integer windDirection;

        /** 降水量(mm) */
        private Double precipitation;
    }
}
