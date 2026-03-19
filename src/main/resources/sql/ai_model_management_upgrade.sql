ALTER TABLE `ai_model`
  ADD COLUMN IF NOT EXISTS `display_name` VARCHAR(100) DEFAULT NULL COMMENT 'Display name' AFTER `ai_enum`,
  ADD COLUMN IF NOT EXISTS `base_url` VARCHAR(255) DEFAULT NULL COMMENT 'OpenAI-compatible base URL' AFTER `display_name`,
  ADD COLUMN IF NOT EXISTS `api_key` VARCHAR(255) DEFAULT NULL COMMENT 'API key' AFTER `base_url`,
  ADD COLUMN IF NOT EXISTS `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether enabled' AFTER `model_name`,
  ADD COLUMN IF NOT EXISTS `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether default model' AFTER `enabled`,
  ADD COLUMN IF NOT EXISTS `supports_deep_thinking` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether deep thinking is supported' AFTER `is_default`,
  ADD COLUMN IF NOT EXISTS `supports_web_search` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether web search is supported' AFTER `supports_deep_thinking`,
  ADD COLUMN IF NOT EXISTS `remark` VARCHAR(500) DEFAULT NULL COMMENT 'Remark' AFTER `supports_web_search`;

ALTER TABLE `chat_conversations`
  ADD COLUMN IF NOT EXISTS `model_id` BIGINT DEFAULT NULL COMMENT 'Bound model id' AFTER `model`;
