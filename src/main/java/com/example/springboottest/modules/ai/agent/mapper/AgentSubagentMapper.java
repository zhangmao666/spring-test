package com.example.springboottest.modules.ai.agent.mapper;

import com.example.springboottest.modules.ai.agent.entity.AgentSubagent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentSubagentMapper {

    int insert(AgentSubagent subagent);

    List<AgentSubagent> findByParentRunId(@Param("parentRunId") String parentRunId);

    int updateStatus(@Param("childRunId") String childRunId,
                     @Param("status") String status,
                     @Param("resultSummary") String resultSummary);
}
