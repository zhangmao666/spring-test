package com.example.springboottest.modules.prompt.dto;

import lombok.Data;

@Data
public class PromptTemplateQueryRequest {

    private String promptCode;
    private String promptName;
    private String promptType;
    private Integer status;
    private int page = 0;
    private int size = 10;
}
