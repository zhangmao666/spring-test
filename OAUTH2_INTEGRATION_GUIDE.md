# OAuth2 第三方集成指南

## 📋 概述

本项目提供**标准OAuth 2.1授权服务器**，第三方应用可以通过OAuth2协议获取访问令牌（Access Token），用于调用受保护的API接口。

---

## 🔑 认证架构

本项目支持**两种认证方式**：

### 1. **用户登录认证**（User Authentication）
- 适用场景：Web/移动应用的最终用户
- 端点：`POST /auth/login`
- Token类型：自定义JWT（HMAC签名）

### 2. **OAuth2客户端认证**（Client Credentials）
- 适用场景：第三方应用/服务间调用
- 端点：`POST /oauth2/token`
- Token类型：标准OAuth2 JWT（RSA签名）
- **推荐第三方公司使用此方式！**

---

## 🚀 快速开始

### 第一步：获取客户端凭证

联系系统管理员获取以下信息：

- **Client ID**：客户端标识符（例如：`client_company_abc_001`）
- **Client Secret**：客户端密钥（请妥善保管，不要泄露！）
- **API Base URL**：接口基础地址（例如：`http://api.example.com/api`）

---

### 第二步：获取Access Token

#### **请求示例**

**HTTP请求**：
```http
POST http://api.example.com/api/oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic <base64(client_id:client_secret)>

grant_type=client_credentials
```

#### **使用curl**：
```bash
curl -X POST http://api.example.com/api/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "your_client_id:your_client_secret" \
  -d "grant_type=client_credentials"
```

#### **使用PowerShell**：
```powershell
# 客户端凭证
$clientId = "your_client_id"
$clientSecret = "your_client_secret"
$baseUrl = "http://api.example.com/api"

# 生成Basic认证头
$basicAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${clientId}:${clientSecret}"))

# 发送请求
$response = Invoke-RestMethod -Uri "$baseUrl/oauth2/token" `
    -Method Post `
    -Headers @{
        "Authorization" = "Basic $basicAuth"
        "Content-Type" = "application/x-www-form-urlencoded"
    } `
    -Body "grant_type=client_credentials"

# 提取access_token
$accessToken = $response.access_token
Write-Host "Access Token: $accessToken"
```

#### **响应示例**：
```json
{
  "access_token": "eyJraWQiOiI5ZjBkOTk4ZC0...",
  "token_type": "Bearer",
  "expires_in": 7200,
  "scope": "read write"
}
```

---

### 第三步：使用Token调用API

在HTTP请求头中添加`Authorization`字段，值为`Bearer <access_token>`：

#### **请求示例**：
```http
GET http://api.example.com/api/files/list
Authorization: Bearer eyJraWQiOiI5ZjBkOTk4ZC0...
```

#### **使用curl**：
```bash
curl -X GET http://api.example.com/api/files/list \
  -H "Authorization: Bearer eyJraWQiOiI5ZjBkOTk4ZC0..."
```

#### **使用PowerShell**：
```powershell
$apiResponse = Invoke-RestMethod -Uri "$baseUrl/files/list" `
    -Method Get `
    -Headers @{
        "Authorization" = "Bearer $accessToken"
    }
```

---

## 📝 完整代码示例

### **PowerShell完整脚本**

```powershell
# OAuth2客户端认证脚本
$baseUrl = "http://api.example.com/api"
$clientId = "your_client_id"
$clientSecret = "your_client_secret"

Write-Host "=== OAuth2 客户端认证 ===" -ForegroundColor Cyan

# 1. 获取Access Token
Write-Host "`n步骤1: 获取Access Token..." -ForegroundColor Yellow

$basicAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${clientId}:${clientSecret}"))

