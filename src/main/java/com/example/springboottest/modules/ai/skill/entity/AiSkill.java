package com.example.springboottest.modules.ai.skill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("ai_skill")
public class AiSkill {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("skill_key")
    private String skillKey;

    private String name;
    private String description;

    @TableField("source_type")
    private String sourceType;

    @TableField("origin_type")
    private String originType;

    @TableField("skill_type")
    private String skillType;

    private String category;
    private String scenario;
    private String tags;
    private String content;

    @TableField("skill_dir")
    private String skillDir;

    @TableField("entry_command")
    private String entryCommand;

    @TableField("parameter_schema")
    private String parameterSchema;

    private Boolean enabled;
    private Boolean readonly;
    private Boolean deleted;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
