package com.example.springboottest.service;

import com.example.springboottest.DTO.*;
import com.example.springboottest.entity.*;
import com.example.springboottest.enums.ApproverType;
import com.example.springboottest.exception.BusinessException;
import com.example.springboottest.exception.DuplicateResourceException;
import com.example.springboottest.exception.ResourceNotFoundException;
import com.example.springboottest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批流配置管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskApprovalFlowService {

    private final TaskApprovalFlowRepository flowRepository;
    private final TaskApprovalNodeRepository nodeRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    /**
     * 创建审批流
     */
    @Transactional
    public TaskApprovalFlowVO createFlow(CreateFlowRequest request, Long userId) {
        log.info("创建审批流，用户ID: {}, 流程编码: {}", userId, request.getFlowCode());

        // 1. 校验流程编码是否重复
        TaskApprovalFlow existingFlow = flowRepository.selectByFlowCode(request.getFlowCode());
        if (existingFlow != null) {
            throw new DuplicateResourceException("审批流编码已存在: " + request.getFlowCode());
        }

        // 2. 校验节点配置
        validateNodes(request.getNodes());

        // 3. 创建审批流定义
        TaskApprovalFlow flow = new TaskApprovalFlow();
        flow.setFlowCode(request.getFlowCode());
        flow.setFlowName(request.getFlowName());
        flow.setDescription(request.getDescription());
        flow.setTaskType(request.getTaskType());
        flow.setStatus(1); // 启用
        flow.setVersion(1);
        flow.setCreatedBy(userId);
        flowRepository.insert(flow);

        // 4. 创建审批节点配置
        for (FlowNodeRequest nodeRequest : request.getNodes()) {
            TaskApprovalNode node = new TaskApprovalNode();
            node.setFlowId(flow.getId());
            node.setNodeCode(nodeRequest.getNodeCode());
            node.setNodeName(nodeRequest.getNodeName());
            node.setNodeOrder(nodeRequest.getNodeOrder());
            node.setApprovalType(nodeRequest.getApprovalType().name());
            node.setApproverType(nodeRequest.getApproverType());

            // 设置审批人
            if (ApproverType.USER.name().equals(nodeRequest.getApproverType())) {
                if (nodeRequest.getApproverIds() != null && !nodeRequest.getApproverIds().isEmpty()) {
                    String approverIds = nodeRequest.getApproverIds().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(","));
                    node.setApproverIds(approverIds);
                }
            } else if (ApproverType.ROLE.name().equals(nodeRequest.getApproverType())) {
                if (nodeRequest.getApproverRoles() != null && !nodeRequest.getApproverRoles().isEmpty()) {
                    String approverRoles = String.join(",", nodeRequest.getApproverRoles());
                    node.setApproverRoles(approverRoles);
                }
            }

            node.setTimeoutHours(nodeRequest.getTimeoutHours());
            node.setAutoPass(0);
            nodeRepository.insert(node);
        }

        log.info("审批流创建成功，流程编码: {}", flow.getFlowCode());

        return convertToVO(flow);
    }

    /**
     * 更新审批流（版本控制）
     */
    @Transactional
    public TaskApprovalFlowVO updateFlow(Long flowId, UpdateFlowRequest request, Long userId) {
        log.info("更新审批流，流程ID: {}, 用户ID: {}", flowId, userId);

        // 1. 查询原审批流
        TaskApprovalFlow oldFlow = flowRepository.selectById(flowId);
        if (oldFlow == null) {
            throw new ResourceNotFoundException("审批流不存在");
        }

        // 2. 校验节点配置
        validateNodes(request.getNodes());

        // 3. 禁用旧版本
        oldFlow.setStatus(0);
        flowRepository.updateById(oldFlow);

        // 4. 创建新版本
        TaskApprovalFlow newFlow = new TaskApprovalFlow();
        newFlow.setFlowCode(oldFlow.getFlowCode());
        newFlow.setFlowName(request.getFlowName());
        newFlow.setDescription(request.getDescription());
        newFlow.setTaskType(request.getTaskType());
        newFlow.setStatus(1);
        newFlow.setVersion(oldFlow.getVersion() + 1);
        newFlow.setCreatedBy(userId);
        flowRepository.insert(newFlow);

        // 5. 创建新版本的审批节点配置
        for (FlowNodeRequest nodeRequest : request.getNodes()) {
            TaskApprovalNode node = new TaskApprovalNode();
            node.setFlowId(newFlow.getId());
            node.setNodeCode(nodeRequest.getNodeCode());
            node.setNodeName(nodeRequest.getNodeName());
            node.setNodeOrder(nodeRequest.getNodeOrder());
            node.setApprovalType(nodeRequest.getApprovalType().name());
            node.setApproverType(nodeRequest.getApproverType());

            if (ApproverType.USER.name().equals(nodeRequest.getApproverType())) {
                if (nodeRequest.getApproverIds() != null && !nodeRequest.getApproverIds().isEmpty()) {
                    String approverIds = nodeRequest.getApproverIds().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(","));
                    node.setApproverIds(approverIds);
                }
            } else if (ApproverType.ROLE.name().equals(nodeRequest.getApproverType())) {
                if (nodeRequest.getApproverRoles() != null && !nodeRequest.getApproverRoles().isEmpty()) {
                    String approverRoles = String.join(",", nodeRequest.getApproverRoles());
                    node.setApproverRoles(approverRoles);
                }
            }

            node.setTimeoutHours(nodeRequest.getTimeoutHours());
            node.setAutoPass(0);
            nodeRepository.insert(node);
        }

        log.info("审批流更新成功，流程编码: {}, 新版本: {}", newFlow.getFlowCode(), newFlow.getVersion());

        return convertToVO(newFlow);
    }

    /**
     * 获取审批流详情
     */
    @Transactional(readOnly = true)
    public TaskApprovalFlowDetailVO getFlowDetail(Long flowId) {
        log.info("获取审批流详情，流程ID: {}", flowId);

        TaskApprovalFlow flow = flowRepository.selectById(flowId);
        if (flow == null) {
            throw new ResourceNotFoundException("审批流不存在");
        }

        List<TaskApprovalNode> nodes = nodeRepository.selectByFlowId(flowId);

        TaskApprovalFlowDetailVO detailVO = new TaskApprovalFlowDetailVO();
        detailVO.setId(flow.getId());
        detailVO.setFlowCode(flow.getFlowCode());
        detailVO.setFlowName(flow.getFlowName());
        detailVO.setDescription(flow.getDescription());
        detailVO.setTaskType(flow.getTaskType());
        detailVO.setStatus(flow.getStatus());
        detailVO.setVersion(flow.getVersion());
        detailVO.setCreatedAt(flow.getCreatedAt());

        List<TaskApprovalNodeVO> nodeVOs = nodes.stream()
                .map(this::convertNodeToVO)
                .collect(Collectors.toList());
        detailVO.setNodes(nodeVOs);

        return detailVO;
    }

    /**
     * 获取审批流列表
     */
    @Transactional(readOnly = true)
    public List<TaskApprovalFlowVO> listFlows(String taskType) {
        log.info("获取审批流列表，任务类型: {}", taskType);

        List<TaskApprovalFlow> flows;
        if (taskType != null && !taskType.isEmpty()) {
            flows = flowRepository.selectByTaskType(taskType);
        } else {
            flows = flowRepository.selectAllActive();
        }

        return flows.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 校验节点配置
     */
    private void validateNodes(List<FlowNodeRequest> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            throw new BusinessException("审批节点不能为空");
        }

        // 校验节点顺序连续性
        nodes.sort((a, b) -> a.getNodeOrder().compareTo(b.getNodeOrder()));
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getNodeOrder() != i + 1) {
                throw new BusinessException("节点顺序必须从1开始连续递增");
            }
        }

        // 校验每个节点的配置
        for (FlowNodeRequest node : nodes) {
            // 校验审批人类型和审批人配置
            if (ApproverType.USER.name().equals(node.getApproverType())) {
                if (node.getApproverIds() == null || node.getApproverIds().isEmpty()) {
                    throw new BusinessException("节点 " + node.getNodeName() + " 未配置审批人");
                }
                // 校验用户是否存在
                for (Long userId : node.getApproverIds()) {
                    User user = userRepository.selectById(userId);
                    if (user == null) {
                        throw new BusinessException("审批人不存在，用户ID: " + userId);
                    }
                }
            } else if (ApproverType.ROLE.name().equals(node.getApproverType())) {
                if (node.getApproverRoles() == null || node.getApproverRoles().isEmpty()) {
                    throw new BusinessException("节点 " + node.getNodeName() + " 未配置审批角色");
                }
                // 校验角色是否存在
                for (String roleCode : node.getApproverRoles()) {
                    Role role = roleRepository.selectByRoleCode(roleCode);
                    if (role == null) {
                        throw new BusinessException("审批角色不存在，角色编码: " + roleCode);
                    }
                }
            } else {
                throw new BusinessException("不支持的审批人类型: " + node.getApproverType());
            }
        }
    }

    /**
     * 转换审批流为VO
     */
    private TaskApprovalFlowVO convertToVO(TaskApprovalFlow flow) {
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
    private TaskApprovalNodeVO convertNodeToVO(TaskApprovalNode node) {
        TaskApprovalNodeVO vo = new TaskApprovalNodeVO();
        vo.setId(node.getId());
        vo.setNodeCode(node.getNodeCode());
        vo.setNodeName(node.getNodeName());
        vo.setNodeOrder(node.getNodeOrder());
        vo.setApprovalType(node.getApprovalType());
        vo.setApproverType(node.getApproverType());
        vo.setTimeoutHours(node.getTimeoutHours());

        // 设置审批人名称
        List<String> approverNames = new ArrayList<>();
        if (ApproverType.USER.name().equals(node.getApproverType())) {
            if (node.getApproverIds() != null && !node.getApproverIds().isEmpty()) {
                String[] ids = node.getApproverIds().split(",");
                for (String id : ids) {
                    try {
                        User user = userRepository.selectById(Long.parseLong(id.trim()));
                        if (user != null) {
                            approverNames.add(user.getUsername());
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析审批人ID失败: {}", id);
                    }
                }
            }
        }
        vo.setApproverNames(approverNames);

        // 设置审批角色名称
        List<String> approverRoleNames = new ArrayList<>();
        if (ApproverType.ROLE.name().equals(node.getApproverType())) {
            if (node.getApproverRoles() != null && !node.getApproverRoles().isEmpty()) {
                String[] roles = node.getApproverRoles().split(",");
                for (String roleCode : roles) {
                    Role role = roleRepository.selectByRoleCode(roleCode.trim());
                    if (role != null) {
                        approverRoleNames.add(role.getRoleName());
                    }
                }
            }
        }
        vo.setApproverRoleNames(approverRoleNames);

        return vo;
    }
}
