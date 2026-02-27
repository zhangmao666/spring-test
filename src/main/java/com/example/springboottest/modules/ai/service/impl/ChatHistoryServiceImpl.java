package com.example.springboottest.modules.ai.service.impl;

import com.example.springboottest.modules.ai.dto.ConversationVO;
import com.example.springboottest.modules.ai.dto.MessageVO;
import com.example.springboottest.modules.ai.entity.ChatConversation;
import com.example.springboottest.modules.ai.entity.ChatMessage;
import com.example.springboottest.modules.ai.mapper.ChatConversationMapper;
import com.example.springboottest.modules.ai.mapper.ChatMessageMapper;
import com.example.springboottest.modules.ai.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private final ChatConversationMapper conversationMapper;
    private final ChatMessageMapper messageMapper;

    @Override
    public List<ConversationVO> getAllConversations() {
        return conversationMapper.findByDeleted(false)
                .stream()
                .map(this::toConversationVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationVO> getUserConversations(String userId) {
        return conversationMapper.findByUserIdAndDeleted(userId, false)
                .stream()
                .map(this::toConversationVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageVO> getConversationMessages(String conversationId) {
        return messageMapper.findByConversationIdOrderByIndex(conversationId)
                .stream()
                .map(this::toMessageVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveUserMessage(String conversationId, String content, boolean useWebSearch, boolean useDeepThinking) {
        ChatConversation conversation = getOrCreateConversation(conversationId);
        
        int messageIndex = conversation.getMessageCount();
        
        ChatMessage message = ChatMessage.builder()
                .conversationId(conversationId)
                .role("user")
                .content(content)
                .usedWebSearch(useWebSearch)
                .usedDeepThinking(useDeepThinking)
                .messageIndex(messageIndex)
                .build();
        
        messageMapper.insert(message);
        
        // 更新会话信息
        conversation.setMessageCount(messageIndex + 1);
        conversation.setLastMessageTime(LocalDateTime.now());
        
        // 如果是第一条消息，用它作为标题
        if (messageIndex == 0) {
            String title = content.length() > 50 ? content.substring(0, 50) + "..." : content;
            conversation.setTitle(title);
        }
        
        conversationMapper.update(conversation);
    }

    @Override
    @Transactional
    public void saveAssistantMessage(String conversationId, String content, String thought, Integer thinkingTime) {
        ChatConversation conversation = getOrCreateConversation(conversationId);
        
        int messageIndex = conversation.getMessageCount();
        
        ChatMessage message = ChatMessage.builder()
                .conversationId(conversationId)
                .role("assistant")
                .content(content)
                .thought(thought)
                .thinkingTime(thinkingTime)
                .messageIndex(messageIndex)
                .build();
        
        messageMapper.insert(message);
        
        // 更新会话信息
        conversation.setMessageCount(messageIndex + 1);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationMapper.update(conversation);
    }

    @Override
    @Transactional
    public ConversationVO createConversation(String conversationId, String title, String provider, String model) {
        ChatConversation conversation = ChatConversation.builder()
                .conversationId(conversationId)
                .title(title != null ? title : "新对话")
                .provider(provider)
                .model(model)
                .messageCount(0)
                .lastMessageTime(LocalDateTime.now())
                .deleted(false)
                .build();
        
        conversationMapper.insert(conversation);
        return toConversationVO(conversation);
    }

    @Override
    @Transactional
    public void updateConversationTitle(String conversationId, String title) {
        conversationMapper.updateTitle(conversationId, title);
    }

    @Override
    @Transactional
    public void deleteConversation(String conversationId) {
        conversationMapper.softDelete(conversationId);
    }

    @Override
    public boolean conversationExists(String conversationId) {
        return conversationMapper.existsByConversationId(conversationId) > 0;
    }

    // ==================== 私有方法 ====================

    private ChatConversation getOrCreateConversation(String conversationId) {
        ChatConversation conversation = conversationMapper.findByConversationId(conversationId);
        if (conversation == null) {
            conversation = ChatConversation.builder()
                    .conversationId(conversationId)
                    .title("新对话")
                    .messageCount(0)
                    .lastMessageTime(LocalDateTime.now())
                    .deleted(false)
                    .build();
            conversationMapper.insert(conversation);
        }
        return conversation;
    }

    private ConversationVO toConversationVO(ChatConversation entity) {
        return ConversationVO.builder()
                .id(entity.getId())
                .conversationId(entity.getConversationId())
                .title(entity.getTitle())
                .provider(entity.getProvider())
                .model(entity.getModel())
                .lastMessageTime(entity.getLastMessageTime())
                .messageCount(entity.getMessageCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private MessageVO toMessageVO(ChatMessage entity) {
        return MessageVO.builder()
                .id(entity.getId())
                .role(entity.getRole())
                .content(entity.getContent())
                .thought(entity.getThought())
                .thinkingTime(entity.getThinkingTime())
                .usedWebSearch(entity.getUsedWebSearch())
                .usedDeepThinking(entity.getUsedDeepThinking())
                .timestamp(entity.getCreatedAt())
                .messageIndex(entity.getMessageIndex())
                .build();
    }
}
