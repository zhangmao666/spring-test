package com.example.springboottest.modules.ai.controller;

import com.example.springboottest.common.dto.ApiResponse;
import com.example.springboottest.modules.ai.dto.ConversationVO;
import com.example.springboottest.modules.ai.dto.MessageVO;
import com.example.springboottest.modules.ai.service.ChatHistoryService;
import com.example.springboottest.modules.ai.skill.dto.AiSkillResponse;
import com.example.springboottest.modules.ai.skill.dto.ConversationSkillUpdateRequest;
import com.example.springboottest.modules.ai.skill.service.AiSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai/history")
@RequiredArgsConstructor
@Tag(name = "AI对话历史", description = "对话历史记录管理接口")
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;
    private final AiSkillService aiSkillService;

    @Operation(summary = "获取所有会话列表", description = "获取所有未删除的会话，按最后消息时间倒序")
    @GetMapping("/conversations")
    public ApiResponse<List<ConversationVO>> getAllConversations() {
        List<ConversationVO> conversations = chatHistoryService.getAllConversations();
        return ApiResponse.success(conversations);
    }

    @Operation(summary = "获取会话的消息列表", description = "根据会话ID获取该会话的所有消息")
    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResponse<List<MessageVO>> getConversationMessages(
            @Parameter(description = "会话ID", required = true) 
            @PathVariable String conversationId) {
        List<MessageVO> messages = chatHistoryService.getConversationMessages(conversationId);
        return ApiResponse.success(messages);
    }

    @Operation(summary = "创建新会话", description = "创建一个新的对话会话")
    @PostMapping("/conversations")
    public ApiResponse<ConversationVO> createConversation(@RequestBody Map<String, String> request) {
        String conversationId = request.get("conversationId");
        String title = request.getOrDefault("title", "新对话");
        String provider = request.get("provider");
        String model = request.get("model");
        
        ConversationVO conversation = chatHistoryService.createConversation(
            conversationId, title, provider, model
        );
        return ApiResponse.success(conversation);
    }

    @Operation(summary = "更新会话标题", description = "修改会话的标题")
    @PutMapping("/conversations/{conversationId}/title")
    public ApiResponse<Void> updateConversationTitle(
            @Parameter(description = "会话ID", required = true) 
            @PathVariable String conversationId,
            @RequestBody Map<String, String> request) {
        String title = request.get("title");
        chatHistoryService.updateConversationTitle(conversationId, title);
        return ApiResponse.success(null);
    }

    @Operation(summary = "删除会话", description = "软删除一个会话（会话仍保留在数据库）")
    @DeleteMapping("/conversations/{conversationId}")
    public ApiResponse<Void> deleteConversation(
            @Parameter(description = "会话ID", required = true) 
            @PathVariable String conversationId) {
        chatHistoryService.deleteConversation(conversationId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取会话挂载技能", description = "获取当前会话已挂载的技能列表")
    @GetMapping("/conversations/{conversationId}/skills")
    public ApiResponse<List<AiSkillResponse>> getConversationSkills(@PathVariable String conversationId) {
        return ApiResponse.success(aiSkillService.getConversationSkills(conversationId));
    }

    @Operation(summary = "保存会话挂载技能", description = "保存当前会话已挂载的技能列表，最多50个")
    @PutMapping("/conversations/{conversationId}/skills")
    public ApiResponse<Void> saveConversationSkills(@PathVariable String conversationId,
                                                    @RequestBody ConversationSkillUpdateRequest request) {
        aiSkillService.saveConversationSkills(conversationId, request == null ? List.of() : request.getSkillIds());
        return ApiResponse.success();
    }
}
