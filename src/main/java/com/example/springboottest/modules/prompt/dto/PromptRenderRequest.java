package com.example.springboottest.modules.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class PromptRenderRequest {

    @NotBlank(message = "Prompt code cannot be blank")
    private String promptCode;

    private Map<String, Object> variables;
}
