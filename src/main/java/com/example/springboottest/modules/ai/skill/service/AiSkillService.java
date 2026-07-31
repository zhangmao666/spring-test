package com.example.springboottest.modules.ai.skill.service;

import com.example.springboottest.modules.ai.skill.dto.AiSkillQueryRequest;
import com.example.springboottest.modules.ai.skill.dto.AiSkillRequest;
import com.example.springboottest.modules.ai.skill.dto.AiSkillResponse;
import com.example.springboottest.modules.ai.skill.dto.SkillTestResponse;
import com.example.springboottest.modules.ai.skill.entity.AiSkill;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AiSkillService {

    List<AiSkillResponse> listSkills(AiSkillQueryRequest query);

    AiSkillResponse getSkill(Long id);

    Long createSkill(AiSkillRequest request);

    void updateSkill(Long id, AiSkillRequest request);

    void updateStatus(Long id, boolean enabled);

    void deleteSkill(Long id);

    int syncLocalSkills();

    AiSkillResponse importSkillZip(MultipartFile file);

    List<AiSkillResponse> getConversationSkills(String conversationId);

    void saveConversationSkills(String conversationId, List<Long> skillIds);

    List<AiSkill> getMountedEnabledSkills(String conversationId);

    SkillTestResponse testSkill(Long id, String conversationId, Map<String, Object> arguments);

    String buildMountedSkillPrompt(String conversationId);
}
