package com.example.springboottest.DTO;

public class DeviceTypeQueryRequest {
    
    private String name;
    private Integer status;
    private String tbTypeCode;
    private int page = 0;
    private int size = 10;
    private String sortBy = "createTime";
    private String sortDirection = "desc";
    
    // 构造函数
    public DeviceTypeQueryRequest() {}
    
    // Getter 和 Setter 方法
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
    
    public String getTbTypeCode() {
        return tbTypeCode;
    }
    
    public void setTbTypeCode(String tbTypeCode) {
        this.tbTypeCode = tbTypeCode;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = Math.max(0, page);
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = Math.min(Math.max(1, size), 100); // 限制最大100条
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
