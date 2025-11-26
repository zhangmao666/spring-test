-- ================================================
-- 工作任务审批流系统 - 数据库初始化脚本
-- ================================================

-- 1. 角色表（新增）
CREATE TABLE IF NOT EXISTS `roles` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
  `role_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码(ROLE_SECTION_CHIEF)',
  `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称(科长)',
  `description` VARCHAR(500) COMMENT '角色描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_role_code` (`role_code`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 2. 用户角色关联表（新增）
CREATE TABLE IF NOT EXISTS `user_roles` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_role_id` (`role_id`),
  CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 3. 任务表
CREATE TABLE IF NOT EXISTS `tasks` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
  `task_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编号(自动生成)',
  `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
  `content` TEXT COMMENT '任务内容',
  `task_type` VARCHAR(50) COMMENT '任务类型',
  `priority` TINYINT DEFAULT 1 COMMENT '优先级：1-低，2-中，3-高，4-紧急',
  `status` VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT '任务状态',
  `current_node_id` BIGINT COMMENT '当前审批节点ID',
  `current_node_order` INT DEFAULT 0 COMMENT '当前节点顺序',
  `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) COMMENT '创建人姓名',
  `flow_id` BIGINT COMMENT '关联的审批流ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `submitted_at` TIMESTAMP NULL COMMENT '提交时间',
  `completed_at` TIMESTAMP NULL COMMENT '完成时间',
  INDEX `idx_task_no` (`task_no`),
  INDEX `idx_status` (`status`),
  INDEX `idx_creator` (`creator_id`),
  INDEX `idx_flow_id` (`flow_id`),
  INDEX `idx_created_at` (`created_at`),
  CONSTRAINT `fk_tasks_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 4. 审批流定义表
CREATE TABLE IF NOT EXISTS `task_approval_flows` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审批流ID',
  `flow_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '审批流编码',
  `flow_name` VARCHAR(100) NOT NULL COMMENT '审批流名称',
  `description` VARCHAR(500) COMMENT '描述',
  `task_type` VARCHAR(50) COMMENT '适用任务类型',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `version` INT DEFAULT 1 COMMENT '版本号',
  `created_by` BIGINT COMMENT '创建人ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_flow_code` (`flow_code`),
  INDEX `idx_task_type` (`task_type`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流定义表';

-- 5. 审批节点配置表
CREATE TABLE IF NOT EXISTS `task_approval_nodes` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '节点ID',
  `flow_id` BIGINT NOT NULL COMMENT '审批流ID',
  `node_code` VARCHAR(50) NOT NULL COMMENT '节点编码',
  `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称',
  `node_order` INT NOT NULL COMMENT '节点顺序（1,2,3...）',
  `approval_type` VARCHAR(20) NOT NULL COMMENT '审批类型：COUNTERSIGN-会签，OR_SIGN-或签',
  `approver_type` VARCHAR(20) NOT NULL COMMENT '审批人类型：ROLE-角色，USER-指定用户',
  `approver_ids` VARCHAR(500) COMMENT '审批人ID列表(逗号分隔)',
  `approver_roles` VARCHAR(500) COMMENT '审批角色编码列表(逗号分隔)',
  `auto_pass` TINYINT DEFAULT 0 COMMENT '是否自动通过：1-是，0-否',
  `timeout_hours` INT COMMENT '超时时间（小时）',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_flow_order` (`flow_id`, `node_order`),
  INDEX `idx_flow_id` (`flow_id`),
  INDEX `idx_node_code` (`node_code`),
  CONSTRAINT `fk_nodes_flow` FOREIGN KEY (`flow_id`) REFERENCES `task_approval_flows` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批节点配置表';

-- 6. 审批记录表
CREATE TABLE IF NOT EXISTS `task_approval_records` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `node_id` BIGINT NOT NULL COMMENT '审批节点ID',
  `node_name` VARCHAR(100) COMMENT '节点名称',
  `node_order` INT COMMENT '节点顺序',
  `approver_id` BIGINT NOT NULL COMMENT '审批人ID',
  `approver_name` VARCHAR(100) COMMENT '审批人姓名',
  `action` VARCHAR(20) NOT NULL COMMENT '操作：APPROVE-通过，REJECT-驳回，TRANSFER-转交，WITHDRAW-撤回',
  `result` VARCHAR(20) COMMENT '结果：PENDING-待审批，APPROVED-已通过，REJECTED-已驳回',
  `comment` VARCHAR(1000) COMMENT '审批意见',
  `reject_to_node_id` BIGINT COMMENT '驳回到的节点ID',
  `transfer_to_user_id` BIGINT COMMENT '转交给的用户ID',
  `transfer_to_user_name` VARCHAR(100) COMMENT '转交给的用户姓名',
  `approval_time` TIMESTAMP NULL COMMENT '审批时间',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_task_id` (`task_id`),
  INDEX `idx_approver_id` (`approver_id`),
  INDEX `idx_result` (`result`),
  INDEX `idx_approval_time` (`approval_time`),
  CONSTRAINT `fk_records_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- ================================================
-- 初始化数据
-- ================================================

-- 7. 初始化角色数据
INSERT INTO `roles` (`role_code`, `role_name`, `description`, `status`) VALUES
('ROLE_SECTION_CHIEF', '科长', '科级领导', 1),
('ROLE_DEPARTMENT_HEAD', '处长', '处级领导', 1),
('ROLE_BUREAU_CHIEF', '局长', '局级领导', 1),
('ROLE_ADMIN', '系统管理员', '系统管理员', 1),
('ROLE_USER', '普通用户', '普通用户', 1)
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- 8. 初始化审批流模板
INSERT INTO `task_approval_flows` (`flow_code`, `flow_name`, `description`, `task_type`, `status`, `version`) VALUES
('FLOW_STANDARD', '标准三级审批流', '科长→处长→局长', 'STANDARD', 1, 1),
('FLOW_URGENT', '紧急两级审批流', '处长→局长', 'URGENT', 1, 1),
('FLOW_SIMPLE', '简易单级审批流', '处长审批', 'SIMPLE', 1, 1)
ON DUPLICATE KEY UPDATE flow_name = VALUES(flow_name);

-- 9. 标准三级审批流节点配置
INSERT INTO `task_approval_nodes` (`flow_id`, `node_code`, `node_name`, `node_order`, `approval_type`, `approver_type`, `approver_roles`, `timeout_hours`)
SELECT id, 'NODE_SECTION', '科长审批', 1, 'OR_SIGN', 'ROLE', 'ROLE_SECTION_CHIEF', 24 FROM `task_approval_flows` WHERE flow_code = 'FLOW_STANDARD'
ON DUPLICATE KEY UPDATE node_name = VALUES(node_name);

INSERT INTO `task_approval_nodes` (`flow_id`, `node_code`, `node_name`, `node_order`, `approval_type`, `approver_type`, `approver_roles`, `timeout_hours`)
SELECT id, 'NODE_DEPARTMENT', '处长审批', 2, 'OR_SIGN', 'ROLE', 'ROLE_DEPARTMENT_HEAD', 48 FROM `task_approval_flows` WHERE flow_code = 'FLOW_STANDARD'
ON DUPLICATE KEY UPDATE node_name = VALUES(node_name);

INSERT INTO `task_approval_nodes` (`flow_id`, `node_code`, `node_name`, `node_order`, `approval_type`, `approver_type`, `approver_roles`, `timeout_hours`)
SELECT id, 'NODE_BUREAU', '局长审批', 3, 'COUNTERSIGN', 'ROLE', 'ROLE_BUREAU_CHIEF', 72 FROM `task_approval_flows` WHERE flow_code = 'FLOW_STANDARD'
ON DUPLICATE KEY UPDATE node_name = VALUES(node_name);

-- 10. 紧急两级审批流节点配置
INSERT INTO `task_approval_nodes` (`flow_id`, `node_code`, `node_name`, `node_order`, `approval_type`, `approver_type`, `approver_roles`, `timeout_hours`)
SELECT id, 'NODE_DEPARTMENT', '处长审批', 1, 'OR_SIGN', 'ROLE', 'ROLE_DEPARTMENT_HEAD', 12 FROM `task_approval_flows` WHERE flow_code = 'FLOW_URGENT'
ON DUPLICATE KEY UPDATE node_name = VALUES(node_name);

INSERT INTO `task_approval_nodes` (`flow_id`, `node_code`, `node_name`, `node_order`, `approval_type`, `approver_type`, `approver_roles`, `timeout_hours`)
SELECT id, 'NODE_BUREAU', '局长审批', 2, 'OR_SIGN', 'ROLE', 'ROLE_BUREAU_CHIEF', 24 FROM `task_approval_flows` WHERE flow_code = 'FLOW_URGENT'
ON DUPLICATE KEY UPDATE node_name = VALUES(node_name);

-- 11. 简易单级审批流节点配置
INSERT INTO `task_approval_nodes` (`flow_id`, `node_code`, `node_name`, `node_order`, `approval_type`, `approver_type`, `approver_roles`, `timeout_hours`)
SELECT id, 'NODE_DEPARTMENT', '处长审批', 1, 'OR_SIGN', 'ROLE', 'ROLE_DEPARTMENT_HEAD', 48 FROM `task_approval_flows` WHERE flow_code = 'FLOW_SIMPLE'
ON DUPLICATE KEY UPDATE node_name = VALUES(node_name);

-- ================================================
-- 完成
-- ================================================
