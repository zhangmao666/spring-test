package com.example.springboottest.modules.ai.agent.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextBlock implements ContentBlock {

    private String text;

    @Builder.Default
    private Map<String, Object> metadata = Map.of();

    @Override
    public ContentBlockType getType() {
        return ContentBlockType.TEXT;
    }
}
