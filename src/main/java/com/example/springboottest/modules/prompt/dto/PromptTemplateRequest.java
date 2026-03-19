package com.example.springboottest.modules.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PromptTemplateRequest {

    @NotBlank(message = "Prompt code cannot be blank")
    @Size(max = 100, message = "Prompt code length cannot exceed 100")
    private String promptCode;

    @NotBlank(message = "Prompt name cannot be blank")
    @Size(max = 100, message = "Prompt name length cannot exceed 100")
    private String promptName;

    @NotBlank(message = "Prompt type cannot be blank")
    @Size(max = 50, message = "Prompt type length cannot exceed 50")
    private String promptType;

    @NotBlank(message = "Prompt content cannot be blank")
    private String promptContent;

    @Size(max = 2000, message = "Variables metadata length cannot exceed 2000")
    private String variables;

    private Integer status;

    @Size(max = 500, message = "Remark length cannot exceed 500")
    private String remark;
}
