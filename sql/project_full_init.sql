-- Full database bootstrap script for spring-boot-test
-- Recommended for a clean MySQL 8.x database.

CREATE DATABASE IF NOT EXISTS `spring_boot_test`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `spring_boot_test`;

SET NAMES utf8mb4;

-- =========================================================
-- Core auth tables
-- =========================================================

CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `username` VARCHAR(50) NOT NULL COMMENT 'Login username',
  `gender` VARCHAR(16) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'Enum name: MALE/FEMALE/UNKNOWN',
  `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt password',
  `email` VARCHAR(100) NOT NULL COMMENT 'Email',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_username` (`username`),
  UNIQUE KEY `uk_users_email` (`email`),
  KEY `idx_users_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System users';

CREATE TABLE IF NOT EXISTS `roles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `role_code` VARCHAR(50) NOT NULL COMMENT 'Role code',
  `role_name` VARCHAR(100) NOT NULL COMMENT 'Role display name',
  `description` VARCHAR(500) DEFAULT NULL COMMENT 'Description',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_roles_code` (`role_code`),
  KEY `idx_roles_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System roles';

CREATE TABLE IF NOT EXISTS `user_roles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` BIGINT NOT NULL COMMENT 'User id',
  `role_id` BIGINT NOT NULL COMMENT 'Role id',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_roles_user_role` (`user_id`, `role_id`),
  KEY `idx_user_roles_role_id` (`role_id`),
  CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User role mapping';

CREATE TABLE IF NOT EXISTS `api_clients` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `client_id` VARCHAR(100) NOT NULL COMMENT 'Client id',
  `client_secret` VARCHAR(255) NOT NULL COMMENT 'BCrypt client secret',
  `api_key` VARCHAR(64) DEFAULT NULL COMMENT 'API key',
  `client_name` VARCHAR(200) NOT NULL COMMENT 'Client name',
  `scopes` VARCHAR(500) DEFAULT NULL COMMENT 'Scopes',
  `token_expiration` BIGINT DEFAULT NULL COMMENT 'Token expiration in ms',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `description` VARCHAR(1000) DEFAULT NULL COMMENT 'Description',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_clients_client_id` (`client_id`),
  UNIQUE KEY `uk_api_clients_api_key` (`api_key`),
  KEY `idx_api_clients_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API clients';

-- =========================================================
-- Device and dict tables
-- =========================================================

CREATE TABLE IF NOT EXISTS `device_type` (
  `device_type_code` VARCHAR(40) NOT NULL COMMENT 'Device type code',
  `name` VARCHAR(40) DEFAULT NULL COMMENT 'Short name',
  `status` TINYINT DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `full_name` VARCHAR(100) DEFAULT NULL COMMENT 'Full name',
  `tb_type_code` VARCHAR(20) DEFAULT NULL COMMENT 'Third-party type code',
  `icon` VARCHAR(1024) DEFAULT NULL COMMENT 'Icon URL',
  `introduction` VARCHAR(255) DEFAULT NULL COMMENT 'Introduction',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL COMMENT 'Created by user id',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT 'Updated by user id',
  `icon_pc` VARCHAR(1024) DEFAULT NULL COMMENT 'Desktop icon URL',
  `icon_colour` VARCHAR(20) DEFAULT NULL COMMENT 'Icon color',
  `device_img` VARCHAR(255) DEFAULT NULL COMMENT 'Device image URL',
  `config` JSON DEFAULT NULL COMMENT 'DeviceConfigDTO JSON',
  PRIMARY KEY (`device_type_code`),
  KEY `idx_device_type_status` (`status`),
  KEY `idx_device_type_name` (`name`),
  KEY `idx_device_type_tb_type_code` (`tb_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Device types';

CREATE TABLE IF NOT EXISTS `dict` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `dict_code` VARCHAR(50) NOT NULL COMMENT 'Dictionary code',
  `dict_name` VARCHAR(100) NOT NULL COMMENT 'Dictionary name',
  `description` VARCHAR(500) DEFAULT NULL COMMENT 'Description',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL COMMENT 'Created by user id',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT 'Updated by user id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_code` (`dict_code`),
  KEY `idx_dict_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Dictionaries';

CREATE TABLE IF NOT EXISTS `dict_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `dict_id` BIGINT NOT NULL COMMENT 'Dictionary id',
  `item_label` VARCHAR(100) NOT NULL COMMENT 'Item label',
  `item_value` VARCHAR(100) NOT NULL COMMENT 'Item value',
  `item_sort` INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `description` VARCHAR(500) DEFAULT NULL COMMENT 'Description',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL COMMENT 'Created by user id',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT 'Updated by user id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_item_dict_value` (`dict_id`, `item_value`),
  KEY `idx_dict_item_status` (`status`),
  KEY `idx_dict_item_sort` (`item_sort`),
  CONSTRAINT `fk_dict_item_dict` FOREIGN KEY (`dict_id`) REFERENCES `dict` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Dictionary items';

