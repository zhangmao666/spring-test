package com.example.springboottest.modules.message.consumer;

import com.example.springboottest.config.ActiveMqConfig;
import com.example.springboottest.modules.file.dto.FileProcessMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

/**
 * 文件消息消费者
 * 监听文件相关的消息队列，执行异步处理任务
 */
@Slf4j
@Service
public class FileMessageConsumer {

    @JmsListener(destination = ActiveMqConfig.FILE_UPLOAD_QUEUE)
    public void handleFileUploadMessage(FileProcessMessage message) {
        try {
            log.info("收到文件上传消息 - 文件名: {}, 大小: {} bytes, 类型: {}", 
                    message.getFileName(), message.getFileSize(), message.getContentType());
            processFileUpload(message);
            log.info("文件上传消息处理完成: {}", message.getFileName());
        } catch (Exception e) {
            log.error("处理文件上传消息失败: {}", message.getFileName(), e);
        }
    }

    @JmsListener(destination = ActiveMqConfig.FILE_PROCESS_QUEUE)
    public void handleFileProcessMessage(FileProcessMessage message) {
        try {
            log.info("收到文件处理消息 - 文件名: {}, 处理类型: {}", 
                    message.getFileName(), message.getProcessType());

            switch (message.getProcessType()) {
                case "THUMBNAIL":
                    generateThumbnail(message);
                    break;
                case "SCAN":
                    scanFileForVirus(message);
                    break;
                case "CONVERT":
                    convertFileFormat(message);
                    break;
                default:
                    log.warn("未知的处理类型: {}", message.getProcessType());
            }

            log.info("文件处理消息处理完成: {}", message.getFileName());
        } catch (Exception e) {
            log.error("处理文件处理消息失败: {}", message.getFileName(), e);
        }
    }

    private void processFileUpload(FileProcessMessage message) {
        log.info("处理文件上传 - 原始文件名: {}, 存储文件名: {}, 分类: {}", 
                message.getOriginalFileName(), message.getFileName(), message.getCategory());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("文件上传处理完成: {}", message.getFileName());
    }

    private void generateThumbnail(FileProcessMessage message) {
        log.info("开始生成缩略图: {}", message.getFileName());
        log.info("缩略图生成完成: {}", message.getFileName());
    }

    private void scanFileForVirus(FileProcessMessage message) {
        log.info("开始病毒扫描: {}", message.getFileName());
        log.info("病毒扫描完成: {}", message.getFileName());
    }

    private void convertFileFormat(FileProcessMessage message) {
        log.info("开始文件格式转换: {}", message.getFileName());
        log.info("文件格式转换完成: {}", message.getFileName());
    }
}
