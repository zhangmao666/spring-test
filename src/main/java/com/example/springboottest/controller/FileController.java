package com.example.springboottest.controller;

import com.example.springboottest.DTO.ApiResponse;
import com.example.springboottest.DTO.FileListResponse;
import com.example.springboottest.DTO.FileUploadResponse;
import com.example.springboottest.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public ApiResponse<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category) {
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                return ApiResponse.error("文件不能为空");
            }
            
            // 验证文件大小（限制100MB）
            if (file.getSize() > 100 * 1024 * 1024) {
                return ApiResponse.error("文件大小不能超过100MB");
            }
            
            FileUploadResponse response = fileService.uploadFile(file, description, category);
            return ApiResponse.success("文件上传成功", response);
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName) {
        
        try {
            // 检查文件是否存在
            if (!fileService.fileExists(fileName)) {
                return ResponseEntity.notFound().build();
            }
            
            InputStream inputStream = fileService.downloadFile(fileName);
            InputStreamResource resource = new InputStreamResource(inputStream);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("文件下载失败: {}", fileName, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("文件下载失败: " + e.getMessage()));
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileName}")
    public ApiResponse<Boolean> deleteFile(@PathVariable String fileName) {
        
        try {
            // 检查文件是否存在
            if (!fileService.fileExists(fileName)) {
                return ApiResponse.error("文件不存在");
            }
            
            boolean success = fileService.deleteFile(fileName);
            if (success) {
                return ApiResponse.success("文件删除成功", true);
            } else {
                return ApiResponse.error("文件删除失败");
            }
            
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileName, e);
            return ApiResponse.error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/list")
    public ApiResponse<FileListResponse> listFiles(
            @RequestParam(value = "prefix", required = false) String prefix) {
        
        try {
            FileListResponse response = fileService.listFiles(prefix);
            return ApiResponse.success("获取文件列表成功", response);
            
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            return ApiResponse.error("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件访问URL
     */
    @GetMapping("/url/{fileName}")
    public ApiResponse<String> getFileUrl(
            @PathVariable String fileName,
            @RequestParam(value = "expiry", defaultValue = "3600") Integer expiry) {
        
        try {
            // 检查文件是否存在
            if (!fileService.fileExists(fileName)) {
                return ApiResponse.error("文件不存在");
            }
            
            String url = fileService.getFileUrl(fileName, expiry);
            return ApiResponse.success("获取文件URL成功", url);
            
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", fileName, e);
            return ApiResponse.error("获取文件URL失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     */
    @GetMapping("/exists/{fileName}")
    public ApiResponse<Boolean> fileExists(@PathVariable String fileName) {
        
        try {
            boolean exists = fileService.fileExists(fileName);
            return ApiResponse.success(exists ? "文件存在" : "文件不存在", exists);
            
        } catch (Exception e) {
            log.error("检查文件是否存在失败: {}", fileName, e);
            return ApiResponse.error("检查文件是否存在失败: " + e.getMessage());
        }
    }
}