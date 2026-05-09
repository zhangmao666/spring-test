package com.example.springboottest.modules.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotNewsItemResponse {

    private Integer rank;

    private String title;

    private String url;

    private String hotValue;

    private String source;
}
