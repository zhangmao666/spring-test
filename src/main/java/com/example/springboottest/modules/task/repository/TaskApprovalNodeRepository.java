package com.example.springboottest.modules.task.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.task.entity.TaskApprovalNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 审批节点配置Repository
 */
@Mapper
public interface TaskApprovalNodeRepository extends BaseMapper<TaskApprovalNode> {

    @Select("SELECT * FROM task_approval_nodes WHERE flow_id = #{flowId} ORDER BY node_order ASC")
    List<TaskApprovalNode> selectByFlowId(Long flowId);

    @Select("SELECT * FROM task_approval_nodes WHERE flow_id = #{flowId} ORDER BY node_order ASC LIMIT 1")
    TaskApprovalNode selectFirstNodeByFlowId(Long flowId);

    @Select("SELECT * FROM task_approval_nodes WHERE flow_id = #{flowId} AND node_order = #{nextOrder}")
    TaskApprovalNode selectNextNode(@Param("flowId") Long flowId, @Param("nextOrder") Integer nextOrder);

    default Optional<TaskApprovalNode> findNextNode(Long flowId, Integer currentOrder) {
        return Optional.ofNullable(selectNextNode(flowId, currentOrder + 1));
    }

    @Select("SELECT * FROM task_approval_nodes WHERE flow_id = #{flowId} AND node_order = #{nodeOrder}")
    TaskApprovalNode selectByFlowIdAndOrder(@Param("flowId") Long flowId, @Param("nodeOrder") Integer nodeOrder);

    default Optional<TaskApprovalNode> findByFlowIdAndOrder(Long flowId, Integer nodeOrder) {
        return Optional.ofNullable(selectByFlowIdAndOrder(flowId, nodeOrder));
    }
}
