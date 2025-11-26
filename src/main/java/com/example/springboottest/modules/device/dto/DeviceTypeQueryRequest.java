package com.example.springboottest.modules.device.dto;

import lombok.Data;

@Data
public class DeviceTypeQueryRequest {
    private String name;
    private Integer status;
    private int page = 0;
    private int size = 10;
}
