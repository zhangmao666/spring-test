package com.example.springboottest.modules.ai.service;

import com.example.springboottest.modules.ai.dto.ConversationVO;
import com.example.springboottest.modules.ai.dto.SearchStatus;
import com.example.springboottest.modules.ai.dto.MessageVO;
import com.example.springboottest.modules.ai.dto.WebSearchSource;

import java.util.List;

public interface ChatHistoryService {

    List<ConversationVO> getAllConversations();

    List<ConversationVO> getUserConversations(String userId);

    ConversationVO getConversation(String conversationId);

    List<MessageVO> getConversationMessages(String conversationId);

    void saveUserMessage(String conversationId, String content, boolean useWebSearch, boolean useDeepThinking);

    void saveAssistantMessage(String conversationId,
                              String content,
                              String thought,
                              Integer thinkingTime,
                              boolean usedWebSearch,
                              boolean usedDeepThinking,
                              String searchQuery,
                              SearchStatus searchStatus,
                              List<WebSearchSource> sources);

    void bindConversationModel(String conversationId, Long modelId, String provider, String model, String modelDisplayName);

    ConversationVO createConversation(String conversationId, String title, String provider, String model);

    void updateConversationTitle(String conversationId, String title);

    void deleteConversation(String conversationId);

    boolean conversationExists(String conversationId);
}
