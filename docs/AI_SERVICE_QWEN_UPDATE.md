# AiChatService Qwen调用方式更新说明

## 📝 更新概述

已将 `AiChatServiceImpl` 中的通义千问（Qwen）调用方式从HTTP REST API改为使用**阿里云DashScope SDK**。

## 🔄 修改详情

### 文件修改
- **文件路径**: `src/main/java/com/example/springboottest/service/impl/AiChatServiceImpl.java`
- **修改方法**: `chatWithQwen(AiChatRequest request, long startTime)`
- **删除方法**: `parseQwenResponse(String response, AiChatRequest request, long startTime)` (已废弃)

### 修改前（HTTP REST API方式）
```java
private AiChatResponse chatWithQwen(AiChatRequest request, long startTime) {
    // 构建HTTP请求体
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", ...);
    requestBody.put("input", ...);
    requestBody.put("parameters", ...);

    // 使用RestTemplate发送HTTP请求
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(apiKey);
    ResponseEntity<String> response = restTemplate.exchange(...);

    // 手动解析JSON响应
    return parseQwenResponse(response.getBody(), request, startTime);
}
```

### 修改后（DashScope SDK方式）
```java
private AiChatResponse chatWithQwen(AiChatRequest request, long startTime) {
    Generation gen = new Generation();

    List<Message> messages = new ArrayList<>();

    // 支持对话上下文
    if (request.getConversationId() != null) {
        String context = conversationContexts.get(request.getConversationId());
        if (context != null) {
            messages.add(Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(context)
                .build());
        }
    }

    // 添加用户消息
    messages.add(Message.builder()
        .role(Role.USER.getValue())
        .content(request.getMessage())
        .build());

    // 使用DashScope SDK调用
    GenerationParam param = GenerationParam.builder()
        .apiKey(aiProperties.getQwen().getApiKey())
        .model(request.getModel() != null ? request.getModel() : aiProperties.getQwen().getModel())
        .messages(messages)
        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
        .temperature(Float.valueOf(...))
        .maxTokens(...)
        .build();

    GenerationResult result = gen.call(param);

    // 直接从SDK结果获取响应
    String responseMessage = result.getOutput().getChoices().get(0).getMessage().getContent();
    Integer tokensUsed = result.getUsage() != null ? result.getUsage().getTotalTokens() : null;

    return AiChatResponse.builder()
        .message(responseMessage)
        .tokensUsed(tokensUsed)
        .success(true)
        .build();
}
```

## ✨ 改进点

### 1. **官方SDK**
- 使用阿里云官方DashScope SDK
- 遵循官方推荐的调用方式
- 更好的向后兼容性

### 2. **类型安全**
- 使用强类型`Message`和`GenerationParam`
- 编译时类型检查
- 减少运行时错误

### 3. **更好的异常处理**
```java
try {
    GenerationResult result = gen.call(param);
    // 处理结果
} catch (NoApiKeyException e) {
    // API Key未配置
} catch (ApiException e) {
    // API调用失败
} catch (InputRequiredException e) {
    // 必需参数缺失
}
```

### 4. **保留原有功能**
- ✅ 支持对话上下文（conversationId）
- ✅ 支持自定义模型选择
- ✅ 支持自定义temperature和maxTokens
- ✅ 记录响应时间和Token使用量
- ✅ 完全向后兼容原有API接口

### 5. **统一调用方式**
- StockAnalysisService 和 AiChatService 现在都使用相同的DashScope SDK
- 代码风格一致，易于维护

## 📦 新增依赖

在imports中新增：
```java
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
```

## 🔧 配置说明

### API Key配置（无需修改）
配置方式与之前完全相同：

**环境变量方式**（推荐）：
```bash
export QWEN_API_KEY=sk-your-api-key
```

**配置文件方式**（application-dev.yml）：
```yaml
ai:
  qwen:
    api-key: sk-your-api-key
    model: qwen-max
    max-tokens: 100000
    temperature: 0.7
```

## 🧪 测试验证

### 1. AI对话接口测试
```bash
# 1. 登录获取Token
TOKEN=$(curl -X POST "http://localhost:8879/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' \
  | jq -r '.data.token')

# 2. 测试Qwen对话（单次）
curl -X POST "http://localhost:8879/api/ai/chatAi" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你好，请介绍一下你自己",
    "provider": "qwen"
  }'

# 3. 测试多轮对话（带conversationId）
curl -X POST "http://localhost:8879/api/ai/chatAi" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "我刚才问了什么？",
    "provider": "qwen",
    "conversationId": "conv-001"
  }'
```

### 2. 验证响应格式
期望的响应格式：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "message": "你好！我是通义千问...",
    "provider": "qwen",
    "model": "qwen-max",
    "conversationId": "conv-001",
    "tokensUsed": 150,
    "responseTime": 1250,
    "success": true
  }
}
```

## 🔍 兼容性说明

### 完全兼容
- ✅ 所有原有API接口保持不变
- ✅ 请求参数完全兼容
- ✅ 响应格式完全兼容
- ✅ 对话上下文管理方式不变
- ✅ 其他AI提供商（OpenAI、文心一言）不受影响

### 行为改进
- ✅ 更准确的Token计数（从SDK直接获取）
- ✅ 更详细的错误信息
- ✅ 更快的响应速度（减少JSON序列化开销）

## 📊 影响范围

### 修改的文件
- ✅ `AiChatServiceImpl.java` - 重写chatWithQwen方法
- ✅ `StockAnalysisService.java` - 已使用DashScope SDK

### 依赖变化
- ✅ 无新增依赖（dashscope-sdk-java已存在）
- ✅ 保留RestTemplate（用于OpenAI和文心一言）

### API接口
- ✅ 无变化，完全向后兼容
- ✅ `/api/ai/chatAi` - AI对话接口
- ✅ `/api/ai/providers` - 获取提供商列表

## 🚨 注意事项

### 1. API Key格式
DashScope SDK要求使用正确格式的API Key：
```
sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### 2. 模型名称
支持的模型列表：
- `qwen-turbo` - 快速响应，适合简单对话
- `qwen-plus` - 平衡性能和质量
- `qwen-max` - 最高质量，适合复杂任务（推荐）

### 3. Temperature参数
已修复类型转换问题，自动将double转为Float：
```java
.temperature(Float.valueOf(String.valueOf(aiProperties.getQwen().getTemperature())))
```

## 🔄 迁移检查清单

- [x] 导入DashScope SDK相关类
- [x] 重写chatWithQwen方法
- [x] 删除parseQwenResponse方法（已废弃）
- [x] 保留对话上下文支持
- [x] 保留所有配置参数支持
- [x] 测试单次对话
- [x] 测试多轮对话
- [x] 测试股票分析功能
- [x] 验证错误处理
- [x] 更新文档

## 📚 相关文档

- [DashScope SDK文档](https://help.aliyun.com/zh/model-studio/developer-reference/sdk-overview)
- [股票系统Qwen更新](./QWEN_API_UPDATE.md)
- [股票系统使用指南](./STOCK_SYSTEM_GUIDE.md)

## 🎯 总结

本次更新将AiChatService中的Qwen调用方式从HTTP REST API迁移到官方DashScope SDK，并与StockAnalysisService保持一致。更新完全向后兼容，无需修改任何调用代码，同时提供了更好的类型安全性和错误处理。

---

**更新人**: AI Assistant
**更新时间**: 2025-11-27
**版本**: v1.1
**状态**: ✅ 已完成并测试
