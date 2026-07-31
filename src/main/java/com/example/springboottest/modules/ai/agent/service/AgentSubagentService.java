package com.example.springboottest.modules.ai.agent.service;

import com.example.springboottest.modules.ai.agent.entity.AgentSubagent;
import com.example.springboottest.modules.ai.agent.mapper.AgentSubagentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentSubagentService {

    private final AgentSubagentMapper subagentMapper;

    public AgentSubagent createPlaceholder(String parentRunId, String conversationId, String agentType, String task) {
        AgentSubagent subagent = AgentSubagent.builder()
                .parentRunId(parentRunId)
                .childRunId(UUID.randomUUID().toString())
                .conversationId(conversationId)
                .agentType(agentType)
                .task(task)
                .status("PENDING")
                .resultSummary("子代理框架已记录，本版本默认不执行写入型后台任务。")
                .build();
        subagentMapper.insert(subagent);
        return subagent;
    }

    public List<AgentSubagent> listByParentRunId(String parentRunId) {
        return subagentMapper.findByParentRunId(parentRunId);
    }
}
