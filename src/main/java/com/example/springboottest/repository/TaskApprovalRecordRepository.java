package com.example.springboottest.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.TaskApprovalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批记录Repository
 */
@Mapper
public interface TaskApprovalRecordRepository extends BaseMapper<TaskApprovalRecord> {

    /**
     * 查询任务的所有审批记录（按时间倒序）
     */
    @Select("SELECT * FROM task_approval_records WHERE task_id = #{taskId} " +
            "ORDER BY node_order ASC, created_at DESC")
    List<TaskApprovalRecord> selectByTaskId(Long taskId);

    /**
     * 查询任务指定节点的所有审批记录
     */
    @Select("SELECT * FROM task_approval_records WHERE task_id = #{taskId} AND node_id = #{nodeId} " +
            "ORDER BY created_at DESC")
    List<TaskApprovalRecord> selectByTaskIdAndNodeId(@Param("taskId") Long taskId, @Param("nodeId") Long nodeId);

    /**
     * 查询任务当前节点待审批的记录
     */
    @Select("SELECT * FROM task_approval_records WHERE task_id = #{taskId} AND node_id = #{nodeId} " +
            "AND result = 'PENDING' ORDER BY created_at ASC")
    List<TaskApprovalRecord> selectPendingByTaskIdAndNodeId(@Param("taskId") Long taskId, @Param("nodeId") Long nodeId);

    /**
     * 查询任务当前节点已通过的记录数
     */
    @Select("SELECT COUNT(*) FROM task_approval_records WHERE task_id = #{taskId} AND node_id = #{nodeId} " +
            "AND result = 'APPROVED'")
    Long countApprovedByTaskIdAndNodeId(@Param("taskId") Long taskId, @Param("nodeId") Long nodeId);

    /**
     * 查询用户对该任务该节点的待审批记录
     */
    @Select("SELECT * FROM task_approval_records WHERE task_id = #{taskId} AND node_id = #{nodeId} " +
            "AND approver_id = #{approverId} AND result = 'PENDING' LIMIT 1")
    TaskApprovalRecord selectPendingByTaskNodeAndApprover(@Param("taskId") Long taskId,
                                                           @Param("nodeId") Long nodeId,
                                                           @Param("approverId") Long approverId);

    /**
     * 删除指定节点顺序之后的所有审批记录（驳回时使用）
     */
    @Select("DELETE FROM task_approval_records WHERE task_id = #{taskId} AND node_order > #{nodeOrder}")
    int deleteByTaskIdAndNodeOrderGreaterThan(@Param("taskId") Long taskId, @Param("nodeOrder") Integer nodeOrder);
}
