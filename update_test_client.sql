-- 如果你想使用自定义密钥，请按以下步骤操作：

-- 方案1：保持使用 "123456" 作为测试密钥（推荐，已在代码中配置）
-- 无需执行任何操作，直接使用即可

-- 方案2：如果想改为使用 "test_secret_123" 或其他密钥
-- 需要先生成BCrypt哈希，然后更新数据库

-- 步骤1：运行Spring Boot应用
-- 步骤2：调用创建客户端接口（需要管理员权限）
-- 或者使用在线BCrypt生成器生成哈希值

-- 示例：更新现有测试客户端的密钥
-- 注意：下面的哈希需要你自己生成
-- UPDATE api_clients 
-- SET client_secret = '$2a$10$YOUR_BCRYPT_HASH_HERE'
-- WHERE client_id = 'client_test_demo_001';

-- 或者删除旧的测试客户端，通过API创建新的
-- DELETE FROM api_clients WHERE client_id = 'client_test_demo_001';

-- ============================================
-- 推荐方案：使用API接口创建新客户端
-- ============================================
-- 1. 先登录获取管理员token
-- 2. 调用创建客户端接口：POST /api/auth/clients
--    {
--      "clientName": "我的测试客户端",
--      "scopes": "read,write",
--      "tokenExpiration": 7200000,
--      "description": "测试用"
--    }
-- 3. 接口会返回新的client_id和未加密的client_secret
-- 4. 保存好这些信息，client_secret只显示一次














