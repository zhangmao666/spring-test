package com.example.springboottest.modules.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboottest.exception.BusinessException;
import com.example.springboottest.exception.ResourceNotFoundException;
import com.example.springboottest.modules.auth.entity.User;
import com.example.springboottest.modules.auth.repository.UserRepository;
import com.example.springboottest.modules.auth.repository.UserRoleRepository;
import com.example.springboottest.modules.task.dto.*;
import com.example.springboottest.modules.task.entity.*;
import com.example.springboottest.modules.task.enums.*;
import com.example.springboottest.modules.task.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务审批服务（核心审批逻辑）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskApprovalService {

    private final TaskRepository taskRepository;
    private final TaskApprovalNodeRepository nodeRepository;
    private final TaskApprovalRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public ApprovalResultVO approve(ApproveRequest request, Long userId) {
        log.info("审批通过，任务ID: {}, 用户ID: {}", request.getTaskId(), userId);

        Task task = taskRepository.selectById(request.getTaskId());
        if (task == null) {
            throw new ResourceNotFoundException("任务不存在");
        }

        TaskStatus status = TaskStatus.fromName(task.getStatus());
        if (!status.canApprove()) {
            throw new BusinessException("任务状态不允许审批，当前状态: " + status.getDisplayName());
        }

        if (!hasApprovalPermission(task, userId)) {
            throw new BusinessException("您没有权限审批此任务");
        }

        TaskApprovalNode currentNode = nodeRepository.selectById(task.getCurrentNodeId());
        if (currentNode == null) {
            throw new BusinessException("审批节点不存在");
        }

        TaskApprovalRecord pendingRecord = recordRepository.selectPendingByTaskNodeAndApprover(task.getId(), currentNode.getId(), userId);
        if (pendingRecord == null) {
            throw new BusinessException("未找到待审批记录");
        }

        pendingRecord.setAction(ApprovalAction.APPROVE.name());
        pendingRecord.setResult(ApprovalResult.APPROVED.name());
        pendingRecord.setComment(request.getComment());
        pendingRecord.setApprovalTime(LocalDateTime.now());
        recordRepository.updateById(pendingRecord);

        boolean nodeCompleted = isNodeCompleted(task.getId(), currentNode.getId(), ApprovalType.fromName(currentNode.getApprovalType()));

        String message;
        boolean flowCompleted = false;
        String nextNodeName = null;

        if (nodeCompleted) {
            TaskApprovalNode nextNode = nodeRepository.selectNextNode(task.getFlowId(), currentNode.getNodeOrder() + 1);
            if (nextNode != null) {
                moveToNextNode(task, nextNode);
                message = "审批通过，已进入下一节点: " + nextNode.getNodeName();
                nextNodeName = nextNode.getNodeName();
            } else {
                task.setStatus(TaskStatus.APPROVED.name());
                task.setCompletedAt(LocalDateTime.now());
                taskRepository.updateById(task);
                message = "审批通过，任务审批流程已全部完成";
                flowCompleted = true;
            }
        } else {
            message = "审批通过，等待其他审批人审批";
            task.setStatus(TaskStatus.IN_PROGRESS.name());
            taskRepository.updateById(task);
        }

        return buildApprovalResult(task, message, flowCompleted, nextNodeName);
    }

    @Transactional
    public ApprovalResultVO reject(RejectRequest request, Long userId) {
        Task task = taskRepository.selectById(request.getTaskId());
        if (task == null) {
            throw new ResourceNotFoundException("任务不存在");
        }

        TaskStatus status = TaskStatus.fromName(task.getStatus());
        if (!status.canApprove()) {
            throw new BusinessException("任务状态不允许审批");
        }

        if (!hasApprovalPermission(task, userId)) {
            throw new BusinessException("您没有权限审批此任务");
        }

        TaskApprovalNode currentNode = nodeRepository.selectById(task.getCurrentNodeId());
        TaskApprovalNode rejectToNode = nodeRepository.selectById(request.getRejectToNodeId());

        if (currentNode == null || rejectToNode == null) {
            throw new BusinessException("审批节点不存在");
        }

        if (rejectToNode.getNodeOrder() >= currentNode.getNodeOrder()) {
            throw new BusinessException("只能驳回到之前的审批节点");
        }

        User user = userRepository.selectById(userId);
        TaskApprovalRecord rejectRecord = new TaskApprovalRecord();
        rejectRecord.setTaskId(task.getId());
        rejectRecord.setNodeId(currentNode.getId());
        rejectRecord.setNodeName(currentNode.getNodeName());
        rejectRecord.setNodeOrder(currentNode.getNodeOrder());
        rejectRecord.setApproverId(userId);
        rejectRecord.setApproverName(user != null ? user.getUsername() : "");
        rejectRecord.setAction(ApprovalAction.REJECT.name());
        rejectRecord.setResult(ApprovalResult.REJECTED.name());
        rejectRecord.setComment(request.getComment());
        rejectRecord.setRejectToNodeId(rejectToNode.getId());
        rejectRecord.setApprovalTime(LocalDateTime.now());
        recordRepository.insert(rejectRecord);

        task.setStatus(TaskStatus.REJECTED.name());
        task.setCurrentNodeId(rejectToNode.getId());
        task.setCurrentNodeOrder(rejectToNode.getNodeOrder());
        taskRepository.updateById(task);

        recordRepository.deleteByTaskIdAndNodeOrderGreaterThan(task.getId(), rejectToNode.getNodeOrder());

        createPendingRecordsForNode(task, rejectToNode);

        String message = "任务已驳回到" + rejectToNode.getNodeName();
        return buildApprovalResult(task, message, false, rejectToNode.getNodeName());
    }

    @Transactional
    public ApprovalResultVO transfer(TransferRequest request, Long userId) {
        Task task = taskRepository.selectById(request.getTaskId());
        if (task == null) {
            throw new ResourceNotFoundException("任务不存在");
        }

        if (!hasApprovalPermission(task, userId)) {
            throw new BusinessException("您没有权限审批此任务");
        }

        User targetUser = userRepository.selectById(request.getTransferToUserId());
        if (targetUser == null) {
            throw new ResourceNotFoundException("转交目标用户不存在");
        }

        TaskApprovalNode currentNode = nodeRepository.selectById(task.getCurrentNodeId());
        if (currentNode == null) {
            throw new BusinessException("当前审批节点不存在");
        }

        TaskApprovalRecord pendingRecord = recordRepository.selectPendingByTaskNodeAndApprover(task.getId(), currentNode.getId(), userId);
        if (pendingRecord == null) {
            throw new BusinessException("未找到待审批记录");
        }

        pendingRecord.setAction(ApprovalAction.TRANSFER.name());
        pendingRecord.setResult(ApprovalResult.TRANSFERRED.name());
        pendingRecord.setComment(request.getComment());
        pendingRecord.setTransferToUserId(targetUser.getId());
        pendingRecord.setTransferToUserName(targetUser.getUsername());
        pendingRecord.setApprovalTime(LocalDateTime.now());
        recordRepository.updateById(pendingRecord);

        TaskApprovalRecord newRecord = new TaskApprovalRecord();
        newRecord.setTaskId(task.getId());
        newRecord.setNodeId(currentNode.getId());
        newRecord.setNodeName(currentNode.getNodeName());
        newRecord.setNodeOrder(currentNode.getNodeOrder());
        newRecord.setApproverId(targetUser.getId());
        newRecord.setApproverName(targetUser.getUsername());
        newRecord.setResult(ApprovalResult.PENDING.name());
        recordRepository.insert(newRecord);

        String message = "审批已转交给" + targetUser.getUsername();
        return buildApprovalResult(task, message, false, currentNode.getNodeName());
    }

    @Transactional(readOnly = true)
    public List<ApprovalRecordVO> getApprovalHistory(Long taskId) {
        List<TaskApprovalRecord> records = recordRepository.selectByTaskId(taskId);
        return records.stream().map(this::convertRecordToVO).collect(Collectors.toList());
    }

    private boolean hasApprovalPermission(Task task, Long userId) {
        if (task.getCurrentNodeId() == null) return false;
        TaskApprovalRecord pendingRecord = recordRepository.selectPendingByTaskNodeAndApprover(task.getId(), task.getCurrentNodeId(), userId);
        return pendingRecord != null;
    }

    private boolean isNodeCompleted(Long taskId, Long nodeId, ApprovalType approvalType) {
        if (ApprovalType.OR_SIGN == approvalType) {
            return recordRepository.countApprovedByTaskIdAndNodeId(taskId, nodeId) > 0;
        } else if (ApprovalType.COUNTERSIGN == approvalType) {
            TaskApprovalNode node = nodeRepository.selectById(nodeId);
            if (node == null) return false;
            List<Long> allApprovers = getNodeApprovers(node);
            if (allApprovers.isEmpty()) return false;
            List<TaskApprovalRecord> approvedRecords = recordRepository.selectList(
                    new LambdaQueryWrapper<TaskApprovalRecord>()
                            .eq(TaskApprovalRecord::getTaskId, taskId)
                            .eq(TaskApprovalRecord::getNodeId, nodeId)
                            .eq(TaskApprovalRecord::getResult, ApprovalResult.APPROVED.name()));
            List<Long> approvedUserIds = approvedRecords.stream().map(TaskApprovalRecord::getApproverId).distinct().collect(Collectors.toList());
            return approvedUserIds.size() >= allApprovers.size() && approvedUserIds.containsAll(allApprovers);
        }
        return false;
    }

    private void moveToNextNode(Task task, TaskApprovalNode nextNode) {
        task.setCurrentNodeId(nextNode.getId());
        task.setCurrentNodeOrder(nextNode.getNodeOrder());
        task.setStatus(TaskStatus.IN_PROGRESS.name());
        taskRepository.updateById(task);
        createPendingRecordsForNode(task, nextNode);
    }

    private void createPendingRecordsForNode(Task task, TaskApprovalNode node) {
        List<Long> approverIds = getNodeApprovers(node);
        for (Long approverId : approverIds) {
            User approver = userRepository.selectById(approverId);
            if (approver != null) {
                TaskApprovalRecord record = new TaskApprovalRecord();
                record.setTaskId(task.getId());
                record.setNodeId(node.getId());
                record.setNodeName(node.getNodeName());
                record.setNodeOrder(node.getNodeOrder());
                record.setApproverId(approverId);
                record.setApproverName(approver.getUsername());
                record.setResult(ApprovalResult.PENDING.name());
                recordRepository.insert(record);
            }
        }
    }

    private List<Long> getNodeApprovers(TaskApprovalNode node) {
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

    private ApprovalResultVO buildApprovalResult(Task task, String message, boolean flowCompleted, String nextNodeName) {
        ApprovalResultVO vo = new ApprovalResultVO();
        vo.setTaskId(task.getId());
        vo.setTaskNo(task.getTaskNo());
        vo.setStatus(task.getStatus());
        vo.setStatusText(TaskStatus.fromName(task.getStatus()).getDisplayName());
        vo.setMessage(message);
        vo.setFlowCompleted(flowCompleted);
        vo.setNextNodeName(nextNodeName);
        return vo;
    }

    private ApprovalRecordVO convertRecordToVO(TaskApprovalRecord record) {
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
