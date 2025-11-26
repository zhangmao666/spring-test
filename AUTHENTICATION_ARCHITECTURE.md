# 认证架构说明

## 📐 架构概览

本项目实现了**双认证体系**，支持不同场景的访问需求：

```
┌──────────────────────────────────────────────────────────────┐
│                      认证架构                                  │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌─────────────────────┐      ┌──────────────────────────┐  │
│  │  用户JWT认证         │      │  OAuth2客户端认证         │  │
│  ├─────────────────────┤      ├──────────────────────────┤  │
│  │ 👤 Web/移动端用户    │      │ 🏢 第三方公司/服务        │  │
│  │ 📍 /auth/login      │      │ 📍 /oauth2/token         │  │
│  │ 🔑 自定义JWT         │      │ 🔑 标准OAuth2 JWT        │  │
│  │ ✍️  HMAC签名         │      │ ✍️  RSA签名              │  │
│  └─────────────────────┘      └──────────────────────────┘  │
│           ↓                             ↓                   │
│  JwtAuthenticationFilter      OAuth2 Resource Server       │
│           ↓                             ↓                   │
│  ─────────────────────────────────────────────────────────  │
│                  Spring Security Context                    │
│  ─────────────────────────────────────────────────────────  │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 🔐 认证方式对比

| 特性 | 用户JWT认证 | OAuth2客户端认证 |
|-----|-----------|----------------|
| **适用场景** | 人机交互（Web/App） | 机器对机器（M2M） |
| **认证端点** | `/auth/login` | `/oauth2/token` |
| **凭证类型** | username + password | client_id + client_secret |
| **Token签名** | HMAC-SHA256 | RSA-2048 |
| **Token类型** | 自定义JWT | 标准OAuth2 JWT |
| **验证方式** | JwtAuthenticationFilter | OAuth2 Resource Server |
| **标准化** | 自定义 | OAuth 2.1标准 |
| **第三方集成** | ❌ | ✅ |

---

## 🎯 方式1：用户JWT认证

### **使用场景**
- Web前端用户登录
- 移动App用户登录
- 内部员工系统访问

### **工作流程**

```
1. 用户提交 username + password
   ↓
2. UserService验证凭证
   ↓
3. JwtUtil生成自定义JWT Token
   ↓
4. 返回Token给前端
   ↓
5. 前端每次请求携带: Authorization: Bearer <token>
   ↓
6. JwtAuthenticationFilter验证Token
   ↓
7. 设置Spring Security上下文
```

### **示例代码**

```powershell
# 登录
$response = Invoke-RestMethod -Uri "http://localhost:8879/api/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body (@{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json)

$userToken = $response.data.token

# 使用Token调用API
Invoke-RestMethod -Uri "http://localhost:8879/api/files/list" `
    -Headers @{"Authorization" = "Bearer $userToken"}
```

---

## 🏢 方式2：OAuth2客户端认证

### **使用场景**
- 第三方公司集成
- 微服务间调用
- 定时任务/批处理脚本
- 服务端对服务端（Server-to-Server）

### **工作流程**

```
1. 客户端提交 client_id + client_secret
   ↓
2. OAuth2授权服务器验证凭证
   ↓
3. 生成标准OAuth2 JWT Token（RSA签名）
   ↓
4. 返回access_token
   ↓
5. 客户端每次请求携带: Authorization: Bearer <token>
   ↓
6. OAuth2 Resource Server验证Token
   ↓
7. 设置Spring Security上下文
```

### **示例代码**

```powershell
# 获取OAuth2 Token
$credentials = "client_id:client_secret"
$basicAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($credentials))

$response = Invoke-RestMethod -Uri "http://localhost:8879/api/oauth2/token" `
    -Method Post `
    -Headers @{
        "Authorization" = "Basic $basicAuth"
        "Content-Type" = "application/x-www-form-urlencoded"
    } `
    -Body "grant_type=client_credentials"

$oauth2Token = $response.access_token

# 使用Token调用API
Invoke-RestMethod -Uri "http://localhost:8879/api/files/list" `
    -Headers @{"Authorization" = "Bearer $oauth2Token"}
```

---

## 🔧 技术实现

### **核心组件**

#### 1. **用户认证链**
```
AuthController
    ↓
UserService (验证密码)
    ↓
JwtUtil (生成HMAC JWT)
    ↓
JwtAuthenticationFilter (验证Token)
```

#### 2. **OAuth2认证链**
```
OAuth2AuthorizationServerConfig
    ↓
MybatisRegisteredClientRepository (验证client凭证)
    ↓
OAuth2 Token Endpoint (生成RSA JWT)
    ↓
OAuth2 Resource Server (验证Token)
```

### **关键文件**

```
src/main/java/com/example/springboottest/
├── config/
│   ├── SecurityConfig.java                    # 主安全配置
│   ├── OAuth2AuthorizationServerConfig.java   # OAuth2服务器配置
│   ├── JwtAuthenticationFilter.java           # 用户JWT过滤器
│   └── RegisteredClientConfig.java            # OAuth2客户端配置
├── controller/
│   └── AuthController.java                    # 用户登录接口
├── service/
│   └── UserService.java                       # 用户认证服务
├── util/
│   └── JwtUtil.java                           # JWT工具类
└── repository/
    ├── ApiClientRepository.java               # OAuth2客户端存储
    └── MybatisRegisteredClientRepository.java # OAuth2客户端适配器
```

---

## 🔄 为什么使用双认证体系？

### **原方案问题**
❌ 存在3种认证方式（用户JWT、自定义API客户端、OAuth2）  
❌ 自定义API客户端与OAuth2功能重复  
❌ 第三方需要学习自定义协议  
❌ 维护成本高  

### **现方案优势**
✅ 清晰的职责划分：用户用JWT，第三方用OAuth2  
✅ 符合行业标准，第三方容易集成  
✅ 减少维护成本  
✅ 保持灵活性  

---

## 📚 相关文档

- **[OAuth2 第三方集成指南](OAUTH2_INTEGRATION_GUIDE.md)** - 第三方公司必读
- **[用户认证测试脚本](test_ai.ps1)** - 用户JWT认证示例
- **[OAuth2测试脚本](test_oauth2_token.ps1)** - OAuth2认证示例

---

## 🔒 安全配置状态

当前SecurityConfig中的认证配置：

```java
// ⚠️ 开发环境：所有接口暂时开放
.anyRequest().permitAll()

// 生产环境应使用：
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**").permitAll()      // 用户登录接口
    .requestMatchers("/oauth2/**").permitAll()    // OAuth2接口
    .requestMatchers("/health").permitAll()       // 健康检查
    .anyRequest().authenticated()                 // 其他需要认证
)
```

**上线前务必启用生产配置！**

---

**最后更新时间**：2025-11-18
