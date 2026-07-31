package com.example.springboottest.modules.ai.skill.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConversationSkillUpdateRequest {

    private List<Long> skillIds;
}
