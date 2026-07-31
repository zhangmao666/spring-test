package com.example.springboottest.modules.ai.skill.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AiSkillResponse {

    private Long id;
    private String skillKey;
    private String name;
    private String description;
    private String sourceType;
    private String originType;
    private String skillType;
    private String category;
    private String scenario;
    private String tags;
    private String content;
    private String skillDir;
    private String entryCommand;
    private String parameterSchema;
    private Boolean enabled;
    private Boolean readonly;
    private List<String> scripts;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
