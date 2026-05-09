package com.example.springboottest.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSearchSource {

    private String title;

    private String url;

    private String domain;

    private String snippet;

    private boolean fetched;
}
