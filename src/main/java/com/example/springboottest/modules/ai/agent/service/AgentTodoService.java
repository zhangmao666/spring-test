package com.example.springboottest.modules.ai.agent.service;

import com.example.springboottest.modules.ai.agent.entity.AgentTodo;
import com.example.springboottest.modules.ai.agent.mapper.AgentTodoMapper;
import com.example.springboottest.modules.ai.agent.runtime.AgentRuntimeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentTodoService {

    private final AgentTodoMapper todoMapper;

    public List<AgentTodo> listTodos(String runId) {
        return todoMapper.findByRunId(runId);
    }

    @Transactional
    public List<AgentTodo> replaceTodos(String runId, String conversationId, List<Map<String, Object>> todos) {
        if (todos == null || todos.isEmpty()) {
            return listTodos(runId);
        }
        List<AgentTodo> created = new ArrayList<>();
        int index = todoMapper.findByRunId(runId).size();
        for (Map<String, Object> item : todos) {
            String content = stringValue(item.get("content"));
            if (!StringUtils.hasText(content)) {
                continue;
            }
            String todoId = defaultIfBlank(stringValue(item.get("todoId")), UUID.randomUUID().toString());
            AgentTodo existing = todoMapper.findByRunIdAndTodoId(runId, todoId);
            if (existing != null) {
                created.add(existing);
                continue;
            }
            AgentTodo todo = AgentTodo.builder()
                    .runId(runId)
                    .conversationId(conversationId)
                    .todoId(todoId)
                    .content(content.trim())
                    .status(normalizeTodoStatus(stringValue(item.get("status")), AgentRuntimeStatus.TODO_PENDING))
                    .sortOrder(index++)
                    .build();
            todoMapper.insert(todo);
            created.add(todo);
        }
        return created;
    }

    @Transactional
    public AgentTodo updateStatus(String runId, String todoId, String status) {
        AgentTodo todo = todoMapper.findByRunIdAndTodoId(runId, todoId);
        if (todo == null) {
            throw new IllegalArgumentException("Todo 不存在: " + todoId);
        }
        String normalized = normalizeTodoStatus(status, AgentRuntimeStatus.TODO_PENDING);
        if (AgentRuntimeStatus.TODO_IN_PROGRESS.equals(normalized)) {
            AgentTodo previous = todoMapper.findInProgress(runId);
            if (previous != null && !previous.getTodoId().equals(todoId)) {
                todoMapper.updateStatus(runId, previous.getTodoId(), AgentRuntimeStatus.TODO_PENDING);
            }
        }
        todoMapper.updateStatus(runId, todoId, normalized);
        return todoMapper.findByRunIdAndTodoId(runId, todoId);
    }

    public boolean hasTodos(String runId) {
        return !todoMapper.findByRunId(runId).isEmpty();
    }

    private String normalizeTodoStatus(String status, String fallback) {
        String value = defaultIfBlank(status, fallback).toUpperCase();
        return switch (value) {
            case "IN_PROGRESS", "COMPLETED", "CANCELLED", "PENDING" -> value;
            default -> fallback;
        };
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