-- =========================================================
-- Log tables
-- =========================================================

CREATE TABLE IF NOT EXISTS `sys_login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `username` VARCHAR(50) DEFAULT '' COMMENT 'Username',
  `ip_address` VARCHAR(128) DEFAULT '' COMMENT 'IP address',
  `login_location` VARCHAR(255) DEFAULT '' COMMENT 'Login location',
  `browser` VARCHAR(50) DEFAULT '' COMMENT 'Browser',
  `os` VARCHAR(50) DEFAULT '' COMMENT 'Operating system',
  `status` TINYINT DEFAULT 0 COMMENT '0 fail, 1 success',
  `msg` VARCHAR(255) DEFAULT '' COMMENT 'Message',
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sys_login_log_username` (`username`),
  KEY `idx_sys_login_log_status` (`status`),
  KEY `idx_sys_login_log_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Login logs';

CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `title` VARCHAR(50) DEFAULT '' COMMENT 'Module title',
  `business_type` INT DEFAULT 0 COMMENT 'Business type',
  `method` VARCHAR(200) DEFAULT '' COMMENT 'Method name',
  `request_method` VARCHAR(10) DEFAULT '' COMMENT 'HTTP method',
  `operator_name` VARCHAR(50) DEFAULT '' COMMENT 'Operator',
  `request_url` VARCHAR(255) DEFAULT '' COMMENT 'Request URL',
  `ip_address` VARCHAR(128) DEFAULT '' COMMENT 'IP address',
  `request_param` TEXT DEFAULT NULL COMMENT 'Request params',
  `json_result` TEXT DEFAULT NULL COMMENT 'Response body',
  `status` TINYINT DEFAULT 0 COMMENT '0 fail, 1 success',
  `error_msg` TEXT DEFAULT NULL COMMENT 'Error message',
  `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cost_time` BIGINT DEFAULT 0 COMMENT 'Cost in ms',
  PRIMARY KEY (`id`),
  KEY `idx_sys_operation_log_business_type` (`business_type`),
  KEY `idx_sys_operation_log_status` (`status`),
  KEY `idx_sys_operation_log_time` (`operation_time`),
  KEY `idx_sys_operation_log_operator` (`operator_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Operation logs';

-- =========================================================
-- News and payment tables
-- =========================================================

