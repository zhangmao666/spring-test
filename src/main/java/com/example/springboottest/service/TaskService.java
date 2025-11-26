package com.example.springboottest.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /**
     * 创建任务
     */
    @Transactional
    public TaskVO createTask(CreateTaskRequest request, Long userId) {
        log.info("创建任务，用户ID: {}, 标题: {}", userId, request.getTitle());

        // 1. 查询用户信息
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        // 2. 验证审批流是否存在
        TaskApprovalFlow flow = flowRepository.selectById(request.getFlowId());
        if (flow == null || flow.getStatus() != 1) {
            throw new BusinessException("审批流不存在或已禁用");
        }

        // 3. 生成任务编号
        String taskNo = generateTaskNo();

        // 4. 创建任务实体
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

        // 5. 保存任务
        taskRepository.insert(task);

        log.info("任务创建成功，任务编号: {}", taskNo);

        // 6. 转换为VO返回
        return convertToVO(task, flow);
    }

    /**
     * 提交任务审批
     */
    @Transactional
    public TaskVO submitTask(Long taskId, Long userId) {
        log.info("提交任务审批，任务ID: {}, 用户ID: {}", taskId, userId);

        // 1. 查询任务
        Task task = taskRepository.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("任务不存在");
        }

        // 2. 校验任务状态
        TaskStatus status = TaskStatus.fromName(task.getStatus());
        if (!status.canSubmit()) {
            throw new BusinessException("任务状态不允许提交，当前状态: " + status.getDisplayName());
        }

        // 3. 校验用户权限（必须是创建人）
        if (!task.getCreatorId().equals(userId)) {
            throw new BusinessException("只有任务创建人才能提交审批");
        }

        // 4. 获取审批流配置
        TaskApprovalFlow flow = flowRepository.selectById(task.getFlowId());
        if (flow == null) {
            throw new BusinessException("审批流配置不存在");
        }

        // 5. 获取第一个审批节点
        TaskApprovalNode firstNode = nodeRepository.selectFirstNodeByFlowId(flow.getId());
        if (firstNode == null) {
            throw new BusinessException("审批流未配置审批节点");
        }

        // 6. 更新任务状态
        task.setStatus(TaskStatus.PENDING.name());
        task.setCurrentNodeId(firstNode.getId());
        task.setCurrentNodeOrder(firstNode.getNodeOrder());
        task.setSubmittedAt(LocalDateTime.now());
        taskRepository.updateById(task);

        // 7. 创建待审批记录
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
                record.setAction(null);
                record.setResult(ApprovalResult.PENDING.name());
                recordRepository.insert(record);
            }
        }

        log.info("任务提交成功，任务编号: {}, 当前节点: {}", task.getTaskNo(), firstNode.getNodeName());

        return convertToVO(task, flow);
    }

    /**
     * 撤回任务
     */
    @Transactional
    public TaskVO withdrawTask(Long taskId, Long userId, String reason) {
        log.info("撤回任务，任务ID: {}, 用户ID: {}, 原因: {}", taskId, userId, reason);

        // 1. 查询任务
        Task task = taskRepository.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("任务不存在");
        }

        // 2. 校验任务状态
        TaskStatus status = TaskStatus.fromName(task.getStatus());
        if (!status.canWithdraw()) {
            throw new BusinessException("任务状态不允许撤回，当前状态: " + status.getDisplayName());
        }

        // 3. 校验用户权限（必须是创建人）
        if (!task.getCreatorId().equals(userId)) {
            throw new BusinessException("只有任务创建人才能撤回");
        }

        // 4. 更新任务状态
        task.setStatus(TaskStatus.WITHDRAWN.name());
        taskRepository.updateById(task);

        // 5. 创建撤回记录
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

        log.info("任务撤回成功，任务编号: {}", task.getTaskNo());

        TaskApprovalFlow flow = flowRepository.selectById(task.getFlowId());
        return convertToVO(task, flow);
    }

    /**
     * 获取任务详情
     */
    @Transactional(readOnly = true)
    public TaskDetailVO getTaskDetail(Long taskId, Long userId) {
        log.info("获取任务详情，任务ID: {}, 用户ID: {}", taskId, userId);

        // 1. 查询任务基本信息
        Task task = taskRepository.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("任务不存在");
        }

        // 2. 查询审批流配置
        TaskApprovalFlow flow = flowRepository.selectById(task.getFlowId());
        List<TaskApprovalNode> nodes = nodeRepository.selectByFlowId(task.getFlowId());

        // 3. 查询所有审批记录
        List<TaskApprovalRecord> records = recordRepository.selectByTaskId(taskId);

        // 4. 组装返回VO
        TaskDetailVO detailVO = new TaskDetailVO();
        // 复制基本信息
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

        // 设置审批流信息
        detailVO.setFlow(convertFlowToVO(flow));

        // 设置审批节点信息
        detailVO.setNodes(nodes.stream().map(this::convertNodeToVO).collect(Collectors.toList()));

        // 设置审批记录
        detailVO.setRecords(records.stream().map(this::convertRecordToVO).collect(Collectors.toList()));

        // 设置当前待审批人
        if (task.getCurrentNodeId() != null) {
            List<TaskApprovalRecord> pendingRecords = recordRepository.selectPendingByTaskIdAndNodeId(
                    taskId, task.getCurrentNodeId());
            detailVO.setCurrentApprovers(pendingRecords.stream()
                    .map(TaskApprovalRecord::getApproverId)
                    .collect(Collectors.toList()));
        } else {
            detailVO.setCurrentApprovers(Collections.emptyList());
        }

        // 判断当前用户是否可以审批
        detailVO.setCanApprove(detailVO.getCurrentApprovers().contains(userId));

        // 判断当前用户是否可以撤回
        detailVO.setCanWithdraw(task.getCreatorId().equals(userId) &&
                TaskStatus.fromName(task.getStatus()).canWithdraw());

        return detailVO;
    }

    /**
     * 获取我的待办任务列表
     */
    @Transactional(readOnly = true)
    public PageResult<TaskVO> getMyPendingTasks(Long userId, int page, int size) {
        log.info("获取待办任务列表，用户ID: {}, 页码: {}, 每页数量: {}", userId, page, size);

        Page<Task> pageParam = new Page<>(page, size);
        IPage<Task> pageResult = taskRepository.selectPendingTasksByApproverId(pageParam, userId);

        List<TaskVO> taskVOs = pageResult.getRecords().stream()
                .map(task -> {
                    TaskApprovalFlow flow = flowRepository.selectById(task.getFlowId());
                    return convertToVO(task, flow);
                })
                .collect(Collectors.toList());

        return new PageResult<>(taskVOs, pageResult.getTotal(), page, size);
    }

    /**
     * 获取我创建的任务列表
     */
    @Transactional(readOnly = true)
    public PageResult<TaskVO> getMyCreatedTasks(Long userId, int page, int size) {
        log.info("获取我创建的任务列表，用户ID: {}, 页码: {}, 每页数量: {}", userId, page, size);

        Page<Task> pageParam = new Page<>(page, size);
        IPage<Task> pageResult = taskRepository.selectPageByCreatorId(pageParam, userId);

        List<TaskVO> taskVOs = pageResult.getRecords().stream()
                .map(task -> {
                    TaskApprovalFlow flow = flowRepository.selectById(task.getFlowId());
                    return convertToVO(task, flow);
                })
                .collect(Collectors.toList());

        return new PageResult<>(taskVOs, pageResult.getTotal(), page, size);
    }

    /**
     * 获取我审批过的任务列表
     */
    @Transactional(readOnly = true)
    public PageResult<TaskVO> getMyApprovedTasks(Long userId, int page, int size) {
        log.info("获取我审批过的任务列表，用户ID: {}, 页码: {}, 每页数量: {}", userId, page, size);

        Page<Task> pageParam = new Page<>(page, size);
        IPage<Task> pageResult = taskRepository.selectApprovedTasksByApproverId(pageParam, userId);

        List<TaskVO> taskVOs = pageResult.getRecords().stream()
                .map(task -> {
                    TaskApprovalFlow flow = flowRepository.selectById(task.getFlowId());
                    return convertToVO(task, flow);
                })
                .collect(Collectors.toList());

        return new PageResult<>(taskVOs, pageResult.getTotal(), page, size);
    }

    /**
     * 生成任务编号
     * 格式：TASK + 年月日 + 6位序列号
     * 例如：TASK20250121000001
     */
    private String generateTaskNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "TASK" + dateStr;

        // 查询今日最大序列号
        String maxTaskNo = taskRepository.getMaxTaskNoByPrefix(prefix);

        int sequence = 1;
        if (maxTaskNo != null && maxTaskNo.length() >= prefix.length() + 6) {
            String seqStr = maxTaskNo.substring(prefix.length());
            try {
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                log.warn("解析任务编号序列失败: {}", maxTaskNo);
            }
        }

        return prefix + String.format("%06d", sequence);
    }

    /**
     * 获取节点的审批人列表
     */
    private List<Long> getNodeApprovers(TaskApprovalNode node) {
        List<Long> approverIds = new ArrayList<>();

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
     * 转换任务为VO
     */
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

        // 设置当前节点名称
        if (task.getCurrentNodeId() != null) {
            TaskApprovalNode currentNode = nodeRepository.selectById(task.getCurrentNodeId());
            vo.setCurrentNodeName(currentNode != null ? currentNode.getNodeName() : "");
        }

        return vo;
    }

    /**
     * 转换审批流为VO
     */
    private TaskApprovalFlowVO convertFlowToVO(TaskApprovalFlow flow) {
        if (flow == null) {
            return null;
        }

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

    /**
     * 转换审批节点为VO
     */
    private ApprovalNodeVO convertNodeToVO(TaskApprovalNode node) {
        ApprovalNodeVO vo = new ApprovalNodeVO();
        vo.setId(node.getId());
        vo.setNodeCode(node.getNodeCode());
        vo.setNodeName(node.getNodeName());
        vo.setNodeOrder(node.getNodeOrder());
        vo.setApprovalType(node.getApprovalType());
        vo.setApprovalTypeText(ApprovalType.fromName(node.getApprovalType()).getDisplayName());

        // 设置审批人名称列表
        List<Long> approverIds = getNodeApprovers(node);
        List<String> approverNames = approverIds.stream()
                .map(id -> {
                    User user = userRepository.selectById(id);
                    return user != null ? user.getUsername() : "";
                })
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());
        vo.setApproverNames(approverNames);

        return vo;
    }

    /**
     * 转换审批记录为VO
     */
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

        // 设置驳回节点名称
        if (record.getRejectToNodeId() != null) {
            TaskApprovalNode rejectNode = nodeRepository.selectById(record.getRejectToNodeId());
            vo.setRejectToNodeName(rejectNode != null ? rejectNode.getNodeName() : "");
        }

        return vo;
    }

    /**
     * 获取优先级文本
     */
    private String getPriorityText(Integer priority) {
        if (priority == null) {
            return "未设置";
        }
        switch (priority) {
            case 1:
                return "低";
            case 2:
                return "中";
            case 3:
                return "高";
            case 4:
                return "紧急";
            default:
                return "未知";
        }
    }
}
