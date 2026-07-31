CREATE TABLE IF NOT EXISTS `ai_skill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `skill_key` VARCHAR(100) NOT NULL COMMENT 'Unique skill key',
  `name` VARCHAR(120) NOT NULL COMMENT 'Skill display name',
  `description` VARCHAR(1000) DEFAULT NULL COMMENT 'Skill description',
  `source_type` VARCHAR(20) NOT NULL DEFAULT 'DB' COMMENT 'LOCAL or DB',
  `origin_type` VARCHAR(20) NOT NULL DEFAULT 'MY' COMMENT 'OFFICIAL, COMMUNITY or MY',
  `skill_type` VARCHAR(20) NOT NULL DEFAULT 'PROMPT' COMMENT 'PROMPT, TOOL or MIXED',
  `category` VARCHAR(80) DEFAULT NULL COMMENT 'Skill category',
  `scenario` VARCHAR(80) DEFAULT NULL COMMENT 'Usage scenario',
  `tags` VARCHAR(500) DEFAULT NULL COMMENT 'Comma separated tags',
  `content` LONGTEXT DEFAULT NULL COMMENT 'Skill instruction content',
  `skill_dir` VARCHAR(1000) DEFAULT NULL COMMENT 'Local skill directory',
  `entry_command` VARCHAR(1000) DEFAULT NULL COMMENT 'Script entry command relative to scripts directory',
  `parameter_schema` LONGTEXT DEFAULT NULL COMMENT 'JSON schema for tool parameters',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether enabled',
  `readonly` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether readonly',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Soft delete flag',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_skill_key` (`skill_key`),
  KEY `idx_ai_skill_source` (`source_type`),
  KEY `idx_ai_skill_origin` (`origin_type`),
  KEY `idx_ai_skill_type` (`skill_type`),
  KEY `idx_ai_skill_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI skill definitions';

CREATE TABLE IF NOT EXISTS `ai_conversation_skill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `conversation_id` VARCHAR(100) NOT NULL COMMENT 'Conversation id',
  `skill_id` BIGINT NOT NULL COMMENT 'Skill id',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_skill` (`conversation_id`, `skill_id`),
  KEY `idx_conversation_skill_conversation` (`conversation_id`),
  KEY `idx_conversation_skill_skill` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Conversation mounted skills';

CREATE TABLE IF NOT EXISTS `ai_skill_execution_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `conversation_id` VARCHAR(100) DEFAULT NULL COMMENT 'Conversation id',
  `skill_id` BIGINT DEFAULT NULL COMMENT 'Skill id',
  `tool_name` VARCHAR(160) DEFAULT NULL COMMENT 'Tool name',
  `arguments_payload` LONGTEXT DEFAULT NULL COMMENT 'Tool arguments JSON',
  `status` VARCHAR(32) NOT NULL COMMENT 'SUCCESS or FAILED',
  `output_summary` LONGTEXT DEFAULT NULL COMMENT 'Output summary',
  `error_message` LONGTEXT DEFAULT NULL COMMENT 'Error message',
  `duration_ms` BIGINT DEFAULT NULL COMMENT 'Execution duration milliseconds',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  PRIMARY KEY (`id`),
  KEY `idx_skill_execution_conversation` (`conversation_id`),
  KEY `idx_skill_execution_skill` (`skill_id`),
  KEY `idx_skill_execution_time` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI skill execution logs';
