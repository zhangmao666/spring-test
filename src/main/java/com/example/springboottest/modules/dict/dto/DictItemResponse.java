package com.example.springboottest.modules.dict.dto;

import com.example.springboottest.modules.dict.entity.DictItem;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class DictItemResponse {
    private Long id;
    private Long dictId;
    private String itemLabel;
    private String itemValue;
    private Integer itemSort;
    private Integer status;
    private String description;
    private LocalDateTime createTime;
    private Long createBy;
    private LocalDateTime updateTime;
    private Long updateBy;

    public DictItemResponse(DictItem dictItem) {
        this.id = dictItem.getId();
        this.dictId = dictItem.getDictId();
        this.itemLabel = dictItem.getItemLabel();
        this.itemValue = dictItem.getItemValue();
        this.itemSort = dictItem.getItemSort();
        this.status = dictItem.getStatus();
        this.description = dictItem.getDescription();
        this.createTime = dictItem.getCreateTime();
        this.createBy = dictItem.getCreateBy();
        this.updateTime = dictItem.getUpdateTime();
        this.updateBy = dictItem.getUpdateBy();
    }
}
