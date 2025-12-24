package com.example.springboottest.modules.device.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: zm
 * @Created: 2025/12/23 09:18
 * @Description:
 */
@Data
@Accessors(chain = true)
public class DeviceConfigDTO {

    private String imgId;

    private String videoId;

}