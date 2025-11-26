package com.example.springboottest.modules.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件处理消息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileProcessMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private String category;
    private String description;
    private String processType;
    private Long uploadTime;
    private String uploadUser;
    private String fileUrl;
    private String metadata;
}
