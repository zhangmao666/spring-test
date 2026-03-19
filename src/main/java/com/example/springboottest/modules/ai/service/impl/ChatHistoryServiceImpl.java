package com.example.springboottest.modules.ai.service.impl;

import com.example.springboottest.modules.ai.dto.ConversationVO;
import com.example.springboottest.modules.ai.dto.MessageVO;
import com.example.springboottest.modules.ai.entity.ChatConversation;
import com.example.springboottest.modules.ai.entity.ChatMessage;
import com.example.springboottest.modules.ai.mapper.ChatConversationMapper;
import com.example.springboottest.modules.ai.mapper.ChatMessageMapper;
import com.example.springboottest.modules.ai.service.ChatHistoryService;
import com.example.springboottest.util.SecurityUtils;
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
    public ConversationVO getConversation(String conversationId) {
        ChatConversation conversation = conversationMapper.findByConversationId(conversationId);
        return conversation == null ? null : toConversationVO(conversation);
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
        int messageIndex = conversation.getMessageCount() == null ? 0 : conversation.getMessageCount();

        ChatMessage message = ChatMessage.builder()
                .conversationId(conversationId)
                .role("user")
                .content(content)
                .usedWebSearch(useWebSearch)
                .usedDeepThinking(useDeepThinking)
                .messageIndex(messageIndex)
                .build();

        messageMapper.insert(message);

        conversation.setMessageCount(messageIndex + 1);
        conversation.setLastMessageTime(LocalDateTime.now());
        if (messageIndex == 0) {
            conversation.setTitle(content.length() > 50 ? content.substring(0, 50) + "..." : content);
        }
        conversationMapper.update(conversation);
    }

    @Override
    @Transactional
    public void saveAssistantMessage(String conversationId, String content, String thought, Integer thinkingTime) {
        ChatConversation conversation = getOrCreateConversation(conversationId);
        int messageIndex = conversation.getMessageCount() == null ? 0 : conversation.getMessageCount();

        ChatMessage message = ChatMessage.builder()
                .conversationId(conversationId)
                .role("assistant")
                .content(content)
                .thought(thought)
                .thinkingTime(thinkingTime)
                .messageIndex(messageIndex)
                .build();

        messageMapper.insert(message);

        conversation.setMessageCount(messageIndex + 1);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationMapper.update(conversation);
    }

    @Override
    @Transactional
    public void bindConversationModel(String conversationId, Long modelId, String provider, String model, String modelDisplayName) {
        ChatConversation conversation = getOrCreateConversation(conversationId);
        conversationMapper.updateModelBinding(conversation.getConversationId(), modelId, provider, model);
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
                .userId(resolveCurrentUserId())
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

    private ChatConversation getOrCreateConversation(String conversationId) {
        ChatConversation conversation = conversationMapper.findByConversationId(conversationId);
        if (conversation != null) {
            return conversation;
        }
        ChatConversation entity = ChatConversation.builder()
                .conversationId(conversationId)
                .title("新对话")
                .messageCount(0)
                .lastMessageTime(LocalDateTime.now())
                .deleted(false)
                .userId(resolveCurrentUserId())
                .build();
        conversationMapper.insert(entity);
        return entity;
    }

    private String resolveCurrentUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userId == null ? null : String.valueOf(userId);
    }

    private ConversationVO toConversationVO(ChatConversation entity) {
        return ConversationVO.builder()
                .id(entity.getId())
                .conversationId(entity.getConversationId())
                .title(entity.getTitle())
                .provider(entity.getProvider())
                .model(entity.getModel())
                .modelId(entity.getModelId())
                .modelDisplayName(entity.getModelDisplayName())
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
