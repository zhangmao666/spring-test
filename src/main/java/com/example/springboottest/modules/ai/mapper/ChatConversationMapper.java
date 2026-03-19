package com.example.springboottest.modules.ai.mapper;

import com.example.springboottest.modules.ai.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatConversationMapper {

    ChatConversation findByConversationId(@Param("conversationId") String conversationId);

    List<ChatConversation> findByUserIdAndDeleted(@Param("userId") String userId, @Param("deleted") Boolean deleted);

    List<ChatConversation> findByDeleted(@Param("deleted") Boolean deleted);

    int existsByConversationId(@Param("conversationId") String conversationId);

    int insert(ChatConversation conversation);

    int update(ChatConversation conversation);

    int updateModelBinding(@Param("conversationId") String conversationId,
                           @Param("modelId") Long modelId,
                           @Param("provider") String provider,
                           @Param("model") String model);

    int updateTitle(@Param("conversationId") String conversationId, @Param("title") String title);

    int softDelete(@Param("conversationId") String conversationId);
}
