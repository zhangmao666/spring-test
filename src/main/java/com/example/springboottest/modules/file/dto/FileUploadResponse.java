package com.example.springboottest.modules.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String fileId;
    private String originalName;
    private String storedName;
    private Long fileSize;
    private String contentType;
    private String fileUrl;
    private Long uploadTime;
    private String description;
    private String category;
}
