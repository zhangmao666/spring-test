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
public class ReferenceBlock implements ContentBlock {

    private String title;
    private String url;
    private String summary;

    @Builder.Default
    private Map<String, Object> metadata = Map.of();

    @Override
    public ContentBlockType getType() {
        return ContentBlockType.REFERENCE;
    }
}
