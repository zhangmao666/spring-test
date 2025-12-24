# Qwen API调用方式更新说明

## 📝 更新内容

已将股票AI分析服务中的Qwen调用方式从原有的通用AiChatService改为直接使用**阿里云DashScope SDK**，参照TestQwen中的实现方式。

## 🔄 主要变化

### 修改前
```java
// 使用通用的AiChatService
AiChatRequest request = AiChatRequest.builder()
    .message(prompt)
    .provider("qwen")
    .build();

return aiChatService.chat(request)
    .thenApply(response -> processAnalysisResult(...));
```

### 修改后
```java
// 直接使用DashScope SDK
Generation gen = new Generation();

Message systemMsg = Message.builder()
    .role(Role.SYSTEM.getValue())
    .content("你是一位专业的股票分析师，擅长技术分析和基本面分析。")
    .build();

Message userMsg = Message.builder()
    .role(Role.USER.getValue())
    .content(prompt)
    .build();

GenerationParam param = GenerationParam.builder()
    .apiKey(aiProperties.getQwen().getApiKey())
    .model(aiProperties.getQwen().getModel())
    .messages(Arrays.asList(systemMsg, userMsg))
    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
    .temperature(Float.valueOf(String.valueOf(aiProperties.getQwen().getTemperature())))
    .maxTokens(aiProperties.getQwen().getMaxTokens())
    .build();

GenerationResult result = gen.call(param);
String analysisContent = result.getOutput().getChoices().get(0).getMessage().getContent();
```

## ✅ 优势

1. **更可靠** - 使用官方SDK，遵循阿里云标准调用方式
2. **更稳定** - 直接调用，减少中间层
3. **类型安全** - 使用强类型Message和GenerationParam
4. **错误处理** - 更精确的异常类型（NoApiKeyException, ApiException等）

## 🔧 配置说明

### 环境变量方式（推荐）
```bash
export QWEN_API_KEY=sk-your-api-key
```

### 配置文件方式
在 `application-dev.yml` 中配置：
```yaml
ai:
  qwen:
    api-key: sk-your-api-key
    model: qwen-max              # 可选：qwen-turbo, qwen-plus, qwen-max
    max-tokens: 100000
    temperature: 0.7
```

## 📦 依赖

项目已包含DashScope SDK依赖（pom.xml）：
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>dashscope-sdk-java</artifactId>
    <version>2.22.2</version>
</dependency>
```

## 🎯 影响范围

### 修改的文件
- `StockAnalysisService.java` - 股票AI分析服务

### 新增依赖
- `com.alibaba.dashscope.aigc.generation.Generation`
- `com.alibaba.dashscope.aigc.generation.GenerationParam`
- `com.alibaba.dashscope.aigc.generation.GenerationResult`
- `com.alibaba.dashscope.common.Message`
- `com.alibaba.dashscope.common.Role`
- `com.alibaba.dashscope.exception.*`

### 移除依赖
- `com.example.springboottest.service.AiChatService` （在StockAnalysisService中）
- `com.example.springboottest.entity.DTO.AiChatRequest`
- `com.example.springboottest.entity.DTO.AiChatResponse`

## 🧪 测试建议

### 1. 单元测试
```bash
# 测试Qwen API调用
mvn test -Dtest=StockAnalysisServiceTest
```

### 2. 集成测试
```bash
# 1. 确保配置了QWEN_API_KEY
export QWEN_API_KEY=sk-xxxxx

# 2. 启动应用
mvn spring-boot:run

# 3. 测试AI分析接口
TOKEN=$(curl -X POST "http://localhost:8879/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' \
  | jq -r '.data.token')

curl -X GET "http://localhost:8879/api/stock/analysis/sh600000" \
  -H "Authorization: Bearer $TOKEN"
```

### 3. 验证响应格式
期望的AI响应格式：
```
1. 趋势判断：[UP]
2. 置信度：[75]
3. 预测价格：[8.50]
4. 风险等级：[MEDIUM]
5. 分析说明：根据最近20个交易日的数据分析...
```

## 🔍 故障排查

### 问题1：NoApiKeyException
**原因：** 未配置API Key

**解决：**
```bash
export QWEN_API_KEY=sk-your-api-key
# 或在application-dev.yml中配置
```

### 问题2：ApiException (400/401/403)
**原因：** API Key无效或权限不足

**解决：**
1. 检查API Key是否正确
2. 确认API Key是否已激活
3. 检查账户余额

### 问题3：Qwen API返回结果为空
**原因：** 网络问题或API响应格式变化

**解决：**
1. 检查网络连接
2. 查看日志中的完整响应
3. 确认使用的模型是否可用（qwen-max, qwen-plus, qwen-turbo）

### 问题4：Temperature类型错误
**原因：** temperature参数需要Float类型

**解决：**
```java
// 已修复，使用类型转换
.temperature(Float.valueOf(String.valueOf(aiProperties.getQwen().getTemperature())))
```

## 📚 参考文档

- [阿里云DashScope SDK文档](https://help.aliyun.com/zh/model-studio/developer-reference/sdk-overview)
- [通义千问API文档](https://help.aliyun.com/zh/model-studio/getting-started/models)
- [TestQwen示例代码](../src/main/java/com/example/springboottest/test/TestQwen.java)

## 🔄 版本历史

### v1.1 (2025-11-27)
- 重写StockAnalysisService，使用DashScope SDK
- 移除对AiChatService的依赖
- 添加system角色消息设置专业背景
- 优化异常处理和日志记录

### v1.0 (2025-11-27)
- 初始版本，使用AiChatService

---

**更新人：** AI Assistant
**更新时间：** 2025-11-27
**状态：** ✅ 已完成并测试
