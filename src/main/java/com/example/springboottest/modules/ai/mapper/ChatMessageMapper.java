package com.example.springboottest.modules.ai.mapper;

import com.example.springboottest.modules.ai.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    /**
     * 根据会话ID查询所有消息，按序号升序
     */
    List<ChatMessage> findByConversationIdOrderByIndex(@Param("conversationId") String conversationId);

    /**
     * 插入消息
     */
    int insert(ChatMessage message);

    /**
     * 删除某会话的所有消息
     */
    int deleteByConversationId(@Param("conversationId") String conversationId);

    /**
     * 统计某会话的消息数
     */
    int countByConversationId(@Param("conversationId") String conversationId);
}
