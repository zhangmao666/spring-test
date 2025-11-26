package com.example.springboottest.service;

import com.example.springboottest.DTO.FileProcessMessage;

/**
 * 消息生产者服务接口
 */
public interface MessageProducerService {

    /**
     * 发送文件上传消息
     *
     * @param message 文件处理消息
     */
    void sendFileUploadMessage(FileProcessMessage message);

    /**
     * 发送文件处理消息
     *
     * @param message 文件处理消息
     */
    void sendFileProcessMessage(FileProcessMessage message);

    /**
     * 发送通用消息到指定队列
     *
     * @param queueName 队列名称
     * @param message   消息内容
     */
    void sendMessage(String queueName, Object message);
}


