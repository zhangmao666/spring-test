package com.example.springboottest.modules.ai.agent.mapper;

import com.example.springboottest.modules.ai.agent.entity.AgentContextSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AgentContextSnapshotMapper {

    int insert(AgentContextSnapshot snapshot);

    AgentContextSnapshot findLatestByRunId(@Param("runId") String runId);

    int countByRunId(@Param("runId") String runId);
}
