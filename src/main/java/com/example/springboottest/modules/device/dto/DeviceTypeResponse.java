package com.example.springboottest.modules.device.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 设备类型响应DTO
 * 使用 DeviceTypeMapper 进行对象转换
 */
@Data
@NoArgsConstructor
public class DeviceTypeResponse {
    private String deviceTypeCode;
    private String name;
    private Integer status;
    private String fullName;
    private String tbTypeCode;
    private String icon;
    private String introduction;
    private LocalDateTime createTime;
    private Long createBy;
    private LocalDateTime updateTime;
    private Long updateBy;
    private String iconPc;
    private String iconColour;
    private String deviceImg;
    private DeviceConfigDTO config;
}
