package com.example.springboottest.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件列表响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileListResponse {
    
    /**
     * 文件列表
     */
    private List<FileInfo> files;
    
    /**
     * 总数量
     */
    private Integer total;
    
    /**
     * 文件信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        /**
         * 文件名
         */
        private String fileName;
        
        /**
         * 文件大小
         */
        private Long fileSize;
        
        /**
         * 最后修改时间
         */
        private String lastModified;
        
        /**
         * 文件URL
         */
        private String fileUrl;
        
        /**
         * 文件类型
         */
        private String contentType;
    }
}