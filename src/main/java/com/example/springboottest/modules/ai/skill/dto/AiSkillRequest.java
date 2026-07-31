package com.example.springboottest.modules.ai.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiSkillRequest {

    @NotBlank(message = "技能标识不能为空")
    @Size(max = 100, message = "技能标识不能超过100个字符")
    private String skillKey;

    @NotBlank(message = "技能名称不能为空")
    @Size(max = 120, message = "技能名称不能超过120个字符")
    private String name;

    private String description;
    private String originType;
    private String skillType;
    private String category;
    private String scenario;
    private String tags;
    private String content;
    private String entryCommand;
    private String parameterSchema;
    private Boolean enabled;
}
