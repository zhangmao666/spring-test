package com.example.springboottest.DTO;

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

    /**
     * 文件名（存储名称）
     */
    private String fileName;

    /**
     * 原始文件名
     */
    private String originalFileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String contentType;

    /**
     * 文件分类
     */
    private String category;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 处理类型：UPLOAD（上传）、PROCESS（处理）、DELETE（删除）
     */
    private String processType;

    /**
     * 上传时间（时间戳）
     */
    private Long uploadTime;

    /**
     * 上传用户（可选）
     */
    private String uploadUser;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 额外的元数据信息
     */
    private String metadata;
}


