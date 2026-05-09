ALTER TABLE `chat_messages`
  ADD COLUMN IF NOT EXISTS `search_query` VARCHAR(500) DEFAULT NULL COMMENT 'Actual search query used' AFTER `used_deep_thinking`,
  ADD COLUMN IF NOT EXISTS `search_status` VARCHAR(32) DEFAULT NULL COMMENT 'Web search execution status' AFTER `search_query`,
  ADD COLUMN IF NOT EXISTS `source_payload` LONGTEXT DEFAULT NULL COMMENT 'Serialized search sources payload' AFTER `search_status`;
