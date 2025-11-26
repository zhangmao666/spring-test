-- ================================================
-- 添加API Key字段到api_clients表
-- ================================================

USE spring_boot_test;

-- 1. 检查表结构
DESC api_clients;

-- 2. 添加api_key字段
ALTER TABLE api_clients 
ADD COLUMN api_key VARCHAR(64) UNIQUE COMMENT 'API Key（用于API Key认证）' 
AFTER client_secret;

-- 3. 添加索引（提升查询性能）
CREATE INDEX idx_api_key ON api_clients(api_key);

-- 4. 查看更新后的表结构
DESC api_clients;

-- 5. 为现有测试客户端生成API Key
UPDATE api_clients 
SET api_key = CONCAT('apk_', REPLACE(UUID(), '-', ''))
WHERE client_id = 'client_test_demo_001' AND api_key IS NULL;

-- 6. 验证数据
SELECT 
    id,
    client_id,
    client_name,
    LEFT(api_key, 20) as api_key_preview,
    scopes,
    status
FROM api_clients;

-- 完成！
SELECT '数据库迁移完成！API Key字段已添加。' as message;
