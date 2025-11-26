package com.example.springboottest.modules.device.dto;

import com.example.springboottest.modules.device.entity.DeviceType;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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

    public DeviceTypeResponse(DeviceType deviceType) {
        this.deviceTypeCode = deviceType.getDeviceTypeCode();
        this.name = deviceType.getName();
        this.status = deviceType.getStatus();
        this.fullName = deviceType.getFullName();
        this.tbTypeCode = deviceType.getTbTypeCode();
        this.icon = deviceType.getIcon();
        this.introduction = deviceType.getIntroduction();
        this.createTime = deviceType.getCreateTime();
        this.createBy = deviceType.getCreateBy();
        this.updateTime = deviceType.getUpdateTime();
        this.updateBy = deviceType.getUpdateBy();
        this.iconPc = deviceType.getIconPc();
        this.iconColour = deviceType.getIconColour();
        this.deviceImg = deviceType.getDeviceImg();
    }
}