try {
    $tokenResponse = Invoke-RestMethod -Uri "$baseUrl/oauth2/token" `
        -Method Post `
        -Headers @{
            "Authorization" = "Basic $basicAuth"
            "Content-Type" = "application/x-www-form-urlencoded"
        } `
        -Body "grant_type=client_credentials"
    
    $accessToken = $tokenResponse.access_token
    $expiresIn = $tokenResponse.expires_in
    
    Write-Host "✓ Token获取成功" -ForegroundColor Green
    Write-Host "  - Access Token: $($accessToken.Substring(0, 50))..." -ForegroundColor Gray
    Write-Host "  - 过期时间: $expiresIn 秒" -ForegroundColor Gray
    
} catch {
    Write-Host "✗ Token获取失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. 调用业务API
Write-Host "`n步骤2: 调用业务API..." -ForegroundColor Yellow

try {
    $apiResponse = Invoke-RestMethod -Uri "$baseUrl/files/list" `
        -Method Get `
        -Headers @{
            "Authorization" = "Bearer $accessToken"
        }
    
    Write-Host "✓ API调用成功" -ForegroundColor Green
    Write-Host ($apiResponse | ConvertTo-Json -Depth 3) -ForegroundColor White
    
} catch {
    Write-Host "✗ API调用失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 完成 ===" -ForegroundColor Cyan
```

### **Python示例**

```python
import requests
import base64

# 配置
BASE_URL = "http://api.example.com/api"
CLIENT_ID = "your_client_id"
CLIENT_SECRET = "your_client_secret"

# 1. 获取Access Token
def get_access_token():
    url = f"{BASE_URL}/oauth2/token"
    
    # Basic认证
    credentials = f"{CLIENT_ID}:{CLIENT_SECRET}"
    basic_auth = base64.b64encode(credentials.encode()).decode()
    
    headers = {
        "Authorization": f"Basic {basic_auth}",
        "Content-Type": "application/x-www-form-urlencoded"
    }
    
    data = {"grant_type": "client_credentials"}
    
    response = requests.post(url, headers=headers, data=data)
    response.raise_for_status()
    
    return response.json()["access_token"]

# 2. 调用API
def call_api(access_token):
    url = f"{BASE_URL}/files/list"
    
    headers = {
        "Authorization": f"Bearer {access_token}"
    }
    
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    
    return response.json()

# 主流程
if __name__ == "__main__":
    try:
        # 获取Token
        print("获取Access Token...")
        token = get_access_token()
        print(f"✓ Token: {token[:50]}...")
        
        # 调用API
        print("\n调用API...")
        result = call_api(token)
        print(f"✓ 响应: {result}")
        
    except Exception as e:
        print(f"✗ 错误: {str(e)}")
```

### **Java示例**

```java
import java.net.http.*;
import java.net.URI;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OAuth2Client {
    
    private static final String BASE_URL = "http://api.example.com/api";
    private static final String CLIENT_ID = "your_client_id";
    private static final String CLIENT_SECRET = "your_client_secret";
    
    public static void main(String[] args) throws Exception {
        // 1. 获取Access Token
        String accessToken = getAccessToken();
        System.out.println("✓ Access Token: " + accessToken.substring(0, 50) + "...");
        
        // 2. 调用API
        String result = callApi(accessToken);
        System.out.println("✓ API响应: " + result);
    }
    
    private static String getAccessToken() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        // Basic认证
        String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
        String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes());
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/oauth2/token"))
            .header("Authorization", "Basic " + basicAuth)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
            .build();
        
        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(response.body()).get("access_token").asText();
    }
    
    private static String callApi(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/files/list"))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();
        
        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        return response.body();
    }
}
```

---

## 🔒 安全最佳实践

### 1. **保护Client Secret**
- ❌ 不要将Client Secret硬编码在前端代码中
- ❌ 不要将Client Secret提交到Git仓库
- ✅ 使用环境变量或配置管理系统存储
- ✅ 定期轮换Client Secret

### 2. **Token管理**
- ✅ 检查`expires_in`字段，在Token过期前刷新
- ✅ 使用HTTPS传输（生产环境必须）
- ✅ Token应存储在安全的位置（服务器端或加密存储）
- ❌ 不要在日志中打印完整Token

### 3. **错误处理**
```powershell
try {
    # API调用
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    
    switch ($statusCode) {
        401 { Write-Host "Token已过期或无效，请重新获取" }
        403 { Write-Host "权限不足" }
        429 { Write-Host "请求过于频繁，请稍后重试" }
        default { Write-Host "服务器错误: $statusCode" }
    }
}
```

---

## 🌐 标准OAuth2端点

本系统实现了完整的OAuth 2.1授权服务器，提供以下标准端点：

| 端点 | 说明 | 公开访问 |
|------|------|---------|
| `/oauth2/token` | 获取访问令牌 | ✅ |
| `/oauth2/jwks` | JWT密钥集合 | ✅ |
| `/oauth2/revoke` | 撤销令牌 | ✅ |
| `/oauth2/introspect` | 令牌自省 | ✅ |
| `/.well-known/oauth-authorization-server` | 服务器元数据 | ✅ |

---

## ❓ 常见问题

### Q1: Token有效期是多久？
A: 默认为2小时（7200秒），可以在响应的`expires_in`字段中查看。

### Q2: Token过期后如何处理？
A: 使用Client Credentials模式时，直接重新请求`/oauth2/token`获取新Token即可。

### Q3: 如何测试Token是否有效？
A: 调用任意受保护的API接口，如果返回401错误，说明Token无效或已过期。

### Q4: 支持哪些授权模式？
A: 目前支持`client_credentials`（客户端凭证模式），适用于服务间调用。

### Q5: 如何查看我有哪些权限（scopes）？
A: Token响应中的`scope`字段包含您的权限范围，常见值：`read`（读取）、`write`（写入）。

---

## 📞 技术支持

如遇到问题，请联系：
- 📧 Email: api-support@example.com
- 📱 技术支持热线: xxx-xxxx-xxxx
- 📖 API文档: http://api.example.com/docs

---

## 📄 附录

### OAuth2 Client Credentials流程图

```
┌─────────────┐                                  ┌─────────────────┐
│             │                                  │                 │
│  第三方应用  │─────(1) 发送凭证────────────────▶│  OAuth2 Server  │
│             │    client_id + secret            │                 │
└─────────────┘                                  └─────────────────┘
       │                                                   │
       │                                                   │
       │                                         (2) 验证凭证
       │                                                   │
       │                                                   ▼
       │                                          生成Access Token
       │                                                   │
       │◀─────(3) 返回Token──────────────────────────────│
       │       {access_token, expires_in}                 │
       │                                                   │
       │                                                   │
       ▼                                                   │
 使用Token调用API                                         │
       │                                                   │
       │                                                   │
       │                                  ┌─────────────────┐
       │                                  │                 │
       └──────(4) Authorization: Bearer──▶│   API Server    │
                  <token>                 │                 │
                                          └─────────────────┘
```

---

**最后更新时间**：2025-11-18
