package com.example.springboottest.modules.ai.agent.mapper;

import com.example.springboottest.modules.ai.agent.entity.AgentRun;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AgentRunMapper {

    int insert(AgentRun run);

    AgentRun findByRunId(@Param("runId") String runId);

    int updateStatus(@Param("runId") String runId,
                     @Param("status") String status,
                     @Param("summary") String summary,
                     @Param("errorMessage") String errorMessage);

    int updateTurn(@Param("runId") String runId, @Param("currentTurn") Integer currentTurn);
}
