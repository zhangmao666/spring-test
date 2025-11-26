-- ================================================
-- 更新OAuth2客户端密码为正确的BCrypt hash
-- ================================================

USE spring_boot_test;

-- 查看当前数据
SELECT 
    id,
    client_id,
    LEFT(client_secret, 60) as old_secret,
    status
FROM api_clients 
WHERE client_id = 'client_test_demo_001';

-- 更新为正确的密码hash
-- 明文密码: 123456
-- 新的BCrypt hash: $2a$10$usvm.w8jhEX/pPkiXZSS1.ZWybOdvPfRNzmG4DPhp8mtgEk6r5P3m
UPDATE api_clients 
SET client_secret = '$2a$10$usvm.w8jhEX/pPkiXZSS1.ZWybOdvPfRNzmG4DPhp8mtgEk6r5P3m'
WHERE client_id = 'client_test_demo_001';

-- 验证更新后的数据
SELECT 
    id,
    client_id,
    client_name,
    LEFT(client_secret, 60) as new_secret,
    LENGTH(client_secret) as secret_length,
    scopes,
    status,
    '密码: 123456' as password_hint
FROM api_clients 
WHERE client_id = 'client_test_demo_001';
