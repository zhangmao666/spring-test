package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("device_type")
public class DeviceType {
    
    @TableId(value = "device_type_code", type = IdType.INPUT)
    private String deviceTypeCode;
    
    @TableField("name")
    private String name;
    
    @TableField("status")
    private Integer status;
    
    @TableField("full_name")
    private String fullName;
    
    @TableField("tb_type_code")
    private String tbTypeCode;
    
    @TableField("icon")
    private String icon;
    
    @TableField("introduction")
    private String introduction;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField("create_by")
    private Long createBy;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField("update_by")
    private Long updateBy;
    
    @TableField("icon_pc")
    private String iconPc;
    
    @TableField("icon_colour")
    private String iconColour;
    
    @TableField("device_img")
    private String deviceImg;
    
    // 构造函数
    public DeviceType() {}
}
