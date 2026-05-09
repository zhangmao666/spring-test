package com.example.springboottest.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCapabilitiesResponse {

    private boolean webSearchEnabled;

    private String webSearchMode;
}
