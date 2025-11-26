package com.example.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboottest.DTO.*;
import com.example.springboottest.entity.*;
import com.example.springboottest.enums.*;
import com.example.springboottest.exception.BusinessException;
import com.example.springboottest.exception.ResourceNotFoundException;
import com.example.springboottest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    /**
     * 审批通过
     */
    @Transactional
    public ApprovalResultVO approve(ApproveRequest request, Long userId) {
        log.info("审批通过，任务ID: {}, 用户ID: {}", request.getTaskId(), userId);

        // 1. 校验任务状态
        Task task = taskRepository.selectById(request.getTaskId());
        if (task == null) {
            throw new ResourceNotFoundException("任务不存在");
        }

        TaskStatus status = TaskStatus.fromName(task.getStatus());
        if (!status.canApprove()) {
            throw new BusinessException("任务状态不允许审批，当前状态: " + status.getDisplayName());
        }

        // 2. 校验审批权限
        if (!hasApprovalPermission(task, userId)) {
            throw new BusinessException("您没有权限审批此任务");
        }

        // 3. 查询当前节点
        TaskApprovalNode currentNode = nodeRepository.selectById(task.getCurrentNodeId());
        if (currentNode == null) {
            throw new BusinessException("审批节点不存在");
        }

        // 4. 查询用户待审批记录
        TaskApprovalRecord pendingRecord = recordRepository.selectPendingByTaskNodeAndApprover(
                task.getId(), currentNode.getId(), userId);
        if (pendingRecord == null) {
            throw new BusinessException("未找到待审批记录");
        }

        // 5. 更新审批记录
        pendingRecord.setAction(ApprovalAction.APPROVE.name());
        pendingRecord.setResult(ApprovalResult.APPROVED.name());
        pendingRecord.setComment(request.getComment());
        pendingRecord.setApprovalTime(LocalDateTime.now());
        recordRepository.updateById(pendingRecord);

        // 6. 判断当前节点是否完成
        boolean nodeCompleted = isNodeCompleted(task.getId(), currentNode.getId(),
                ApprovalType.fromName(currentNode.getApprovalType()));

        String message;
        boolean flowCompleted = false;
        String nextNodeName = null;

        if (nodeCompleted) {
            log.info("节点审批完成，节点: {}", currentNode.getNodeName());

            // 7. 查询下一个节点
            TaskApprovalNode nextNode = nodeRepository.selectNextNode(
                    task.getFlowId(), currentNode.getNodeOrder() + 1);

            if (nextNode != null) {
                // 进入下一个审批节点
                moveToNextNode(task, nextNode);
                message = "审批通过，已进入下一节点: " + nextNode.getNodeName();
                nextNodeName = nextNode.getNodeName();
            } else {
                // 所有节点审批完成
                task.setStatus(TaskStatus.APPROVED.name());
                task.setCompletedAt(LocalDateTime.now());
                taskRepository.updateById(task);
                message = "审批通过，任务审批流程已全部完成";
                flowCompleted = true;
            }
        } else {
            // 当前节点还需其他人审批（会签场景）
            message = "审批通过，等待其他审批人审批";
            task.setStatus(TaskStatus.IN_PROGRESS.name());
            taskRepository.updateById(task);
        }

        log.info("审批通过成功，任务编号: {}, 节点: {}", task.getTaskNo(), currentNode.getNodeName());

        return buildApprovalResult(task, message, flowCompleted, nextNodeName);
    }

    /**
     * 驳回到指定节点
     */
    @Transactional
    public ApprovalResultVO reject(RejectRequest request, Long userId) {
        log.info("驳回任务，任务ID: {}, 用户ID: {}, 驳回到节点ID: {}",
                request.getTaskId(), userId, request.getRejectToNodeId());

        // 1. 校验任务状态
        Task task = taskRepository.selectById(request.getTaskId());
        if (task == null) {
            throw new ResourceNotFoundException("任务不存在");
        }

        TaskStatus status = TaskStatus.fromName(task.getStatus());
        if (!status.canApprove()) {
            throw new BusinessException("任务状态不允许审批，当前状态: " + status.getDisplayName());
        }

        // 2. 校验审批权限
        if (!hasApprovalPermission(task, userId)) {
            throw new BusinessException("您没有权限审批此任务");
        }

        // 3. 查询当前节点和驳回目标节点
        TaskApprovalNode currentNode = nodeRepository.selectById(task.getCurrentNodeId());
        TaskApprovalNode rejectToNode = nodeRepository.selectById(request.getRejectToNodeId());

        if (currentNode == null) {
            throw new BusinessException("当前审批节点不存在");
        }
        if (rejectToNode == null) {
            throw new ResourceNotFoundException("驳回目标节点不存在");
        }

        // 4. 校验驳回目标节点（必须是之前的节点）
        if (rejectToNode.getNodeOrder() >= currentNode.getNodeOrder()) {
            throw new BusinessException("只能驳回到之前的审批节点");
        }

        // 5. 创建驳回记录
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

        // 6. 更新任务状态和当前节点
        task.setStatus(TaskStatus.REJECTED.name());
        task.setCurrentNodeId(rejectToNode.getId());
        task.setCurrentNodeOrder(rejectToNode.getNodeOrder());
        taskRepository.updateById(task);

        // 7. 删除驳回节点之后的所有审批记录
        recordRepository.deleteByTaskIdAndNodeOrderGreaterThan(task.getId(), rejectToNode.getNodeOrder());

        // 8. 创建驳回节点的待审批记录
        List<Long> approverIds = getNodeApprovers(rejectToNode);
        for (Long approverId : approverIds) {
            User approver = userRepository.selectById(approverId);
            if (approver != null) {
                TaskApprovalRecord record = new TaskApprovalRecord();
                record.setTaskId(task.getId());
                record.setNodeId(rejectToNode.getId());
                record.setNodeName(rejectToNode.getNodeName());
                record.setNodeOrder(rejectToNode.getNodeOrder());
                record.setApproverId(approverId);
                record.setApproverName(approver.getUsername());
                record.setResult(ApprovalResult.PENDING.name());
                recordRepository.insert(record);
            }
        }

        log.info("任务驳回成功，任务编号: {}, 驳回到节点: {}",
                task.getTaskNo(), rejectToNode.getNodeName());

        String message = "任务已驳回到" + rejectToNode.getNodeName();
        return buildApprovalResult(task, message, false, rejectToNode.getNodeName());
    }

    /**
     * 转交他人审批
     */
    @Transactional
    public ApprovalResultVO transfer(TransferRequest request, Long userId) {
        log.info("转交审批，任务ID: {}, 用户ID: {}, 转交给: {}",
                request.getTaskId(), userId, request.getTransferToUserId());

        // 1. 校验任务状态
        Task task = taskRepository.selectById(request.getTaskId());
        if (task == null) {
            throw new ResourceNotFoundException("任务不存在");
        }

        TaskStatus status = TaskStatus.fromName(task.getStatus());
        if (!status.canApprove()) {
            throw new BusinessException("任务状态不允许审批，当前状态: " + status.getDisplayName());
        }

        // 2. 校验审批权限
        if (!hasApprovalPermission(task, userId)) {
            throw new BusinessException("您没有权限审批此任务");
        }

        // 3. 查询转交目标用户
        User targetUser = userRepository.selectById(request.getTransferToUserId());
        if (targetUser == null) {
            throw new ResourceNotFoundException("转交目标用户不存在");
        }

        // 4. 查询当前节点
        TaskApprovalNode currentNode = nodeRepository.selectById(task.getCurrentNodeId());
        if (currentNode == null) {
            throw new BusinessException("当前审批节点不存在");
        }

        // 5. 查询用户待审批记录并更新为已转交
        TaskApprovalRecord pendingRecord = recordRepository.selectPendingByTaskNodeAndApprover(
                task.getId(), currentNode.getId(), userId);
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

        // 6. 创建新的待审批记录（审批人为转交目标用户）
        TaskApprovalRecord newRecord = new TaskApprovalRecord();
        newRecord.setTaskId(task.getId());
        newRecord.setNodeId(currentNode.getId());
        newRecord.setNodeName(currentNode.getNodeName());
        newRecord.setNodeOrder(currentNode.getNodeOrder());
        newRecord.setApproverId(targetUser.getId());
        newRecord.setApproverName(targetUser.getUsername());
        newRecord.setResult(ApprovalResult.PENDING.name());
        recordRepository.insert(newRecord);

        log.info("审批转交成功，任务编号: {}, 转交给: {}",
                task.getTaskNo(), targetUser.getUsername());

        String message = "审批已转交给" + targetUser.getUsername();
        return buildApprovalResult(task, message, false, currentNode.getNodeName());
    }

    /**
     * 获取审批历史
     */
    @Transactional(readOnly = true)
    public List<ApprovalRecordVO> getApprovalHistory(Long taskId) {
        log.info("获取审批历史，任务ID: {}", taskId);

        List<TaskApprovalRecord> records = recordRepository.selectByTaskId(taskId);

        return records.stream().map(record -> {
            ApprovalRecordVO vo = new ApprovalRecordVO();
            vo.setId(record.getId());
            vo.setNodeName(record.getNodeName());
            vo.setNodeOrder(record.getNodeOrder());
            vo.setApproverName(record.getApproverName());
            vo.setAction(record.getAction());
            vo.setActionText(record.getAction() != null ?
                    ApprovalAction.fromName(record.getAction()).getDisplayName() : "");
            vo.setResult(record.getResult());
            vo.setResultText(ApprovalResult.fromName(record.getResult()).getDisplayName());
            vo.setComment(record.getComment());
            vo.setTransferToUserName(record.getTransferToUserName());
            vo.setApprovalTime(record.getApprovalTime());

            // 设置驳回节点名称
            if (record.getRejectToNodeId() != null) {
                TaskApprovalNode rejectNode = nodeRepository.selectById(record.getRejectToNodeId());
                vo.setRejectToNodeName(rejectNode != null ? rejectNode.getNodeName() : "");
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 判断用户是否有审批权限
     */
    private boolean hasApprovalPermission(Task task, Long userId) {
        if (task.getCurrentNodeId() == null) {
            return false;
        }

        TaskApprovalNode currentNode = nodeRepository.selectById(task.getCurrentNodeId());
        if (currentNode == null) {
            return false;
        }

        // 查询用户是否有待审批记录
        TaskApprovalRecord pendingRecord = recordRepository.selectPendingByTaskNodeAndApprover(
                task.getId(), currentNode.getId(), userId);

        return pendingRecord != null;
    }

    /**
     * 检查当前节点是否完成
     * 会签：所有审批人都通过才完成
     * 或签：任意一人通过即完成
     */
    private boolean isNodeCompleted(Long taskId, Long nodeId, ApprovalType approvalType) {
        if (ApprovalType.OR_SIGN == approvalType) {
            // 或签：任意一人通过即完成
            return isOrSignNodeCompleted(taskId, nodeId);
        } else if (ApprovalType.COUNTERSIGN == approvalType) {
            // 会签：所有人通过才完成
            return isCountersignNodeCompleted(taskId, nodeId);
        }
        return false;
    }

    /**
     * 检查或签节点是否完成
     * 只要有任意一人通过即可
     */
    private boolean isOrSignNodeCompleted(Long taskId, Long nodeId) {
        Long count = recordRepository.countApprovedByTaskIdAndNodeId(taskId, nodeId);
        return count > 0;
    }

    /**
     * 检查会签节点是否完成
     * 所有审批人都必须通过
     */
    private boolean isCountersignNodeCompleted(Long taskId, Long nodeId) {
        // 1. 获取该节点所有审批人
        TaskApprovalNode node = nodeRepository.selectById(nodeId);
        if (node == null) {
            return false;
        }

        List<Long> allApprovers = getNodeApprovers(node);
        if (allApprovers.isEmpty()) {
            return false;
        }

        // 2. 查询已通过的审批记录
        List<TaskApprovalRecord> approvedRecords = recordRepository.selectList(
                new LambdaQueryWrapper<TaskApprovalRecord>()
                        .eq(TaskApprovalRecord::getTaskId, taskId)
                        .eq(TaskApprovalRecord::getNodeId, nodeId)
                        .eq(TaskApprovalRecord::getResult, ApprovalResult.APPROVED.name())
        );

        // 3. 检查是否所有人都已审批通过
        List<Long> approvedUserIds = approvedRecords.stream()
                .map(TaskApprovalRecord::getApproverId)
                .distinct()
                .collect(Collectors.toList());

        return approvedUserIds.size() >= allApprovers.size() &&
                approvedUserIds.containsAll(allApprovers);
    }

    /**
     * 进入下一个审批节点
     */
    private void moveToNextNode(Task task, TaskApprovalNode nextNode) {
        log.info("进入下一个审批节点，任务: {}, 节点: {}", task.getTaskNo(), nextNode.getNodeName());

        // 1. 更新任务的当前节点
        task.setCurrentNodeId(nextNode.getId());
        task.setCurrentNodeOrder(nextNode.getNodeOrder());
        task.setStatus(TaskStatus.IN_PROGRESS.name());
        taskRepository.updateById(task);

        // 2. 创建待审批记录
        List<Long> approverIds = getNodeApprovers(nextNode);
        for (Long approverId : approverIds) {
            User approver = userRepository.selectById(approverId);
            if (approver != null) {
                TaskApprovalRecord record = new TaskApprovalRecord();
                record.setTaskId(task.getId());
                record.setNodeId(nextNode.getId());
                record.setNodeName(nextNode.getNodeName());
                record.setNodeOrder(nextNode.getNodeOrder());
                record.setApproverId(approverId);
                record.setApproverName(approver.getUsername());
                record.setResult(ApprovalResult.PENDING.name());
                recordRepository.insert(record);
            }
        }
    }

    /**
     * 获取节点的审批人列表
     */
    private List<Long> getNodeApprovers(TaskApprovalNode node) {
        List<Long> approverIds = new java.util.ArrayList<>();

        if (ApproverType.USER.name().equals(node.getApproverType())) {
            // 指定用户审批
            if (node.getApproverIds() != null && !node.getApproverIds().isEmpty()) {
                String[] ids = node.getApproverIds().split(",");
                for (String id : ids) {
                    try {
                        approverIds.add(Long.parseLong(id.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("解析审批人ID失败: {}", id);
                    }
                }
            }
        } else if (ApproverType.ROLE.name().equals(node.getApproverType())) {
            // 按角色审批
            if (node.getApproverRoles() != null && !node.getApproverRoles().isEmpty()) {
                String[] roles = node.getApproverRoles().split(",");
                for (String roleCode : roles) {
                    List<Long> userIds = userRoleRepository.selectUserIdsByRoleCode(roleCode.trim());
                    approverIds.addAll(userIds);
                }
            }
        }

        return approverIds.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 构建审批结果VO
     */
    private ApprovalResultVO buildApprovalResult(Task task, String message,
                                                  boolean flowCompleted, String nextNodeName) {
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
}
