package com.example.springboottest.DTO;

import com.example.springboottest.entity.DictItem;
import java.time.LocalDateTime;

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

    public DictItemResponse() {}

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDictId() {
        return dictId;
    }

    public void setDictId(Long dictId) {
        this.dictId = dictId;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public Integer getItemSort() {
        return itemSort;
    }

    public void setItemSort(Integer itemSort) {
        this.itemSort = itemSort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
