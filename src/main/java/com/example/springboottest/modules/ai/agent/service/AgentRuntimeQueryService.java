package com.example.springboottest.modules.ai.agent.service;

import com.example.springboottest.modules.ai.agent.entity.AgentContextSnapshot;
import com.example.springboottest.modules.ai.agent.entity.AgentEventRecord;
import com.example.springboottest.modules.ai.agent.entity.AgentRun;
import com.example.springboottest.modules.ai.agent.entity.AgentSubagent;
import com.example.springboottest.modules.ai.agent.entity.AgentTodo;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentRuntimeQueryService {

    private final AgentRunService runService;
    private final AgentTodoService todoService;
    private final AgentEventStore eventStore;
    private final AgentContextCompactService compactService;
    private final AgentSubagentService subagentService;

    public AgentRunDetail detail(String runId) {
        return AgentRunDetail.builder()
                .run(runService.getRun(runId))
                .todos(todoService.listTodos(runId))
                .events(eventStore.listEvents(runId))
                .latestSnapshot(compactService.latest(runId))
                .subagents(subagentService.listByParentRunId(runId))
                .build();
    }

    @Value
    @Builder
    public static class AgentRunDetail {
        AgentRun run;
        List<AgentTodo> todos;
        List<AgentEventRecord> events;
        AgentContextSnapshot latestSnapshot;
        List<AgentSubagent> subagents;
    }
}
