# 🔄 从OAuth2迁移到API Key认证

## 📋 迁移概述

本项目已从OAuth2认证切换到更简单的**API Key认证方式**。

---

## ✅ 已完成的更改

### **1. 删除的文件**
- ❌ `OAuth2AuthorizationServerConfig.java` - OAuth2服务器配置
- ❌ `RegisteredClientConfig.java` - OAuth2客户端注册配置
- ❌ `MybatisRegisteredClientRepository.java` - OAuth2客户端仓库

### **2. 新增的文件**
- ✅ `ApiKeyAuthenticationFilter.java` - API Key认证过滤器
- ✅ `ApiKeyService.java` - API Key管理服务
- ✅ `ApiKeyController.java` - API Key管理控制器

### **3. 修改的文件**
- ✅ `SecurityConfig.java` - 移除OAuth2配置，添加API Key过滤器
- ✅ `ApiClient.java` - 添加`apiKey`字段
- ✅ `ApiClientRepository.java` - 添加`findByApiKey`方法

### **4. 数据库更改**
- ✅ `api_clients`表添加`api_key`字段（VARCHAR(64) UNIQUE）

---

## 🚀 迁移步骤

### **步骤1：执行数据库迁移**

在MySQL中执行以下SQL：

```bash
mysql -h 118.24.128.221 -u admin -p spring_boot_test < add_api_key_column.sql
```

或手动执行：

```sql
-- 添加api_key字段
ALTER TABLE api_clients 
ADD COLUMN api_key VARCHAR(64) UNIQUE COMMENT 'API Key' 
AFTER client_secret;

-- 添加索引
CREATE INDEX idx_api_key ON api_clients(api_key);

-- 为现有客户端生成API Key
UPDATE api_clients 
SET api_key = CONCAT('apk_', REPLACE(UUID(), '-', ''))
WHERE api_key IS NULL;
```

### **步骤2：验证数据库**

```sql
-- 查看客户端列表
SELECT 
    client_id,
    client_name,
    LEFT(api_key, 20) as api_key_preview,
    status
FROM api_clients;
```

### **步骤3：重启应用**

```bash
# 停止应用
# 启动应用
mvn spring-boot:run
```

### **步骤4：测试API Key认证**

使用PowerShell测试：

```powershell
.\test_api_key.ps1
```

或手动测试：

```powershell
# 获取API Key（从数据库）
$apiKey = "apk_your_api_key_here"

# 测试API调用
Invoke-RestMethod -Uri "http://localhost:8879/api/weather/cities" `
    -Headers @{"X-API-Key" = $apiKey}
```

---

## 📖 新的认证方式

### **之前（OAuth2）**

```bash
# 步骤1: 获取Token
POST /oauth2/token
Authorization: Basic base64(client_id:client_secret)
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials

# 步骤2: 使用Token
GET /api/weather/cities
Authorization: Bearer <access_token>
```

### **现在（API Key）**

```bash
# 直接使用API Key
GET /api/weather/cities
X-API-Key: apk_your_api_key_here
```

---

## 🔑 获取API Key

### **方法1：从数据库获取**

```sql
SELECT client_id, api_key 
FROM api_clients 
WHERE client_id = 'client_test_demo_001';
```

### **方法2：管理员创建新客户端**

```bash
POST /api/clients
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json

{
  "clientName": "第三方公司",
  "scopes": "read,write",
  "description": "描述"
}
```

响应包含新生成的API Key。

---

## 📝 Postman配置

### **Headers配置**

| Key | Value |
|-----|-------|
| X-API-Key | apk_your_api_key_here |

### **或使用Authorization标签**

- Type: `API Key`
- Key: `X-API-Key`
- Value: `apk_your_api_key_here`
- Add to: `Header`

---

## ⚠️ 注意事项

### **1. API Key安全性**

- ✅ API Key不会过期（除非重新生成）
- ✅ 使用HTTPS传输（生产环境）
- ✅ 不要将API Key提交到Git
- ✅ 使用环境变量存储

### **2. 管理API Key**

管理员可以：
- 创建新客户端（自动生成API Key）
- 重新生成API Key（旧Key立即失效）
- 启用/禁用客户端
- 删除客户端

### **3. 权限控制**

API Key的权限由`scopes`字段控制：
- `read` - 读取权限
- `write` - 写入权限
- 多个权限用逗号分隔：`read,write`

---

## 🧪 测试清单

- [ ] 数据库迁移成功
- [ ] 应用启动无报错
- [ ] 可以使用API Key访问受保护接口
- [ ] 无效的API Key返回401错误
- [ ] 禁用的客户端无法访问
- [ ] 管理接口正常工作（需要管理员权限）

---

## 🐛 常见问题

### **Q: 数据库中没有API Key？**
A: 执行SQL迁移脚本 `add_api_key_column.sql`

### **Q: API调用返回401？**
A: 检查：
1. X-API-Key请求头是否正确
2. API Key是否有效
3. 客户端状态是否为启用（status=1）

### **Q: 如何获取第一个API Key？**
A: 
```sql
-- 为现有客户端生成API Key
UPDATE api_clients 
SET api_key = CONCAT('apk_', REPLACE(UUID(), '-', ''))
WHERE client_id = 'client_test_demo_001';

-- 查看生成的API Key
SELECT api_key FROM api_clients 
WHERE client_id = 'client_test_demo_001';
```

### **Q: OAuth2端点还能用吗？**
A: 不能。OAuth2相关配置已删除，请使用API Key认证。

---

## 📚 相关文档

- **[API_KEY_GUIDE.md](API_KEY_GUIDE.md)** - 完整的API Key使用指南
- **[AUTHENTICATION_ARCHITECTURE.md](AUTHENTICATION_ARCHITECTURE.md)** - 认证架构说明
- **[test_api_key.ps1](test_api_key.ps1)** - 测试脚本

---

## 📞 技术支持

如遇到迁移问题，请联系技术支持团队。

---

**迁移完成日期**：2025-11-18
