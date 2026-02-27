package com.example.springboottest.modules.ai.mapper;

import com.example.springboottest.modules.ai.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatConversationMapper {

    /**
     * 根据会话ID查询
     */
    ChatConversation findByConversationId(@Param("conversationId") String conversationId);

    /**
     * 查询某用户的所有未删除会话，按最后消息时间倒序
     */
    List<ChatConversation> findByUserIdAndDeleted(@Param("userId") String userId, @Param("deleted") Boolean deleted);

    /**
     * 查询所有未删除的会话（如果无用户系统）
     */
    List<ChatConversation> findByDeleted(@Param("deleted") Boolean deleted);

    /**
     * 检查会话是否存在
     */
    int existsByConversationId(@Param("conversationId") String conversationId);

    /**
     * 插入会话
     */
    int insert(ChatConversation conversation);

    /**
     * 更新会话
     */
    int update(ChatConversation conversation);

    /**
     * 更新会话标题
     */
    int updateTitle(@Param("conversationId") String conversationId, @Param("title") String title);

    /**
     * 软删除会话
     */
    int softDelete(@Param("conversationId") String conversationId);
}
