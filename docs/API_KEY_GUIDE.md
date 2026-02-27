# 🔑 API Key认证使用指南

## 📋 概述

本项目已从OAuth2认证切换到**更简单的API Key认证方式**，第三方客户端只需携带API Key即可访问受保护的接口。

---

## 🎯 认证架构

```
┌──────────────────────────────────────────────────────────┐
│                    认证架构（简化版）                      │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  ┌─────────────────────┐      ┌───────────────────────┐ │
│  │  用户JWT认证         │      │  API Key认证          │ │
│  ├─────────────────────┤      ├───────────────────────┤ │
│  │ 👤 Web/移动端用户    │      │ 🏢 第三方公司/服务     │ │
│  │ 📍 /auth/login      │      │ 📍 X-API-Key Header   │ │
│  │ 🔑 自定义JWT         │      │ 🔑 API Key字符串      │ │
│  └─────────────────────┘      └───────────────────────┘ │
│           ↓                             ↓                │
│  JwtAuthenticationFilter      ApiKeyAuthenticationFilter│
│           ↓                             ↓                │
│  ─────────────────────────────────────────────────────  │
│                  Spring Security Context                 │
│  ─────────────────────────────────────────────────────  │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## 🚀 快速开始

### 步骤1：获取API Key

联系系统管理员获取API Key，格式如下：
```
apk_0123456789abcdef0123456789abcdef
```

### 步骤2：在请求头中携带API Key

所有API请求都需要添加 `X-API-Key` 请求头：

```http
GET /api/weather/cities HTTP/1.1
Host: localhost:8879
X-API-Key: apk_0123456789abcdef0123456789abcdef
```

---

## 📮 Postman使用

### **配置方法1：在Headers中添加**

1. 打开Postman请求
2. 切换到 **Headers** 标签
3. 添加请求头：

| Key | Value |
|-----|-------|
| X-API-Key | `apk_your_api_key_here` |

4. 发送请求

### **配置方法2：使用API Key类型认证**

1. 切换到 **Authorization** 标签
2. Type选择：`API Key`
3. 配置：
   - Key: `X-API-Key`
   - Value: `apk_your_api_key_here`
   - Add to: `Header`

---

## 💻 代码示例

### **PowerShell**

```powershell
# 配置
$baseUrl = "http://localhost:8879/api"
$apiKey = "apk_your_api_key_here"

# 调用API
$response = Invoke-RestMethod -Uri "$baseUrl/weather/cities" `
    -Method Get `
    -Headers @{
        "X-API-Key" = $apiKey
    }

Write-Host ($response | ConvertTo-Json -Depth 2)
```

### **cURL**

```bash
# 调用API
curl -X GET http://localhost:8879/api/weather/cities \
  -H "X-API-Key: apk_your_api_key_here"

# 示例：上传文件
curl -X POST http://localhost:8879/api/files/upload \
  -H "X-API-Key: apk_your_api_key_here" \
  -F "file=@document.pdf"
```

### **Python**

```python
import requests

# 配置
BASE_URL = "http://localhost:8879/api"
API_KEY = "apk_your_api_key_here"

# 设置请求头
headers = {
    "X-API-Key": API_KEY
}

# GET请求
response = requests.get(f"{BASE_URL}/weather/cities", headers=headers)
print(response.json())

# POST请求
data = {"name": "test"}
response = requests.post(f"{BASE_URL}/files/upload", 
                        headers=headers, 
                        json=data)
print(response.json())
```

### **Java (Spring RestTemplate)**

```java
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class ApiKeyClient {
    
    private static final String BASE_URL = "http://localhost:8879/api";
    private static final String API_KEY = "apk_your_api_key_here";
    
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", API_KEY);
        
        // 创建请求实体
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // 发送请求
        String response = restTemplate.exchange(
            BASE_URL + "/weather/cities",
            HttpMethod.GET,
            entity,
            String.class
        ).getBody();
        
        System.out.println(response);
    }
}
```

### **JavaScript (Fetch)**

```javascript
const BASE_URL = 'http://localhost:8879/api';
const API_KEY = 'apk_your_api_key_here';

// GET请求
fetch(`${BASE_URL}/weather/cities`, {
  method: 'GET',
  headers: {
    'X-API-Key': API_KEY
  }
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));

// POST请求
fetch(`${BASE_URL}/files/upload`, {
  method: 'POST',
  headers: {
    'X-API-Key': API_KEY,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ name: 'test' })
})
.then(response => response.json())
.then(data => console.log(data));
```

---

## 🔧 管理接口（管理员）

### **创建新客户端**

```bash
POST /api/clients
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json

