-- AI对话会话表
CREATE TABLE chat_conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    conversation_id VARCHAR(64) NOT NULL UNIQUE COMMENT '会话ID（前端UUID）',
    title VARCHAR(200) NOT NULL COMMENT '会话标题',
    user_id VARCHAR(64) COMMENT '用户ID',
    provider VARCHAR(32) COMMENT 'AI服务提供商',
    model VARCHAR(64) COMMENT '模型名称',
    last_message_time DATETIME COMMENT '最后消息时间',
    message_count INT NOT NULL DEFAULT 0 COMMENT '消息总数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已删除'
) COMMENT='AI对话会话表';

-- AI对话消息表
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT '所属会话ID',
    role VARCHAR(20) NOT NULL COMMENT '消息角色：user/assistant',
    content TEXT NOT NULL COMMENT '消息内容',
    thought TEXT COMMENT '思考过程',
    thinking_time INT COMMENT '思考时长（秒）',
    used_web_search TINYINT(1) DEFAULT 0 COMMENT '是否使用了联网搜索',
    used_deep_thinking TINYINT(1) DEFAULT 0 COMMENT '是否使用了深度思考',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    message_index INT COMMENT '消息序号',
    INDEX idx_conversation_id (conversation_id)
) COMMENT='AI对话消息表';
