package com.example.springboottest.service.impl;

import com.example.springboottest.config.ActiveMqConfig;
import com.example.springboottest.DTO.FileProcessMessage;
import com.example.springboottest.service.MessageProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

/**
 * 消息生产者服务实现类
 */
@Slf4j
@Service
public class MessageProducerServiceImpl implements MessageProducerService {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void sendFileUploadMessage(FileProcessMessage message) {
        try {
            log.info("发送文件上传消息到队列: {}, 文件名: {}", 
                    ActiveMqConfig.FILE_UPLOAD_QUEUE, message.getFileName());
            jmsTemplate.convertAndSend(ActiveMqConfig.FILE_UPLOAD_QUEUE, message);
            log.info("文件上传消息发送成功: {}", message.getFileName());
        } catch (Exception e) {
            log.error("发送文件上传消息失败: {}", message.getFileName(), e);
            throw new RuntimeException("发送文件上传消息失败", e);
        }
    }

    @Override
    public void sendFileProcessMessage(FileProcessMessage message) {
        try {
            log.info("发送文件处理消息到队列: {}, 文件名: {}", 
                    ActiveMqConfig.FILE_PROCESS_QUEUE, message.getFileName());
            jmsTemplate.convertAndSend(ActiveMqConfig.FILE_PROCESS_QUEUE, message);
            log.info("文件处理消息发送成功: {}", message.getFileName());
        } catch (Exception e) {
            log.error("发送文件处理消息失败: {}", message.getFileName(), e);
            throw new RuntimeException("发送文件处理消息失败", e);
        }
    }

    @Override
    public void sendMessage(String queueName, Object message) {
        try {
            log.info("发送消息到队列: {}", queueName);
            jmsTemplate.convertAndSend(queueName, message);
            log.info("消息发送成功到队列: {}", queueName);
        } catch (Exception e) {
            log.error("发送消息失败到队列: {}", queueName, e);
            throw new RuntimeException("发送消息失败", e);
        }
    }
}


