package com.example.springboottest.modules.dict.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictItemRequest {
    @NotNull(message = "字典ID不能为空")
    private Long dictId;

    @NotBlank(message = "字典项标签不能为空")
    @Size(max = 100, message = "字典项标签长度不能超过100个字符")
    private String itemLabel;

    @NotBlank(message = "字典项值不能为空")
    @Size(max = 100, message = "字典项值长度不能超过100个字符")
    private String itemValue;

    private Integer itemSort;
    private Integer status;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
}
