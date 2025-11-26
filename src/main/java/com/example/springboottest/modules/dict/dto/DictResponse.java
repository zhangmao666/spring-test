package com.example.springboottest.modules.dict.dto;

import com.example.springboottest.modules.dict.entity.Dict;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class DictResponse {
    private Long id;
    private String dictCode;
    private String dictName;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private Long createBy;
    private LocalDateTime updateTime;
    private Long updateBy;
    private List<DictItemResponse> items;

    public DictResponse(Dict dict) {
        this.id = dict.getId();
        this.dictCode = dict.getDictCode();
        this.dictName = dict.getDictName();
        this.description = dict.getDescription();
        this.status = dict.getStatus();
        this.createTime = dict.getCreateTime();
        this.createBy = dict.getCreateBy();
        this.updateTime = dict.getUpdateTime();
        this.updateBy = dict.getUpdateBy();
    }
}
