package com.example.springboottest.modules.ai.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiProviderInfo {
    private String provider;
    private String displayName;
    private List<String> availableModels;
    private boolean available;
    private String description;
}
