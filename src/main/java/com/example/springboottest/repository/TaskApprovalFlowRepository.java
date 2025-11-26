package com.example.springboottest.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.TaskApprovalFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 审批流定义Repository
 */
@Mapper
public interface TaskApprovalFlowRepository extends BaseMapper<TaskApprovalFlow> {

    /**
     * 根据流程编码查询审批流
     */
    @Select("SELECT * FROM task_approval_flows WHERE flow_code = #{flowCode} AND status = 1")
    TaskApprovalFlow selectByFlowCode(String flowCode);

    /**
     * 根据流程编码查询审批流（Optional包装）
     */
    default Optional<TaskApprovalFlow> findByFlowCode(String flowCode) {
        return Optional.ofNullable(selectByFlowCode(flowCode));
    }

    /**
     * 查询所有启用的审批流
     */
    @Select("SELECT * FROM task_approval_flows WHERE status = 1 ORDER BY created_at DESC")
    List<TaskApprovalFlow> selectAllActive();

    /**
     * 根据任务类型查询审批流
     */
    @Select("SELECT * FROM task_approval_flows WHERE task_type = #{taskType} AND status = 1 " +
            "ORDER BY version DESC")
    List<TaskApprovalFlow> selectByTaskType(String taskType);

    /**
     * 根据任务类型查询最新版本的审批流
     */
    @Select("SELECT * FROM task_approval_flows WHERE task_type = #{taskType} AND status = 1 " +
            "ORDER BY version DESC LIMIT 1")
    TaskApprovalFlow selectLatestByTaskType(String taskType);
}
