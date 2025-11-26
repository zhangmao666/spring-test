package com.example.springboottest.modules.task.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.task.entity.TaskApprovalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批记录Repository
 */
@Mapper
public interface TaskApprovalRecordRepository extends BaseMapper<TaskApprovalRecord> {

    @Select("SELECT * FROM task_approval_records WHERE task_id = #{taskId} ORDER BY node_order ASC, created_at DESC")
    List<TaskApprovalRecord> selectByTaskId(Long taskId);

    @Select("SELECT * FROM task_approval_records WHERE task_id = #{taskId} AND node_id = #{nodeId} ORDER BY created_at DESC")
    List<TaskApprovalRecord> selectByTaskIdAndNodeId(@Param("taskId") Long taskId, @Param("nodeId") Long nodeId);

    @Select("SELECT * FROM task_approval_records WHERE task_id = #{taskId} AND node_id = #{nodeId} AND result = 'PENDING' ORDER BY created_at ASC")
    List<TaskApprovalRecord> selectPendingByTaskIdAndNodeId(@Param("taskId") Long taskId, @Param("nodeId") Long nodeId);

    @Select("SELECT COUNT(*) FROM task_approval_records WHERE task_id = #{taskId} AND node_id = #{nodeId} AND result = 'APPROVED'")
    Long countApprovedByTaskIdAndNodeId(@Param("taskId") Long taskId, @Param("nodeId") Long nodeId);

    @Select("SELECT * FROM task_approval_records WHERE task_id = #{taskId} AND node_id = #{nodeId} AND approver_id = #{approverId} AND result = 'PENDING' LIMIT 1")
    TaskApprovalRecord selectPendingByTaskNodeAndApprover(@Param("taskId") Long taskId, @Param("nodeId") Long nodeId, @Param("approverId") Long approverId);

    @Select("DELETE FROM task_approval_records WHERE task_id = #{taskId} AND node_order > #{nodeOrder}")
    int deleteByTaskIdAndNodeOrderGreaterThan(@Param("taskId") Long taskId, @Param("nodeOrder") Integer nodeOrder);
}
