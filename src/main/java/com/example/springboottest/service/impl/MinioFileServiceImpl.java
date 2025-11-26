package com.example.springboottest.service.impl;

import com.example.springboottest.config.MinioProperties;
import com.example.springboottest.DTO.FileListResponse;
import com.example.springboottest.DTO.FileProcessMessage;
import com.example.springboottest.DTO.FileUploadResponse;
import com.example.springboottest.service.FileService;
import com.example.springboottest.service.MessageProducerService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO文件服务实现类
 */
@Slf4j
@Service
public class MinioFileServiceImpl implements FileService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties minioProperties;

    @Autowired
    private MessageProducerService messageProducerService;

    /**
     * 初始化存储桶
     */
    private void initBucket() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .build()
            );
            
            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .build()
                );
                log.info("创建存储桶成功: {}", minioProperties.getBucketName());
            }
        } catch (Exception e) {
            log.error("初始化存储桶失败", e);
            throw new RuntimeException("初始化存储桶失败", e);
        }
    }

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String description, String category) {
        try {
            // 初始化存储桶
            initBucket();
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID().toString() + extension;
            
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(storedName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            
            log.info("文件上传成功: {}", storedName);
            
            // 生成访问URL
            String fileUrl = getFileUrl(storedName, 3600); // 1小时有效期
            
            FileUploadResponse response = new FileUploadResponse(
                    storedName,
                    originalFilename,
                    storedName,
                    file.getSize(),
                    file.getContentType(),
                    fileUrl,
                    System.currentTimeMillis(),
                    description,
                    category
            );

            // 发送文件上传消息到 ActiveMQ 进行异步处理
            try {
                FileProcessMessage message = FileProcessMessage.builder()
                        .fileName(storedName)
                        .originalFileName(originalFilename)
                        .fileSize(file.getSize())
                        .contentType(file.getContentType())
                        .category(category)
                        .description(description)
                        .processType("UPLOAD")
                        .uploadTime(System.currentTimeMillis())
                        .fileUrl(fileUrl)
                        .build();
                
                messageProducerService.sendFileUploadMessage(message);
                log.info("文件上传消息已发送到 ActiveMQ: {}", storedName);
            } catch (Exception e) {
                // 消息发送失败不影响文件上传结果
                log.error("发送文件上传消息到 ActiveMQ 失败: {}", storedName, e);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            log.error("文件下载失败: {}", fileName, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileName)
                            .build()
            );
            log.info("文件删除成功: {}", fileName);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileName, e);
            return false;
        }
    }

    @Override
    public FileListResponse listFiles(String prefix) {
        try {
            List<FileListResponse.FileInfo> files = new ArrayList<>();
            
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .prefix(prefix)
                            .build()
            );
            
            for (Result<Item> result : results) {
                Item item = result.get();
                String fileUrl = getFileUrl(item.objectName(), 3600);
                
                FileListResponse.FileInfo fileInfo = new FileListResponse.FileInfo(
                        item.objectName(),
                        item.size(),
                        item.lastModified().toString(),
                        fileUrl,
                        "application/octet-stream" // 默认类型，可以从对象元数据获取
                );
                files.add(fileInfo);
            }
            
            return new FileListResponse(files, files.size());
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            throw new RuntimeException("获取文件列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFileUrl(String fileName, Integer expiry) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperties.getBucketName())
                            .object(fileName)
                            .expiry(expiry, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成文件访问URL失败: {}", fileName, e);
            throw new RuntimeException("生成文件访问URL失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String fileName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}