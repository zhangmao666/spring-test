package com.example.springboottest.modules.ai.agent.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentMessage {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String name;
    private AgentRole role;

    @Builder.Default
    private List<ContentBlock> blocks = List.of();

    @Builder.Default
    private Map<String, Object> metadata = Map.of();

    @Builder.Default
    private Instant timestamp = Instant.now();

    public static AgentMessage text(AgentRole role, String text) {
        return AgentMessage.builder()
                .role(role)
                .blocks(List.of(TextBlock.builder().text(text).build()))
                .build();
    }

    public String textContent() {
        if (blocks == null || blocks.isEmpty()) {
            return "";
        }
        return blocks.stream()
                .filter(TextBlock.class::isInstance)
                .map(TextBlock.class::cast)
                .map(TextBlock::getText)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("\n"))
                .trim();
    }
}
