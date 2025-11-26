package com.example.springboottest.DTO;

import com.example.springboottest.entity.DeviceType;
import java.time.LocalDateTime;

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
    
    // 构造函数
    public DeviceTypeResponse() {}
    
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
    
    // Getter 和 Setter 方法
    public String getDeviceTypeCode() {
        return deviceTypeCode;
    }
    
    public void setDeviceTypeCode(String deviceTypeCode) {
        this.deviceTypeCode = deviceTypeCode;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getTbTypeCode() {
        return tbTypeCode;
    }
    
    public void setTbTypeCode(String tbTypeCode) {
        this.tbTypeCode = tbTypeCode;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getIntroduction() {
        return introduction;
    }
    
    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public Long getCreateBy() {
        return createBy;
    }
    
    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public Long getUpdateBy() {
        return updateBy;
    }
    
    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }
    
    public String getIconPc() {
        return iconPc;
    }
    
    public void setIconPc(String iconPc) {
        this.iconPc = iconPc;
    }
    
    public String getIconColour() {
        return iconColour;
    }
    
    public void setIconColour(String iconColour) {
        this.iconColour = iconColour;
    }
    
    public String getDeviceImg() {
        return deviceImg;
    }
    
    public void setDeviceImg(String deviceImg) {
        this.deviceImg = deviceImg;
    }
}
