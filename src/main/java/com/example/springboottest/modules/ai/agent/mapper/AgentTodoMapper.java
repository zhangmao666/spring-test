package com.example.springboottest.modules.ai.agent.mapper;

import com.example.springboottest.modules.ai.agent.entity.AgentTodo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentTodoMapper {

    int insert(AgentTodo todo);

    List<AgentTodo> findByRunId(@Param("runId") String runId);

    AgentTodo findByRunIdAndTodoId(@Param("runId") String runId, @Param("todoId") String todoId);

    AgentTodo findInProgress(@Param("runId") String runId);

    int updateStatus(@Param("runId") String runId,
                     @Param("todoId") String todoId,
                     @Param("status") String status);
}
