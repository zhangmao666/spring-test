CREATE TABLE IF NOT EXISTS `ai_agent_run` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `run_id` VARCHAR(64) NOT NULL,
  `conversation_id` VARCHAR(100) DEFAULT NULL,
  `parent_run_id` VARCHAR(64) DEFAULT NULL,
  `agent_name` VARCHAR(120) DEFAULT NULL,
  `agent_mode` VARCHAR(32) NOT NULL DEFAULT 'DEFAULT',
  `permission_mode` VARCHAR(32) NOT NULL DEFAULT 'DEFAULT',
  `provider` VARCHAR(64) DEFAULT NULL,
  `model` VARCHAR(160) DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'RUNNING',
  `max_turns` INT NOT NULL DEFAULT 12,
  `current_turn` INT NOT NULL DEFAULT 0,
  `resume_from_run_id` VARCHAR(64) DEFAULT NULL,
  `summary` LONGTEXT DEFAULT NULL,
  `error_message` LONGTEXT DEFAULT NULL,
  `started_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `completed_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_run_id` (`run_id`),
  KEY `idx_agent_run_conversation` (`conversation_id`),
  KEY `idx_agent_run_parent` (`parent_run_id`),
  KEY `idx_agent_run_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent runtime runs';

CREATE TABLE IF NOT EXISTS `ai_agent_todo` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `run_id` VARCHAR(64) NOT NULL,
  `conversation_id` VARCHAR(100) DEFAULT NULL,
  `todo_id` VARCHAR(64) NOT NULL,
  `content` VARCHAR(1000) NOT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_todo` (`run_id`, `todo_id`),
  KEY `idx_agent_todo_run` (`run_id`),
  KEY `idx_agent_todo_status` (`run_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent externalized todos';

CREATE TABLE IF NOT EXISTS `ai_agent_event` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `event_id` VARCHAR(64) NOT NULL,
  `run_id` VARCHAR(64) NOT NULL,
  `conversation_id` VARCHAR(100) DEFAULT NULL,
  `event_type` VARCHAR(64) NOT NULL,
  `payload` LONGTEXT DEFAULT NULL,
  `turn_index` INT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_event_id` (`event_id`),
  KEY `idx_agent_event_run` (`run_id`, `id`),
  KEY `idx_agent_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent event trace';

CREATE TABLE IF NOT EXISTS `ai_agent_context_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `run_id` VARCHAR(64) NOT NULL,
  `conversation_id` VARCHAR(100) DEFAULT NULL,
  `snapshot_index` INT NOT NULL DEFAULT 0,
  `compact_level` VARCHAR(64) NOT NULL,
  `estimated_tokens` INT DEFAULT NULL,
  `char_budget` INT DEFAULT NULL,
  `summary` LONGTEXT DEFAULT NULL,
  `source_boundary` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_agent_snapshot_run` (`run_id`, `snapshot_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent compacted context snapshots';

CREATE TABLE IF NOT EXISTS `ai_agent_subagent` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `parent_run_id` VARCHAR(64) NOT NULL,
  `child_run_id` VARCHAR(64) NOT NULL,
  `conversation_id` VARCHAR(100) DEFAULT NULL,
  `agent_type` VARCHAR(64) NOT NULL,
  `task` LONGTEXT DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  `result_summary` LONGTEXT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_subagent_child` (`child_run_id`),
  KEY `idx_agent_subagent_parent` (`parent_run_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent subagent dispatch records';

CREATE TABLE IF NOT EXISTS `ai_agent_permission_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `request_id` VARCHAR(64) NOT NULL,
  `run_id` VARCHAR(64) NOT NULL,
  `conversation_id` VARCHAR(100) DEFAULT NULL,
  `tool_name` VARCHAR(160) DEFAULT NULL,
  `arguments_payload` LONGTEXT DEFAULT NULL,
  `reason` LONGTEXT DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  `decision` VARCHAR(32) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `decided_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_permission_request` (`request_id`),
  KEY `idx_agent_permission_run` (`run_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent permission requests';
