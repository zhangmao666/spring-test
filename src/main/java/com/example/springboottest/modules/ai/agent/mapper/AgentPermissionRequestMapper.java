package com.example.springboottest.modules.ai.agent.mapper;

import com.example.springboottest.modules.ai.agent.entity.AgentPermissionRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AgentPermissionRequestMapper {

    int insert(AgentPermissionRequest request);

    AgentPermissionRequest findByRequestId(@Param("requestId") String requestId);

    int decide(@Param("requestId") String requestId,
               @Param("status") String status,
               @Param("decision") String decision);
}
