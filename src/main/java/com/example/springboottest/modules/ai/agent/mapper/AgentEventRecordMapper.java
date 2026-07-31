package com.example.springboottest.modules.ai.agent.mapper;

import com.example.springboottest.modules.ai.agent.entity.AgentEventRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentEventRecordMapper {

    int insert(AgentEventRecord event);

    List<AgentEventRecord> findByRunId(@Param("runId") String runId);
}
