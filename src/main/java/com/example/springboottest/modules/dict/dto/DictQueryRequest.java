package com.example.springboottest.modules.dict.dto;

import lombok.Data;

@Data
public class DictQueryRequest {
    private String dictCode;
    private String dictName;
    private Integer status;
    private int page = 0;
    private int size = 10;
}
