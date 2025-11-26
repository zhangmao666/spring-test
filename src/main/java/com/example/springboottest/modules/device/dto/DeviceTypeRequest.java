package com.example.springboottest.modules.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeviceTypeRequest {
    @NotBlank(message = "设备类型代码不能为空")
    @Size(max = 40, message = "设备类型代码长度不能超过40个字符")
    private String deviceTypeCode;

    @Size(max = 40, message = "名称长度不能超过40个字符")
    private String name;

    private Integer status;

    @Size(max = 100, message = "全名长度不能超过100个字符")
    private String fullName;

    @Size(max = 20, message = "类型代码长度不能超过20个字符")
    private String tbTypeCode;

    @Size(max = 1024, message = "图标URL长度不能超过1024个字符")
    private String icon;

    @Size(max = 255, message = "介绍长度不能超过255个字符")
    private String introduction;

    @Size(max = 1024, message = "PC图标URL长度不能超过1024个字符")
    private String iconPc;

    @Size(max = 10, message = "图标颜色长度不能超过10个字符")
    private String iconColour;

    @Size(max = 255, message = "设备图片URL长度不能超过255个字符")
    private String deviceImg;
}
