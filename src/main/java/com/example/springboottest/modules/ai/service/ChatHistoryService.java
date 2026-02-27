package com.example.springboottest.modules.ai.service;

import com.example.springboottest.modules.ai.dto.ConversationVO;
import com.example.springboottest.modules.ai.dto.MessageVO;

import java.util.List;

/**
 * 对话历史管理服务
 */
public interface ChatHistoryService {

    /**
     * 获取所有会话列表
     */
    List<ConversationVO> getAllConversations();

    /**
     * 获取某用户的所有会话列表
     */
    List<ConversationVO> getUserConversations(String userId);

    /**
     * 获取某个会话的所有消息
     */
    List<MessageVO> getConversationMessages(String conversationId);

    /**
     * 保存用户消息
     */
    void saveUserMessage(String conversationId, String content, boolean useWebSearch, boolean useDeepThinking);

    /**
     * 保存AI回复消息
     */
    void saveAssistantMessage(String conversationId, String content, String thought, Integer thinkingTime);

    /**
     * 创建新会话
     */
    ConversationVO createConversation(String conversationId, String title, String provider, String model);

    /**
     * 更新会话标题
     */
    void updateConversationTitle(String conversationId, String title);

    /**
     * 删除会话（软删除）
     */
    void deleteConversation(String conversationId);

    /**
     * 检查会话是否存在
     */
    boolean conversationExists(String conversationId);
}
