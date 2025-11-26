package com.example.springboottest.modules.task.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.task.entity.TaskApprovalFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 审批流定义Repository
 */
@Mapper
public interface TaskApprovalFlowRepository extends BaseMapper<TaskApprovalFlow> {

    @Select("SELECT * FROM task_approval_flows WHERE flow_code = #{flowCode} AND status = 1")
    TaskApprovalFlow selectByFlowCode(String flowCode);

    default Optional<TaskApprovalFlow> findByFlowCode(String flowCode) {
        return Optional.ofNullable(selectByFlowCode(flowCode));
    }

    @Select("SELECT * FROM task_approval_flows WHERE status = 1 ORDER BY created_at DESC")
    List<TaskApprovalFlow> selectAllActive();

    @Select("SELECT * FROM task_approval_flows WHERE task_type = #{taskType} AND status = 1 ORDER BY version DESC")
    List<TaskApprovalFlow> selectByTaskType(String taskType);

    @Select("SELECT * FROM task_approval_flows WHERE task_type = #{taskType} AND status = 1 ORDER BY version DESC LIMIT 1")
    TaskApprovalFlow selectLatestByTaskType(String taskType);
}
