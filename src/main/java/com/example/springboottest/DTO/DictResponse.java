package com.example.springboottest.DTO;

import com.example.springboottest.entity.Dict;
import java.time.LocalDateTime;
import java.util.List;

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

    public DictResponse() {}

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public List<DictItemResponse> getItems() {
        return items;
    }

    public void setItems(List<DictItemResponse> items) {
        this.items = items;
    }
}
