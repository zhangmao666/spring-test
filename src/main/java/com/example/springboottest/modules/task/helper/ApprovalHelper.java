package com.example.springboottest.modules.task.helper;

import com.example.springboottest.modules.auth.repository.UserRoleRepository;
import com.example.springboottest.modules.task.dto.ApprovalRecordVO;
import com.example.springboottest.modules.task.entity.TaskApprovalNode;
import com.example.springboottest.modules.task.entity.TaskApprovalRecord;
import com.example.springboottest.modules.task.enums.ApprovalAction;
import com.example.springboottest.modules.task.enums.ApprovalResult;
import com.example.springboottest.modules.task.enums.ApproverType;
import com.example.springboottest.modules.task.repository.TaskApprovalNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批相关帮助类
 * 提供审批人解析、VO转换等公共方法
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalHelper {

    private final UserRoleRepository userRoleRepository;
    private final TaskApprovalNodeRepository nodeRepository;

    /**
     * 获取节点的审批人列表
     *
     * @param node 审批节点
     * @return 审批人ID列表
     */
    public List<Long> getNodeApprovers(TaskApprovalNode node) {
        List<Long> approverIds = new ArrayList<>();
        if (ApproverType.USER.name().equals(node.getApproverType())) {
            if (node.getApproverIds() != null && !node.getApproverIds().isEmpty()) {
                for (String id : node.getApproverIds().split(",")) {
                    try {
                        approverIds.add(Long.parseLong(id.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("解析审批人ID失败: {}", id);
                    }
                }
            }
        } else if (ApproverType.ROLE.name().equals(node.getApproverType())) {
            if (node.getApproverRoles() != null && !node.getApproverRoles().isEmpty()) {
                for (String roleCode : node.getApproverRoles().split(",")) {
                    approverIds.addAll(userRoleRepository.selectUserIdsByRoleCode(roleCode.trim()));
                }
            }
        }
        return approverIds.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 将审批记录实体转换为VO
     *
     * @param record 审批记录实体
     * @return 审批记录VO
     */
    public ApprovalRecordVO convertRecordToVO(TaskApprovalRecord record) {
        ApprovalRecordVO vo = new ApprovalRecordVO();
        vo.setId(record.getId());
        vo.setNodeName(record.getNodeName());
        vo.setNodeOrder(record.getNodeOrder());
        vo.setApproverName(record.getApproverName());
        vo.setAction(record.getAction());
        vo.setActionText(record.getAction() != null ? ApprovalAction.fromName(record.getAction()).getDisplayName() : "");
        vo.setResult(record.getResult());
        vo.setResultText(ApprovalResult.fromName(record.getResult()).getDisplayName());
        vo.setComment(record.getComment());
        vo.setTransferToUserName(record.getTransferToUserName());
        vo.setApprovalTime(record.getApprovalTime());
        if (record.getRejectToNodeId() != null) {
            TaskApprovalNode rejectNode = nodeRepository.selectById(record.getRejectToNodeId());
            vo.setRejectToNodeName(rejectNode != null ? rejectNode.getNodeName() : "");
        }
        return vo;
    }
}
