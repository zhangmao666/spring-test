package com.example.springboottest.modules.file.service;

import com.example.springboottest.modules.file.dto.FileListResponse;
import com.example.springboottest.modules.file.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件服务接口
 */
public interface FileService {
    FileUploadResponse uploadFile(MultipartFile file, String description, String category);
    InputStream downloadFile(String fileName);
    boolean deleteFile(String fileName);
    FileListResponse listFiles(String prefix);
    String getFileUrl(String fileName, Integer expiry);
    boolean fileExists(String fileName);
}
