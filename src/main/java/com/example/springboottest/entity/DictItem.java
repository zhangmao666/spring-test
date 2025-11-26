package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dict_item")
public class DictItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("dict_id")
    private Long dictId;

    @TableField("item_label")
    private String itemLabel;

    @TableField("item_value")
    private String itemValue;

    @TableField("item_sort")
    private Integer itemSort;

    @TableField("status")
    private Integer status;

    @TableField("description")
    private String description;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("create_by")
    private Long createBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("update_by")
    private Long updateBy;

    public DictItem() {}
}
