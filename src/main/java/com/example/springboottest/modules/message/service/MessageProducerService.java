package com.example.springboottest.modules.message.service;

import com.example.springboottest.modules.file.dto.FileProcessMessage;

/**
 * 消息生产者服务接口
 */
public interface MessageProducerService {

    /**
     * 发送文件上传消息
     */
    void sendFileUploadMessage(FileProcessMessage message);

    /**
     * 发送文件处理消息
     */
    void sendFileProcessMessage(FileProcessMessage message);

    /**
     * 发送通用消息到指定队列
     */
    void sendMessage(String queueName, Object message);
}
