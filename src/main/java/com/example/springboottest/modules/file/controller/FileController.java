package com.example.springboottest.modules.file.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.file.dto.FileListResponse;
import com.example.springboottest.modules.file.dto.FileUploadResponse;
import com.example.springboottest.modules.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传、下载、删除等管理接口")
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文件", description = "上传文件到MinIO存储，单个文件大小限制100MB")
    @PostMapping("/upload")
    public ApiResponse<FileUploadResponse> uploadFile(
            @Parameter(description = "文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件描述") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "文件分类") @RequestParam(value = "category", required = false) String category) {
        try {
            if (file.isEmpty())
                return ApiResponse.error("文件不能为空");
            if (file.getSize() > 100 * 1024 * 1024)
                return ApiResponse.error("文件大小不能超过100MB");
            FileUploadResponse response = fileService.uploadFile(file, description, category);
            return ApiResponse.success("文件上传成功", response);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "下载文件", description = "根据文件名下载文件")
    @GetMapping("/download/{fileName}")
    public ResponseEntity<?> downloadFile(
            @Parameter(description = "文件名", required = true) @PathVariable String fileName) {
        try {
            if (!fileService.fileExists(fileName))
                return ResponseEntity.notFound().build();
            InputStream inputStream = fileService.downloadFile(fileName);
            InputStreamResource resource = new InputStreamResource(inputStream);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");
            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
        } catch (Exception e) {
            log.error("文件下载失败: {}", fileName, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("文件下载失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "删除文件", description = "根据文件名删除文件")
    @DeleteMapping("/{fileName}")
    public ApiResponse<Boolean> deleteFile(
            @Parameter(description = "文件名", required = true) @PathVariable String fileName) {
        try {
            if (!fileService.fileExists(fileName))
                return ApiResponse.error("文件不存在");
            boolean success = fileService.deleteFile(fileName);
            return success ? ApiResponse.success("文件删除成功", true) : ApiResponse.error("文件删除失败");
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileName, e);
            return ApiResponse.error("文件删除失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询文件列表", description = "查询文件列表，支持按前缀筛选")
    @GetMapping("/list")
    public ApiResponse<FileListResponse> listFiles(
            @Parameter(description = "文件名前缀") @RequestParam(value = "prefix", required = false) String prefix) {
        try {
            FileListResponse response = fileService.listFiles(prefix);
            return ApiResponse.success("获取文件列表成功", response);
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            return ApiResponse.error("获取文件列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取文件URL", description = "获取文件的临时访问URL")
    @GetMapping("/url/{fileName}")
    public ApiResponse<String> getFileUrl(
            @Parameter(description = "文件名", required = true) @PathVariable String fileName,
            @Parameter(description = "URL过期时间（秒）") @RequestParam(value = "expiry", defaultValue = "3600") Integer expiry) {
        try {
            if (!fileService.fileExists(fileName))
                return ApiResponse.error("文件不存在");
            String url = fileService.getFileUrl(fileName, expiry);
            return ApiResponse.success("获取文件URL成功", url);
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", fileName, e);
            return ApiResponse.error("获取文件URL失败: " + e.getMessage());
        }
    }

    @Operation(summary = "检查文件是否存在", description = "检查指定文件名的文件是否存在")
    @GetMapping("/exists/{fileName}")
    public ApiResponse<Boolean> fileExists(
            @Parameter(description = "文件名", required = true) @PathVariable String fileName) {
        try {
            boolean exists = fileService.fileExists(fileName);
            return ApiResponse.success(exists ? "文件存在" : "文件不存在", exists);
        } catch (Exception e) {
            log.error("检查文件是否存在失败: {}", fileName, e);
            return ApiResponse.error("检查文件是否存在失败: " + e.getMessage());
        }
    }
}
