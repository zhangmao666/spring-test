package com.example.springboottest.modules.prompt.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("prompt_template")
public class PromptTemplate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("prompt_code")
    private String promptCode;

    @TableField("prompt_name")
    private String promptName;

    @TableField("prompt_type")
    private String promptType;

    @TableField("prompt_content")
    private String promptContent;

    @TableField("variables")
    private String variables;

    @TableField("status")
    private Integer status;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("create_by")
    private Long createBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("update_by")
    private Long updateBy;
}
