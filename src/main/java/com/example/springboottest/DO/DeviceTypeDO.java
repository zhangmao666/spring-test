package com.example.springboottest.DO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author: zm
 * @Created: 2025/9/29 下午4:47
 * @Description:
 */
@Data
@Accessors(chain = true)
@TableName("device_type")
public class DeviceTypeDO {

    private String deviceTypeCode;
    private String name;
    private Integer status;
    private String fullName;
    private String tbTypeCode;
    private String icon;
    private String introduction;
    private String createTime;
    private Long createBy;
    private String updateTime;
    private Long updateBy;
    private String iconPc;
    private String iconColour;
    private String deviceImg;

}