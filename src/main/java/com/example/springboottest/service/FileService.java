package com.example.springboottest.service;

import com.example.springboottest.DTO.FileListResponse;
import com.example.springboottest.DTO.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传文件
     * 
     * @param file 文件
     * @param description 文件描述
     * @param category 文件分类
     * @return 上传结果
     */
    FileUploadResponse uploadFile(MultipartFile file, String description, String category);
    
    /**
     * 下载文件
     * 
     * @param fileName 文件名
     * @return 文件流
     */
    InputStream downloadFile(String fileName);
    
    /**
     * 删除文件
     * 
     * @param fileName 文件名
     * @return 是否删除成功
     */
    boolean deleteFile(String fileName);
    
    /**
     * 获取文件列表
     * 
     * @param prefix 文件名前缀（可选）
     * @return 文件列表
     */
    FileListResponse listFiles(String prefix);
    
    /**
     * 获取文件访问URL
     * 
     * @param fileName 文件名
     * @param expiry 过期时间（秒）
     * @return 访问URL
     */
    String getFileUrl(String fileName, Integer expiry);
    
    /**
     * 检查文件是否存在
     * 
     * @param fileName 文件名
     * @return 是否存在
     */
    boolean fileExists(String fileName);
}