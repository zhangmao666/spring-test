package com.example.springboottest.modules.file.dto;

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
    private List<FileInfo> files;
    private Integer total;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        private String fileName;
        private Long fileSize;
        private String lastModified;
        private String fileUrl;
        private String contentType;
    }
}
