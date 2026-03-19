package com.example.springboottest.modules.prompt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PromptRenderResponse {

    private String promptCode;
    private String renderedContent;
}
