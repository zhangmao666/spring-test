package com.example.springboottest.modules.task.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.common.dto.PageResult;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskApprovalFlowRepository flowRepository;
    private final TaskApprovalNodeRepository nodeRepository;
    private final TaskApprovalRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public TaskVO createTask(CreateTaskRequest request, Long userId) {
        log.info("创建任务，用户ID: {}, 标题: {}", userId, request.getTitle());

        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        TaskApprovalFlow flow = flowRepository.selectById(request.getFlowId());
        if (flow == null || flow.getStatus() != 1) {
            throw new BusinessException("审批流不存在或已禁用");
        }

        String taskNo = generateTaskNo();

        Task task = new Task();
        task.setTaskNo(taskNo);
        task.setTitle(request.getTitle());
        task.setContent(request.getContent());
        task.setTaskType(request.getTaskType());
        task.setPriority(request.getPriority());
        task.setStatus(TaskStatus.DRAFT.name());
        task.setCreatorId(userId);
        task.setCreatorName(user.getUsername());
        task.setFlowId(flow.getId());
        task.setCurrentNodeOrder(0);

        taskRepository.insert(task);
        log.info("任务创建成功，任务编号: {}", taskNo);

        return convertToVO(task, flow);
    }

    @Transactional
    public TaskVO submitTask(Long taskId, Long userId) {
        log.info("提交任务审批，任务ID: {}, 用户ID: {}", taskId, userId);

        Task task = taskRepository.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("任务不存在");
        }

        TaskStatus status = TaskStatus.fromName(task.getStatus());
        if (!status.canSubmit()) {
            throw new BusinessException("任务状态不允许提交，当前状态: " + status.getDisplayName());
        }

        if (!task.getCreatorId().equals(userId)) {
            throw new BusinessException("只有任务创建人才能提交审批");
        }

        TaskApprovalFlow flow = flowRepository.selectById(task.getFlowId());
        if (flow == null) {
            throw new BusinessException("审批流配置不存在");
        }

        TaskApprovalNode firstNode = nodeRepository.selectFirstNodeByFlowId(flow.getId());
        if (firstNode == null) {
            throw new BusinessException("审批流未配置审批节点");
        }

        task.setStatus(TaskStatus.PENDING.name());
        task.setCurrentNodeId(firstNode.getId());
        task.setCurrentNodeOrder(firstNode.getNodeOrder());
        task.setSubmittedAt(LocalDateTime.now());
        taskRepository.updateById(task);

        List<Long> approverIds = getNodeApprovers(firstNode);
        for (Long approverId : approverIds) {
            User approver = userRepository.selectById(approverId);
            if (approver != null) {
                TaskApprovalRecord record = new TaskApprovalRecord();
                record.setTaskId(task.getId());
                record.setNodeId(firstNode.getId());
                record.setNodeName(firstNode.getNodeName());
                record.setNodeOrder(firstNode.getNodeOrder());
                record.setApproverId(approverId);
                record.setApproverName(approver.getUsername());
                record.setResult(ApprovalResult.PENDING.name());
                recordRepository.insert(record);
            }
        }

        log.info("任务提交成功，任务编号: {}", task.getTaskNo());
        return convertToVO(task, flow);
    }

    @Transactional
    public TaskVO withdrawTask(Long taskId, Long userId, String reason) {
        Task task = taskRepository.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("任务不存在");
        }

        TaskStatus status = TaskStatus.fromName(task.getStatus());
        if (!status.canWithdraw()) {
            throw new BusinessException("任务状态不允许撤回");
        }

        if (!task.getCreatorId().equals(userId)) {
            throw new BusinessException("只有任务创建人才能撤回");
        }

        task.setStatus(TaskStatus.WITHDRAWN.name());
        taskRepository.updateById(task);

        User user = userRepository.selectById(userId);
        TaskApprovalRecord record = new TaskApprovalRecord();
        record.setTaskId(task.getId());
        record.setNodeId(task.getCurrentNodeId());
        record.setNodeOrder(task.getCurrentNodeOrder());
        record.setApproverId(userId);
        record.setApproverName(user != null ? user.getUsername() : "");
        record.setAction(ApprovalAction.WITHDRAW.name());
        record.setResult(ApprovalResult.WITHDRAWN.name());
        record.setComment(reason);
        record.setApprovalTime(LocalDateTime.now());
        recordRepository.insert(record);

        TaskApprovalFlow flow = flowRepository.selectById(task.getFlowId());
        return convertToVO(task, flow);
    }

    @Transactional(readOnly = true)
    public TaskDetailVO getTaskDetail(Long taskId, Long userId) {
        Task task = taskRepository.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("任务不存在");
        }

        TaskApprovalFlow flow = flowRepository.selectById(task.getFlowId());
        List<TaskApprovalNode> nodes = nodeRepository.selectByFlowId(task.getFlowId());
        List<TaskApprovalRecord> records = recordRepository.selectByTaskId(taskId);

        TaskDetailVO detailVO = new TaskDetailVO();
        TaskVO taskVO = convertToVO(task, flow);
        
        detailVO.setId(taskVO.getId());
        detailVO.setTaskNo(taskVO.getTaskNo());
        detailVO.setTitle(taskVO.getTitle());
        detailVO.setContent(taskVO.getContent());
        detailVO.setTaskType(taskVO.getTaskType());
        detailVO.setPriority(taskVO.getPriority());
        detailVO.setPriorityText(taskVO.getPriorityText());
        detailVO.setStatus(taskVO.getStatus());
        detailVO.setStatusText(taskVO.getStatusText());
        detailVO.setCreatorId(taskVO.getCreatorId());
        detailVO.setCreatorName(taskVO.getCreatorName());
        detailVO.setFlowId(taskVO.getFlowId());
        detailVO.setFlowName(taskVO.getFlowName());
        detailVO.setCurrentNodeName(taskVO.getCurrentNodeName());
        detailVO.setCreatedAt(taskVO.getCreatedAt());
        detailVO.setSubmittedAt(taskVO.getSubmittedAt());
        detailVO.setCompletedAt(taskVO.getCompletedAt());

        detailVO.setFlow(convertFlowToVO(flow));
        detailVO.setNodes(nodes.stream().map(this::convertNodeToVO).collect(Collectors.toList()));
        detailVO.setRecords(records.stream().map(this::convertRecordToVO).collect(Collectors.toList()));

        if (task.getCurrentNodeId() != null) {
            List<TaskApprovalRecord> pendingRecords = recordRepository.selectPendingByTaskIdAndNodeId(taskId, task.getCurrentNodeId());
            detailVO.setCurrentApprovers(pendingRecords.stream().map(TaskApprovalRecord::getApproverId).collect(Collectors.toList()));
        } else {
            detailVO.setCurrentApprovers(Collections.emptyList());
        }

        detailVO.setCanApprove(detailVO.getCurrentApprovers().contains(userId));
        detailVO.setCanWithdraw(task.getCreatorId().equals(userId) && TaskStatus.fromName(task.getStatus()).canWithdraw());

        return detailVO;
    }

    @Transactional(readOnly = true)
    public PageResult<TaskVO> getMyPendingTasks(Long userId, int page, int size) {
        Page<Task> pageParam = new Page<>(page, size);
        IPage<Task> pageResult = taskRepository.selectPendingTasksByApproverId(pageParam, userId);

        List<TaskVO> taskVOs = pageResult.getRecords().stream()
                .map(task -> convertToVO(task, flowRepository.selectById(task.getFlowId())))
                .collect(Collectors.toList());

        return new PageResult<>(taskVOs, pageResult.getTotal(), page, size);
    }

    @Transactional(readOnly = true)
    public PageResult<TaskVO> getMyCreatedTasks(Long userId, int page, int size) {
        Page<Task> pageParam = new Page<>(page, size);
        IPage<Task> pageResult = taskRepository.selectPageByCreatorId(pageParam, userId);

        List<TaskVO> taskVOs = pageResult.getRecords().stream()
                .map(task -> convertToVO(task, flowRepository.selectById(task.getFlowId())))
                .collect(Collectors.toList());

        return new PageResult<>(taskVOs, pageResult.getTotal(), page, size);
    }

    private String generateTaskNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "TASK" + dateStr;
        String maxTaskNo = taskRepository.getMaxTaskNoByPrefix(prefix);

        int sequence = 1;
        if (maxTaskNo != null && maxTaskNo.length() >= prefix.length() + 6) {
            try {
                sequence = Integer.parseInt(maxTaskNo.substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                log.warn("解析任务编号序列失败: {}", maxTaskNo);
            }
        }
        return prefix + String.format("%06d", sequence);
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

    private TaskVO convertToVO(Task task, TaskApprovalFlow flow) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setTaskNo(task.getTaskNo());
        vo.setTitle(task.getTitle());
        vo.setContent(task.getContent());
        vo.setTaskType(task.getTaskType());
        vo.setPriority(task.getPriority());
        vo.setPriorityText(getPriorityText(task.getPriority()));
        vo.setStatus(task.getStatus());
        vo.setStatusText(TaskStatus.fromName(task.getStatus()).getDisplayName());
        vo.setCreatorId(task.getCreatorId());
        vo.setCreatorName(task.getCreatorName());
        vo.setFlowId(task.getFlowId());
        vo.setFlowName(flow != null ? flow.getFlowName() : "");
        vo.setCreatedAt(task.getCreatedAt());
        vo.setSubmittedAt(task.getSubmittedAt());
        vo.setCompletedAt(task.getCompletedAt());

        if (task.getCurrentNodeId() != null) {
            TaskApprovalNode currentNode = nodeRepository.selectById(task.getCurrentNodeId());
            vo.setCurrentNodeName(currentNode != null ? currentNode.getNodeName() : "");
        }
        return vo;
    }

    private TaskApprovalFlowVO convertFlowToVO(TaskApprovalFlow flow) {
        if (flow == null) return null;
        TaskApprovalFlowVO vo = new TaskApprovalFlowVO();
        vo.setId(flow.getId());
        vo.setFlowCode(flow.getFlowCode());
        vo.setFlowName(flow.getFlowName());
        vo.setDescription(flow.getDescription());
        vo.setTaskType(flow.getTaskType());
        vo.setStatus(flow.getStatus());
        vo.setVersion(flow.getVersion());
        vo.setCreatedAt(flow.getCreatedAt());
        return vo;
    }

    private ApprovalNodeVO convertNodeToVO(TaskApprovalNode node) {
        ApprovalNodeVO vo = new ApprovalNodeVO();
        vo.setId(node.getId());
        vo.setNodeCode(node.getNodeCode());
        vo.setNodeName(node.getNodeName());
        vo.setNodeOrder(node.getNodeOrder());
        vo.setApprovalType(node.getApprovalType());
        vo.setApprovalTypeText(ApprovalType.fromName(node.getApprovalType()).getDisplayName());
        List<Long> approverIds = getNodeApprovers(node);
        vo.setApproverNames(approverIds.stream()
                .map(id -> userRepository.selectById(id))
                .filter(Objects::nonNull)
                .map(User::getUsername)
                .collect(Collectors.toList()));
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

    private String getPriorityText(Integer priority) {
        if (priority == null) return "未设置";
        return switch (priority) {
            case 1 -> "低";
            case 2 -> "中";
            case 3 -> "高";
            case 4 -> "紧急";
            default -> "未知";
        };
    }
}
