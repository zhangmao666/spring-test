-- ================================================
-- 修复OAuth2客户端密钥问题
-- ================================================

USE spring_boot_test;

-- 1. 查看当前客户端数据
SELECT 
    id,
    client_id,
    client_name,
    LEFT(client_secret, 20) as secret_preview,
    LENGTH(client_secret) as secret_length,
    scopes,
    status
FROM api_clients 
WHERE client_id = 'client_test_demo_001';

-- 2. 删除旧数据（如果存在）
DELETE FROM api_clients WHERE client_id = 'client_test_demo_001';

-- 3. 插入正确的测试客户端
-- client_secret: 123456 (明文)
-- BCrypt hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVajIK
INSERT INTO api_clients (
    client_id, 
    client_secret, 
    client_name, 
    scopes, 
    token_expiration, 
    status, 
    description
) VALUES (
    'client_test_demo_001',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVajIK',
    '测试客户端',
    'read,write',
    7200000,
    1,
    '用于开发测试的API客户端'
);

-- 4. 验证插入结果
SELECT 
    id,
    client_id,
    client_name,
    LEFT(client_secret, 60) as secret_hash,
    LENGTH(client_secret) as secret_length,
    scopes,
    token_expiration,
    status,
    description
FROM api_clients 
WHERE client_id = 'client_test_demo_001';

-- 5. 验证BCrypt格式
SELECT 
    CASE 
        WHEN client_secret LIKE '$2a$%' OR client_secret LIKE '$2b$%' 
        THEN 'BCrypt格式正确'
        ELSE 'BCrypt格式错误！'
    END as validation_result,
    client_id
FROM api_clients 
WHERE client_id = 'client_test_demo_001';
