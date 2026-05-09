package com.example.springboottest.modules.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotNewsResponse {

    private String platform;

    private String platformName;

    private String updateTime;

    private List<HotNewsItemResponse> items;
}
