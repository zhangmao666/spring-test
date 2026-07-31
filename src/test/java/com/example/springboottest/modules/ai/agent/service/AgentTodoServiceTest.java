package com.example.springboottest.modules.ai.agent.service;

import com.example.springboottest.modules.ai.agent.entity.AgentTodo;
import com.example.springboottest.modules.ai.agent.mapper.AgentTodoMapper;
import com.example.springboottest.modules.ai.agent.runtime.AgentRuntimeStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentTodoServiceTest {

    @Test
    void shouldAllowOnlyOneInProgressTodoPerRun() {
        FakeTodoMapper mapper = new FakeTodoMapper();
        AgentTodoService service = new AgentTodoService(mapper);
        service.replaceTodos("run-1", "conv-1", List.of(
                Map.of("todoId", "t1", "content", "分析需求"),
                Map.of("todoId", "t2", "content", "调用工具")
        ));

        service.updateStatus("run-1", "t1", AgentRuntimeStatus.TODO_IN_PROGRESS);
        service.updateStatus("run-1", "t2", AgentRuntimeStatus.TODO_IN_PROGRESS);

        assertEquals(AgentRuntimeStatus.TODO_PENDING, mapper.findByRunIdAndTodoId("run-1", "t1").getStatus());
        assertEquals(AgentRuntimeStatus.TODO_IN_PROGRESS, mapper.findByRunIdAndTodoId("run-1", "t2").getStatus());
    }

    private static class FakeTodoMapper implements AgentTodoMapper {
        private final Map<String, AgentTodo> todos = new HashMap<>();

        @Override
        public int insert(AgentTodo todo) {
            todos.put(todo.getRunId() + ":" + todo.getTodoId(), todo);
            return 1;
        }

        @Override
        public List<AgentTodo> findByRunId(String runId) {
            return todos.values().stream()
                    .filter(todo -> runId.equals(todo.getRunId()))
                    .sorted((left, right) -> Integer.compare(left.getSortOrder(), right.getSortOrder()))
                    .toList();
        }

        @Override
        public AgentTodo findByRunIdAndTodoId(String runId, String todoId) {
            return todos.get(runId + ":" + todoId);
        }

        @Override
        public AgentTodo findInProgress(String runId) {
            return findByRunId(runId).stream()
                    .filter(todo -> AgentRuntimeStatus.TODO_IN_PROGRESS.equals(todo.getStatus()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public int updateStatus(String runId, String todoId, String status) {
            AgentTodo todo = findByRunIdAndTodoId(runId, todoId);
            todo.setStatus(status);
            return 1;
        }
    }
}
