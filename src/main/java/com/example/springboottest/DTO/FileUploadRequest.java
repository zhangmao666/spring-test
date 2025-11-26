package com.example.springboottest.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {
    
    /**
     * 文件名称
     */
    private String fileName;
    
    /**
     * 文件描述
     */
    private String description;
    
    /**
     * 文件分类（可选）
     */
    private String category;
}