CREATE TABLE IF NOT EXISTS `daily_news` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `title` VARCHAR(500) NOT NULL COMMENT 'Title',
  `content` TEXT DEFAULT NULL COMMENT 'Content',
  `source` VARCHAR(100) DEFAULT NULL COMMENT 'Source',
  `url` VARCHAR(500) DEFAULT NULL COMMENT 'URL',
  `category` VARCHAR(50) DEFAULT NULL COMMENT 'Category',
  `publish_time` DATETIME DEFAULT NULL COMMENT 'Publish time',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  PRIMARY KEY (`id`),
  KEY `idx_daily_news_create_time` (`create_time`),
  KEY `idx_daily_news_publish_time` (`publish_time`),
  KEY `idx_daily_news_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Daily news';

CREATE TABLE IF NOT EXISTS `news_subscriber` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `email` VARCHAR(100) NOT NULL COMMENT 'Email',
  `username` VARCHAR(100) DEFAULT NULL COMMENT 'Display name',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1 active, 0 inactive',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_news_subscriber_email` (`email`),
  KEY `idx_news_subscriber_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='News subscribers';

CREATE TABLE IF NOT EXISTS `transfer_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `out_trade_no` VARCHAR(64) NOT NULL COMMENT 'Merchant order number',
  `trade_no` VARCHAR(128) DEFAULT NULL COMMENT 'Third-party trade number',
  `channel` VARCHAR(20) NOT NULL COMMENT 'alipay or wechat',
  `amount` DECIMAL(10, 2) NOT NULL COMMENT 'Amount',
  `payee_account` VARCHAR(128) NOT NULL COMMENT 'Payee account',
  `payee_name` VARCHAR(64) NOT NULL COMMENT 'Payee name',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT 'Remark',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0 pending, 1 processing, 2 success, 3 failed, 4 closed',
  `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT 'Fail reason',
  `user_id` BIGINT NOT NULL COMMENT 'Initiator user id',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `finish_time` DATETIME DEFAULT NULL COMMENT 'Finish time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_order_out_trade_no` (`out_trade_no`),
  KEY `idx_transfer_order_trade_no` (`trade_no`),
  KEY `idx_transfer_order_user_id` (`user_id`),
  KEY `idx_transfer_order_channel_status` (`channel`, `status`),
  CONSTRAINT `fk_transfer_order_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Transfer orders';

-- =========================================================
-- Task approval tables
-- =========================================================

CREATE TABLE IF NOT EXISTS `task_approval_flows` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `flow_code` VARCHAR(50) NOT NULL COMMENT 'Flow code',
  `flow_name` VARCHAR(100) NOT NULL COMMENT 'Flow name',
  `description` VARCHAR(500) DEFAULT NULL COMMENT 'Description',
  `task_type` VARCHAR(50) DEFAULT NULL COMMENT 'Task type',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `version` INT NOT NULL DEFAULT 1 COMMENT 'Version',
  `created_by` BIGINT DEFAULT NULL COMMENT 'Creator user id',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_approval_flows_code` (`flow_code`),
  KEY `idx_task_approval_flows_task_type` (`task_type`),
  KEY `idx_task_approval_flows_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Approval flows';

CREATE TABLE IF NOT EXISTS `task_approval_nodes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `flow_id` BIGINT NOT NULL COMMENT 'Flow id',
  `node_code` VARCHAR(50) NOT NULL COMMENT 'Node code',
  `node_name` VARCHAR(100) NOT NULL COMMENT 'Node name',
  `node_order` INT NOT NULL COMMENT 'Node order',
  `approval_type` VARCHAR(20) NOT NULL COMMENT 'OR_SIGN/COUNTERSIGN',
  `approver_type` VARCHAR(20) NOT NULL COMMENT 'ROLE/USER',
  `approver_ids` VARCHAR(500) DEFAULT NULL COMMENT 'Comma-separated user ids',
  `approver_roles` VARCHAR(500) DEFAULT NULL COMMENT 'Comma-separated role codes',
  `auto_pass` TINYINT NOT NULL DEFAULT 0 COMMENT '1 auto pass, 0 manual',
  `timeout_hours` INT DEFAULT NULL COMMENT 'Timeout in hours',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_approval_nodes_flow_order` (`flow_id`, `node_order`),
  KEY `idx_task_approval_nodes_code` (`node_code`),
  CONSTRAINT `fk_task_approval_nodes_flow` FOREIGN KEY (`flow_id`) REFERENCES `task_approval_flows` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Approval nodes';

CREATE TABLE IF NOT EXISTS `tasks` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `task_no` VARCHAR(50) NOT NULL COMMENT 'Task number',
  `title` VARCHAR(200) NOT NULL COMMENT 'Title',
  `content` TEXT DEFAULT NULL COMMENT 'Content',
  `task_type` VARCHAR(50) DEFAULT NULL COMMENT 'Task type',
  `priority` TINYINT NOT NULL DEFAULT 1 COMMENT '1 low, 2 medium, 3 high, 4 urgent',
  `status` VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT 'Task status',
  `current_node_id` BIGINT DEFAULT NULL COMMENT 'Current node id',
  `current_node_order` INT NOT NULL DEFAULT 0 COMMENT 'Current node order',
  `creator_id` BIGINT NOT NULL COMMENT 'Creator user id',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT 'Creator name',
  `flow_id` BIGINT DEFAULT NULL COMMENT 'Flow id',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `submitted_at` DATETIME DEFAULT NULL COMMENT 'Submit time',
  `completed_at` DATETIME DEFAULT NULL COMMENT 'Completion time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tasks_task_no` (`task_no`),
  KEY `idx_tasks_status` (`status`),
  KEY `idx_tasks_creator_id` (`creator_id`),
  KEY `idx_tasks_flow_id` (`flow_id`),
  KEY `idx_tasks_created_at` (`created_at`),
  CONSTRAINT `fk_tasks_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tasks';

CREATE TABLE IF NOT EXISTS `task_approval_records` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `task_id` BIGINT NOT NULL COMMENT 'Task id',
  `node_id` BIGINT NOT NULL COMMENT 'Node id',
  `node_name` VARCHAR(100) DEFAULT NULL COMMENT 'Node name',
  `node_order` INT DEFAULT NULL COMMENT 'Node order',
  `approver_id` BIGINT NOT NULL COMMENT 'Approver id',
  `approver_name` VARCHAR(100) DEFAULT NULL COMMENT 'Approver name',
  `action` VARCHAR(20) DEFAULT NULL COMMENT 'APPROVE/REJECT/TRANSFER/WITHDRAW',
  `result` VARCHAR(20) DEFAULT NULL COMMENT 'PENDING/APPROVED/REJECTED/WITHDRAWN/TRANSFERRED',
  `comment` VARCHAR(1000) DEFAULT NULL COMMENT 'Comment',
  `reject_to_node_id` BIGINT DEFAULT NULL COMMENT 'Rejected back to node id',
  `transfer_to_user_id` BIGINT DEFAULT NULL COMMENT 'Transfer target user id',
  `transfer_to_user_name` VARCHAR(100) DEFAULT NULL COMMENT 'Transfer target username',
  `approval_time` DATETIME DEFAULT NULL COMMENT 'Approval time',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_approval_records_task_id` (`task_id`),
  KEY `idx_task_approval_records_node_id` (`node_id`),
  KEY `idx_task_approval_records_approver_id` (`approver_id`),
  KEY `idx_task_approval_records_result` (`result`),
  CONSTRAINT `fk_task_approval_records_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Approval records';

-- =========================================================
-- AI tables
-- =========================================================

CREATE TABLE IF NOT EXISTS `ai_model` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `ai_enum` VARCHAR(50) NOT NULL COMMENT 'Provider key',
  `display_name` VARCHAR(100) DEFAULT NULL COMMENT 'Display name',
  `base_url` VARCHAR(255) DEFAULT NULL COMMENT 'OpenAI-compatible base URL',
  `api_key` VARCHAR(255) DEFAULT NULL COMMENT 'API key',
  `model_name` VARCHAR(100) NOT NULL COMMENT 'Model name',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether enabled',
  `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether default model',
  `supports_deep_thinking` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether deep thinking is supported',
  `supports_web_search` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether web search is supported',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT 'Remark',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL COMMENT 'Creator user id',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT 'Updater user id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_model_enum_name` (`ai_enum`, `model_name`),
  KEY `idx_ai_model_ai_enum` (`ai_enum`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI model registry';

ALTER TABLE `ai_model`
  ADD COLUMN IF NOT EXISTS `display_name` VARCHAR(100) DEFAULT NULL COMMENT 'Display name' AFTER `ai_enum`,
  ADD COLUMN IF NOT EXISTS `base_url` VARCHAR(255) DEFAULT NULL COMMENT 'OpenAI-compatible base URL' AFTER `display_name`,
  ADD COLUMN IF NOT EXISTS `api_key` VARCHAR(255) DEFAULT NULL COMMENT 'API key' AFTER `base_url`,
  ADD COLUMN IF NOT EXISTS `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether enabled' AFTER `model_name`,
  ADD COLUMN IF NOT EXISTS `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether default model' AFTER `enabled`,
  ADD COLUMN IF NOT EXISTS `supports_deep_thinking` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether deep thinking is supported' AFTER `is_default`,
  ADD COLUMN IF NOT EXISTS `supports_web_search` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether web search is supported' AFTER `supports_deep_thinking`,
  ADD COLUMN IF NOT EXISTS `remark` VARCHAR(500) DEFAULT NULL COMMENT 'Remark' AFTER `supports_web_search`;

CREATE TABLE IF NOT EXISTS `chat_conversations` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `conversation_id` VARCHAR(64) NOT NULL COMMENT 'Conversation id',
  `title` VARCHAR(200) NOT NULL COMMENT 'Conversation title',
  `user_id` VARCHAR(64) DEFAULT NULL COMMENT 'User id as string',
  `provider` VARCHAR(32) DEFAULT NULL COMMENT 'AI provider',
  `model` VARCHAR(64) DEFAULT NULL COMMENT 'Model name',
  `model_id` BIGINT DEFAULT NULL COMMENT 'Bound model id',
  `last_message_time` DATETIME DEFAULT NULL COMMENT 'Last message time',
  `message_count` INT NOT NULL DEFAULT 0 COMMENT 'Message count',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0 normal, 1 deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_conversations_conversation_id` (`conversation_id`),
  KEY `idx_chat_conversations_user_deleted` (`user_id`, `deleted`),
  KEY `idx_chat_conversations_last_message_time` (`last_message_time`),
  KEY `idx_chat_conversations_model_id` (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Chat conversations';

ALTER TABLE `chat_conversations`
  ADD COLUMN IF NOT EXISTS `model_id` BIGINT DEFAULT NULL COMMENT 'Bound model id' AFTER `model`;

CREATE TABLE IF NOT EXISTS `chat_messages` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `conversation_id` VARCHAR(64) NOT NULL COMMENT 'Conversation id',
  `role` VARCHAR(20) NOT NULL COMMENT 'user or assistant',
  `content` TEXT NOT NULL COMMENT 'Message content',
  `thought` TEXT DEFAULT NULL COMMENT 'Thought content',
  `thinking_time` INT DEFAULT NULL COMMENT 'Thinking time in seconds',
  `used_web_search` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether web search was used',
  `used_deep_thinking` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether deep thinking was used',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `message_index` INT DEFAULT NULL COMMENT 'Message order in conversation',
  PRIMARY KEY (`id`),
  KEY `idx_chat_messages_conversation_id` (`conversation_id`),
  KEY `idx_chat_messages_conversation_index` (`conversation_id`, `message_index`),
  CONSTRAINT `fk_chat_messages_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversations` (`conversation_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Chat messages';

-- =========================================================
-- Seed data
-- =========================================================

-- BCrypt hash for plain text 123456
SET @bcrypt_123456 = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVajIK';

INSERT INTO `users` (`username`, `gender`, `password`, `email`, `status`)
VALUES
  ('admin', 'UNKNOWN', @bcrypt_123456, 'admin@example.com', 1),
  ('section_chief', 'MALE', @bcrypt_123456, 'section_chief@example.com', 1),
  ('department_head', 'MALE', @bcrypt_123456, 'department_head@example.com', 1),
  ('bureau_chief', 'MALE', @bcrypt_123456, 'bureau_chief@example.com', 1),
  ('test_user', 'FEMALE', @bcrypt_123456, 'test_user@example.com', 1)
ON DUPLICATE KEY UPDATE
  `gender` = VALUES(`gender`),
  `password` = VALUES(`password`),
  `email` = VALUES(`email`),
  `status` = VALUES(`status`);

INSERT INTO `roles` (`role_code`, `role_name`, `description`, `status`)
VALUES
  ('ROLE_ADMIN', 'Administrator', 'System administrator', 1),
  ('ROLE_USER', 'User', 'Default system user', 1),
  ('ROLE_SECTION_CHIEF', 'Section Chief', 'Task approval role', 1),
  ('ROLE_DEPARTMENT_HEAD', 'Department Head', 'Task approval role', 1),
  ('ROLE_BUREAU_CHIEF', 'Bureau Chief', 'Task approval role', 1)
ON DUPLICATE KEY UPDATE
  `role_name` = VALUES(`role_name`),
  `description` = VALUES(`description`),
  `status` = VALUES(`status`);

INSERT INTO `user_roles` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `users` u
JOIN `roles` r ON r.role_code = 'ROLE_ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM `user_roles` ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO `user_roles` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `users` u
JOIN `roles` r ON r.role_code = 'ROLE_USER'
WHERE u.username IN ('admin', 'section_chief', 'department_head', 'bureau_chief', 'test_user')
  AND NOT EXISTS (
    SELECT 1 FROM `user_roles` ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO `user_roles` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `users` u
JOIN `roles` r ON r.role_code = 'ROLE_SECTION_CHIEF'
WHERE u.username = 'section_chief'
  AND NOT EXISTS (
    SELECT 1 FROM `user_roles` ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO `user_roles` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `users` u
JOIN `roles` r ON r.role_code = 'ROLE_DEPARTMENT_HEAD'
WHERE u.username = 'department_head'
  AND NOT EXISTS (
    SELECT 1 FROM `user_roles` ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO `user_roles` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `users` u
JOIN `roles` r ON r.role_code = 'ROLE_BUREAU_CHIEF'
WHERE u.username = 'bureau_chief'
  AND NOT EXISTS (
    SELECT 1 FROM `user_roles` ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO `api_clients`
  (`client_id`, `client_secret`, `api_key`, `client_name`, `scopes`, `token_expiration`, `status`, `description`)
VALUES
  ('client_test_demo_001', @bcrypt_123456, 'apk_client_test_demo_001', 'Default Test Client', 'read,write', 7200000, 1, 'Default API client for local development')
ON DUPLICATE KEY UPDATE
  `client_secret` = VALUES(`client_secret`),
  `api_key` = VALUES(`api_key`),
  `client_name` = VALUES(`client_name`),
  `scopes` = VALUES(`scopes`),
  `token_expiration` = VALUES(`token_expiration`),
  `status` = VALUES(`status`),
  `description` = VALUES(`description`);

INSERT INTO `device_type`
  (`device_type_code`, `name`, `status`, `full_name`, `tb_type_code`, `icon`, `introduction`, `create_by`, `update_by`, `icon_pc`, `icon_colour`, `device_img`, `config`)
VALUES
  ('SENSOR_001', 'Temperature Sensor', 1, 'High Precision Temperature Sensor', 'TEMP', '/icons/temperature.png', 'Used for temperature monitoring', 1, 1, '/icons/pc/temperature.png', '#FF5722', '/images/temp_sensor.jpg', JSON_OBJECT('imgId', 'img-temp-001', 'videoId', 'video-temp-001')),
  ('SENSOR_002', 'Humidity Sensor', 1, 'Digital Humidity Sensor', 'HUMI', '/icons/humidity.png', 'Used for humidity monitoring', 1, 1, '/icons/pc/humidity.png', '#2196F3', '/images/humi_sensor.jpg', JSON_OBJECT('imgId', 'img-humi-001', 'videoId', 'video-humi-001')),
  ('CAMERA_001', 'Network Camera', 1, '1080P Monitoring Camera', 'CAM', '/icons/camera.png', 'Used for site monitoring', 1, 1, '/icons/pc/camera.png', '#4CAF50', '/images/camera.jpg', JSON_OBJECT('imgId', 'img-cam-001', 'videoId', 'video-cam-001'))
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `status` = VALUES(`status`),
  `full_name` = VALUES(`full_name`),
  `tb_type_code` = VALUES(`tb_type_code`),
  `icon` = VALUES(`icon`),
  `introduction` = VALUES(`introduction`),
  `update_by` = VALUES(`update_by`),
  `icon_pc` = VALUES(`icon_pc`),
  `icon_colour` = VALUES(`icon_colour`),
  `device_img` = VALUES(`device_img`),
  `config` = VALUES(`config`);

INSERT INTO `dict` (`dict_code`, `dict_name`, `description`, `status`, `create_by`, `update_by`)
VALUES
  ('user_status', 'User Status', 'User account status', 1, 1, 1),
  ('device_status', 'Device Status', 'Device runtime status', 1, 1, 1),
  ('gender', 'Gender', 'User gender enum values', 1, 1, 1),
  ('task_status', 'Task Status', 'Task workflow status', 1, 1, 1)
ON DUPLICATE KEY UPDATE
  `dict_name` = VALUES(`dict_name`),
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `update_by` = VALUES(`update_by`);

INSERT INTO `dict_item` (`dict_id`, `item_label`, `item_value`, `item_sort`, `status`, `description`, `create_by`, `update_by`)
SELECT d.id, 'Enabled', '1', 1, 1, 'Enabled user', 1, 1
FROM `dict` d
WHERE d.dict_code = 'user_status'
  AND NOT EXISTS (
    SELECT 1 FROM `dict_item` di WHERE di.dict_id = d.id AND di.item_value = '1'
  );

INSERT INTO `dict_item` (`dict_id`, `item_label`, `item_value`, `item_sort`, `status`, `description`, `create_by`, `update_by`)
SELECT d.id, 'Disabled', '0', 2, 1, 'Disabled user', 1, 1
FROM `dict` d
WHERE d.dict_code = 'user_status'
  AND NOT EXISTS (
    SELECT 1 FROM `dict_item` di WHERE di.dict_id = d.id AND di.item_value = '0'
  );

INSERT INTO `dict_item` (`dict_id`, `item_label`, `item_value`, `item_sort`, `status`, `description`, `create_by`, `update_by`)
SELECT d.id, 'Online', 'online', 1, 1, 'Device online', 1, 1
FROM `dict` d
WHERE d.dict_code = 'device_status'
  AND NOT EXISTS (
    SELECT 1 FROM `dict_item` di WHERE di.dict_id = d.id AND di.item_value = 'online'
  );

INSERT INTO `dict_item` (`dict_id`, `item_label`, `item_value`, `item_sort`, `status`, `description`, `create_by`, `update_by`)
SELECT d.id, 'Offline', 'offline', 2, 1, 'Device offline', 1, 1
FROM `dict` d
WHERE d.dict_code = 'device_status'
  AND NOT EXISTS (
    SELECT 1 FROM `dict_item` di WHERE di.dict_id = d.id AND di.item_value = 'offline'
  );

INSERT INTO `dict_item` (`dict_id`, `item_label`, `item_value`, `item_sort`, `status`, `description`, `create_by`, `update_by`)
SELECT d.id, 'MALE', 'MALE', 1, 1, 'Male enum', 1, 1
FROM `dict` d
WHERE d.dict_code = 'gender'
  AND NOT EXISTS (
    SELECT 1 FROM `dict_item` di WHERE di.dict_id = d.id AND di.item_value = 'MALE'
  );

INSERT INTO `dict_item` (`dict_id`, `item_label`, `item_value`, `item_sort`, `status`, `description`, `create_by`, `update_by`)
SELECT d.id, 'FEMALE', 'FEMALE', 2, 1, 'Female enum', 1, 1
FROM `dict` d
WHERE d.dict_code = 'gender'
  AND NOT EXISTS (
    SELECT 1 FROM `dict_item` di WHERE di.dict_id = d.id AND di.item_value = 'FEMALE'
  );

INSERT INTO `dict_item` (`dict_id`, `item_label`, `item_value`, `item_sort`, `status`, `description`, `create_by`, `update_by`)
SELECT d.id, 'UNKNOWN', 'UNKNOWN', 3, 1, 'Unknown enum', 1, 1
FROM `dict` d
WHERE d.dict_code = 'gender'
  AND NOT EXISTS (
    SELECT 1 FROM `dict_item` di WHERE di.dict_id = d.id AND di.item_value = 'UNKNOWN'
  );

INSERT INTO `dict_item` (`dict_id`, `item_label`, `item_value`, `item_sort`, `status`, `description`, `create_by`, `update_by`)
SELECT d.id, 'DRAFT', 'DRAFT', 1, 1, 'Draft task', 1, 1
FROM `dict` d
WHERE d.dict_code = 'task_status'
  AND NOT EXISTS (
    SELECT 1 FROM `dict_item` di WHERE di.dict_id = d.id AND di.item_value = 'DRAFT'
  );

INSERT INTO `dict_item` (`dict_id`, `item_label`, `item_value`, `item_sort`, `status`, `description`, `create_by`, `update_by`)
SELECT d.id, 'PENDING', 'PENDING', 2, 1, 'Pending approval', 1, 1
FROM `dict` d
WHERE d.dict_code = 'task_status'
  AND NOT EXISTS (
    SELECT 1 FROM `dict_item` di WHERE di.dict_id = d.id AND di.item_value = 'PENDING'
  );

INSERT INTO `dict_item` (`dict_id`, `item_label`, `item_value`, `item_sort`, `status`, `description`, `create_by`, `update_by`)
SELECT d.id, 'IN_PROGRESS', 'IN_PROGRESS', 3, 1, 'In progress', 1, 1
FROM `dict` d
WHERE d.dict_code = 'task_status'
  AND NOT EXISTS (
    SELECT 1 FROM `dict_item` di WHERE di.dict_id = d.id AND di.item_value = 'IN_PROGRESS'
  );

INSERT INTO `dict_item` (`dict_id`, `item_label`, `item_value`, `item_sort`, `status`, `description`, `create_by`, `update_by`)
SELECT d.id, 'APPROVED', 'APPROVED', 4, 1, 'Approved task', 1, 1
FROM `dict` d
WHERE d.dict_code = 'task_status'
  AND NOT EXISTS (
    SELECT 1 FROM `dict_item` di WHERE di.dict_id = d.id AND di.item_value = 'APPROVED'
  );

INSERT INTO `dict_item` (`dict_id`, `item_label`, `item_value`, `item_sort`, `status`, `description`, `create_by`, `update_by`)
SELECT d.id, 'REJECTED', 'REJECTED', 5, 1, 'Rejected task', 1, 1
FROM `dict` d
WHERE d.dict_code = 'task_status'
  AND NOT EXISTS (
    SELECT 1 FROM `dict_item` di WHERE di.dict_id = d.id AND di.item_value = 'REJECTED'
  );

INSERT INTO `dict_item` (`dict_id`, `item_label`, `item_value`, `item_sort`, `status`, `description`, `create_by`, `update_by`)
SELECT d.id, 'WITHDRAWN', 'WITHDRAWN', 6, 1, 'Withdrawn task', 1, 1
FROM `dict` d
WHERE d.dict_code = 'task_status'
  AND NOT EXISTS (
    SELECT 1 FROM `dict_item` di WHERE di.dict_id = d.id AND di.item_value = 'WITHDRAWN'
  );

INSERT INTO `task_approval_flows` (`flow_code`, `flow_name`, `description`, `task_type`, `status`, `version`, `created_by`)
VALUES
  ('FLOW_STANDARD', 'Standard 3-Level Approval', 'Section chief -> department head -> bureau chief', 'STANDARD', 1, 1, 1),
  ('FLOW_URGENT', 'Urgent 2-Level Approval', 'Department head -> bureau chief', 'URGENT', 1, 1, 1),
  ('FLOW_SIMPLE', 'Simple 1-Level Approval', 'Department head only', 'SIMPLE', 1, 1, 1)
ON DUPLICATE KEY UPDATE
  `flow_name` = VALUES(`flow_name`),
  `description` = VALUES(`description`),
  `task_type` = VALUES(`task_type`),
  `status` = VALUES(`status`),
  `version` = VALUES(`version`),
  `created_by` = VALUES(`created_by`);

INSERT INTO `task_approval_nodes`
  (`flow_id`, `node_code`, `node_name`, `node_order`, `approval_type`, `approver_type`, `approver_roles`, `timeout_hours`)
SELECT f.id, 'NODE_SECTION', 'Section Chief Approval', 1, 'OR_SIGN', 'ROLE', 'ROLE_SECTION_CHIEF', 24
FROM `task_approval_flows` f
WHERE f.flow_code = 'FLOW_STANDARD'
  AND NOT EXISTS (
    SELECT 1 FROM `task_approval_nodes` n WHERE n.flow_id = f.id AND n.node_order = 1
  );

INSERT INTO `task_approval_nodes`
  (`flow_id`, `node_code`, `node_name`, `node_order`, `approval_type`, `approver_type`, `approver_roles`, `timeout_hours`)
SELECT f.id, 'NODE_DEPARTMENT', 'Department Head Approval', 2, 'OR_SIGN', 'ROLE', 'ROLE_DEPARTMENT_HEAD', 48
FROM `task_approval_flows` f
WHERE f.flow_code = 'FLOW_STANDARD'
  AND NOT EXISTS (
    SELECT 1 FROM `task_approval_nodes` n WHERE n.flow_id = f.id AND n.node_order = 2
  );

INSERT INTO `task_approval_nodes`
  (`flow_id`, `node_code`, `node_name`, `node_order`, `approval_type`, `approver_type`, `approver_roles`, `timeout_hours`)
SELECT f.id, 'NODE_BUREAU', 'Bureau Chief Approval', 3, 'COUNTERSIGN', 'ROLE', 'ROLE_BUREAU_CHIEF', 72
FROM `task_approval_flows` f
WHERE f.flow_code = 'FLOW_STANDARD'
  AND NOT EXISTS (
    SELECT 1 FROM `task_approval_nodes` n WHERE n.flow_id = f.id AND n.node_order = 3
  );

INSERT INTO `task_approval_nodes`
  (`flow_id`, `node_code`, `node_name`, `node_order`, `approval_type`, `approver_type`, `approver_roles`, `timeout_hours`)
SELECT f.id, 'NODE_DEPARTMENT', 'Department Head Approval', 1, 'OR_SIGN', 'ROLE', 'ROLE_DEPARTMENT_HEAD', 12
FROM `task_approval_flows` f
WHERE f.flow_code = 'FLOW_URGENT'
  AND NOT EXISTS (
    SELECT 1 FROM `task_approval_nodes` n WHERE n.flow_id = f.id AND n.node_order = 1
  );

INSERT INTO `task_approval_nodes`
  (`flow_id`, `node_code`, `node_name`, `node_order`, `approval_type`, `approver_type`, `approver_roles`, `timeout_hours`)
SELECT f.id, 'NODE_BUREAU', 'Bureau Chief Approval', 2, 'OR_SIGN', 'ROLE', 'ROLE_BUREAU_CHIEF', 24
FROM `task_approval_flows` f
WHERE f.flow_code = 'FLOW_URGENT'
  AND NOT EXISTS (
    SELECT 1 FROM `task_approval_nodes` n WHERE n.flow_id = f.id AND n.node_order = 2
  );

INSERT INTO `task_approval_nodes`
  (`flow_id`, `node_code`, `node_name`, `node_order`, `approval_type`, `approver_type`, `approver_roles`, `timeout_hours`)
SELECT f.id, 'NODE_DEPARTMENT', 'Department Head Approval', 1, 'OR_SIGN', 'ROLE', 'ROLE_DEPARTMENT_HEAD', 48
FROM `task_approval_flows` f
WHERE f.flow_code = 'FLOW_SIMPLE'
  AND NOT EXISTS (
    SELECT 1 FROM `task_approval_nodes` n WHERE n.flow_id = f.id AND n.node_order = 1
  );

INSERT INTO `ai_model`
  (`ai_enum`, `display_name`, `base_url`, `api_key`, `model_name`, `enabled`, `is_default`,
   `supports_deep_thinking`, `supports_web_search`, `remark`, `create_by`, `update_by`)
VALUES
  ('openai', 'DeepSeek V3.2', 'https://yunwu.ai', '${OPENAI_API_KEY}', 'deepseek-v3.2', 1, 1, 1, 0, 'Default managed model', 1, 1),
  ('openai', 'Gemini Flash', 'https://yunwu.ai', '${OPENAI_API_KEY}', 'gemini-2.5-flash-all', 1, 0, 1, 0, 'Alternative managed model', 1, 1)
ON DUPLICATE KEY UPDATE
  `display_name` = VALUES(`display_name`),
  `base_url` = VALUES(`base_url`),
  `api_key` = VALUES(`api_key`),
  `enabled` = VALUES(`enabled`),
  `is_default` = VALUES(`is_default`),
  `supports_deep_thinking` = VALUES(`supports_deep_thinking`),
  `supports_web_search` = VALUES(`supports_web_search`),
  `remark` = VALUES(`remark`),
  `update_by` = VALUES(`update_by`);

INSERT INTO `daily_news` (`title`, `content`, `source`, `url`, `category`, `publish_time`, `create_time`)
SELECT 'Sample daily news item', 'This is a bootstrap record used for the daily news module.', 'Bootstrap', 'https://example.com/daily-news-1', 'Technology', CONCAT(CURDATE(), ' 08:00:00'), NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1
  FROM `daily_news` dn
  WHERE dn.title = 'Sample daily news item'
    AND DATE(dn.publish_time) = CURDATE()
);

INSERT INTO `news_subscriber` (`email`, `username`, `is_active`)
VALUES
  ('subscriber@example.com', 'news_reader', 1)
ON DUPLICATE KEY UPDATE
  `username` = VALUES(`username`),
  `is_active` = VALUES(`is_active`);

-- Bootstrap complete.
SELECT 'spring_boot_test bootstrap completed' AS `message`;