{
  "clientName": "第三方公司名称",
  "scopes": "read,write",
  "description": "客户端描述"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "客户端创建成功",
  "data": {
    "clientId": "client_1700000000000",
    "clientName": "第三方公司名称",
    "apiKey": "apk_0123456789abcdef0123456789abcdef",
    "scopes": "read,write",
    "status": 1,
    "message": "请妥善保管API Key，它不会再次显示！"
  }
}
```

### **重新生成API Key**

```bash
POST /api/clients/{clientId}/regenerate-key
Authorization: Bearer <admin_jwt_token>
```

### **启用/禁用客户端**

```bash
# 启用
PUT /api/clients/{clientId}/enable
Authorization: Bearer <admin_jwt_token>

# 禁用
PUT /api/clients/{clientId}/disable
Authorization: Bearer <admin_jwt_token>
```

### **获取客户端列表**

```bash
GET /api/clients
Authorization: Bearer <admin_jwt_token>
```

### **删除客户端**

```bash
DELETE /api/clients/{clientId}
Authorization: Bearer <admin_jwt_token>
```

---

## 🔒 安全最佳实践

### 1. **保护API Key**

❌ **不要做的事**：
- 将API Key硬编码在前端代码中
- 将API Key提交到Git仓库
- 在日志中打印完整的API Key
- 通过URL参数传递API Key

✅ **应该做的事**：
- 使用环境变量存储API Key
- 使用配置管理系统（如Vault）
- 定期轮换API Key
- 监控API Key使用情况

### 2. **环境变量示例**

**PowerShell**:
```powershell
$env:API_KEY = "apk_your_api_key_here"
$apiKey = $env:API_KEY
```

**Bash**:
```bash
export API_KEY="apk_your_api_key_here"
api_key=$API_KEY
```

**Python (.env文件)**:
```python
# .env
API_KEY=apk_your_api_key_here

# app.py
from dotenv import load_dotenv
import os

load_dotenv()
API_KEY = os.getenv('API_KEY')
```

### 3. **HTTPS传输**

生产环境必须使用HTTPS，防止API Key在传输过程中被截获。

---

## ⚠️ 错误处理

### **401 Unauthorized**

**原因**：API Key无效或未提供

**解决方案**：
- 检查`X-API-Key`请求头是否正确设置
- 确认API Key没有拼写错误
- 联系管理员确认API Key状态

### **403 Forbidden**

**原因**：API Key对应的客户端已被禁用或权限不足

**解决方案**：
- 联系管理员检查客户端状态
- 确认客户端的scopes是否包含所需权限

---

## 📊 API Key格式

API Key格式：`apk_<32位十六进制字符>`

示例：
```
apk_a1b2c3d4e5f67890a1b2c3d4e5f67890
```

- 前缀：`apk_`（API Key的标识）
- 长度：36个字符（前缀4个+UUID 32个）
- 字符集：`[a-f0-9]`（十六进制）

---

## 🔄 从OAuth2迁移

如果你之前使用OAuth2认证，现在只需：

**之前（OAuth2）**：
```bash
# 1. 获取token
curl -X POST http://localhost:8879/api/oauth2/token \
  -u "client_id:client_secret" \
  -d "grant_type=client_credentials"

# 2. 使用token
curl -X GET http://localhost:8879/api/weather/cities \
  -H "Authorization: Bearer <access_token>"
```

**现在（API Key）**：
```bash
# 直接使用API Key
curl -X GET http://localhost:8879/api/weather/cities \
  -H "X-API-Key: apk_your_api_key_here"
```

**优势**：
- ✅ 无需先获取token
- ✅ 没有token过期问题
- ✅ 集成更简单
- ✅ 性能更好（减少一次请求）

---

## 📞 技术支持

- 📧 Email: api-support@example.com
- 📱 技术支持热线: xxx-xxxx-xxxx
- 📖 API文档: http://api.example.com/docs

---

## 📝 常见问题

**Q: API Key会过期吗？**  
A: 不会。API Key长期有效，除非被重新生成或客户端被禁用。

**Q: 如何刷新API Key？**  
A: 联系管理员使用"重新生成API Key"接口，旧Key立即失效。

**Q: 可以使用查询参数传递API Key吗？**  
A: 技术上可以（`?apiKey=xxx`），但强烈不推荐，因为URL会被记录在日志中。

**Q: 一个客户端可以有多个API Key吗？**  
A: 目前不支持。每个客户端只有一个API Key。

---

**最后更新时间**：2025-11-18
