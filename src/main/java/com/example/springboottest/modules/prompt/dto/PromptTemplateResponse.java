package com.example.springboottest.modules.prompt.dto;

import com.example.springboottest.modules.prompt.entity.PromptTemplate;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PromptTemplateResponse {

    private Long id;
    private String promptCode;
    private String promptName;
    private String promptType;
    private String promptContent;
    private String variables;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private Long createBy;
    private LocalDateTime updateTime;
    private Long updateBy;

    public PromptTemplateResponse(PromptTemplate template) {
        this.id = template.getId();
        this.promptCode = template.getPromptCode();
        this.promptName = template.getPromptName();
        this.promptType = template.getPromptType();
        this.promptContent = template.getPromptContent();
        this.variables = template.getVariables();
        this.status = template.getStatus();
        this.remark = template.getRemark();
        this.createTime = template.getCreateTime();
        this.createBy = template.getCreateBy();
        this.updateTime = template.getUpdateTime();
        this.updateBy = template.getUpdateBy();
    }
}
