-- AI chat conversations
CREATE TABLE chat_conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    conversation_id VARCHAR(64) NOT NULL UNIQUE COMMENT 'Conversation UUID',
    title VARCHAR(200) NOT NULL COMMENT 'Conversation title',
    user_id VARCHAR(64) COMMENT 'User id',
    provider VARCHAR(32) COMMENT 'AI provider',
    model VARCHAR(64) COMMENT 'Model name',
    last_message_time DATETIME COMMENT 'Last message time',
    message_count INT NOT NULL DEFAULT 0 COMMENT 'Message count',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Soft delete flag'
) COMMENT='AI chat conversations';

-- AI chat messages
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    conversation_id VARCHAR(64) NOT NULL COMMENT 'Conversation id',
    role VARCHAR(20) NOT NULL COMMENT 'user / assistant',
    content TEXT NOT NULL COMMENT 'Message content',
    thought TEXT COMMENT 'Reasoning content',
    thinking_time INT COMMENT 'Reasoning time in seconds',
    used_web_search TINYINT(1) DEFAULT 0 COMMENT 'Whether web search was requested',
    used_deep_thinking TINYINT(1) DEFAULT 0 COMMENT 'Whether deep thinking was requested',
    search_query VARCHAR(500) COMMENT 'Actual search query used',
    search_status VARCHAR(32) COMMENT 'Web search execution status',
    source_payload LONGTEXT COMMENT 'Serialized source list JSON',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    message_index INT COMMENT 'Message order',
    INDEX idx_conversation_id (conversation_id)
) COMMENT='AI chat messages';
