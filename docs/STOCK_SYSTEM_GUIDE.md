# 股票实时分析系统使用指南

## 📋 功能概述

股票实时分析系统是一个基于Spring Boot 3.2.0的企业级股票数据分析平台，提供以下核心功能：

- ✅ 实时行情展示（价格、涨跌幅、成交量等）
- ✅ WebSocket实时推送
- ✅ AI智能分析（趋势判断、价格预测、风险评估）
- ✅ 用户股票关注列表
- ✅ 价格提醒功能
- ✅ 定时任务自动更新

## 🚀 快速开始

### 1. 数据库初始化

执行SQL脚本创建数据库表：

```bash
mysql -u admin -p spring_boot_test < sql/stock_system.sql
```

脚本会创建以下表：
- `stocks` - 股票基本信息表
- `stock_quotes` - 实时行情表
- `stock_analysis` - AI分析记录表
- `user_stock_watch` - 用户关注股票表

### 2. 配置说明

在 `application-dev.yml` 中已添加股票模块配置：

```yaml
stock:
  data:
    source: sina                    # 数据源：sina（新浪财经）
    fetch-interval: 5               # 行情获取间隔（秒）
    trading-hours:
      start: "09:30"                # 交易开始时间
      end: "15:00"                  # 交易结束时间
    schedule-enabled: true          # 是否启用定时任务

  websocket:
    allowed-origins: "*"            # WebSocket跨域配置
    message-size-limit: 128         # 消息大小限制（KB）

  analysis:
    interval: 15                    # AI分析间隔（分钟）
    provider: qwen                  # AI提供商（openai/qwen）
    history-window: 7               # 历史数据窗口（天）
    enabled: true                   # 是否启用AI分析
```

### 3. 启动应用

```bash
mvn clean install
mvn spring-boot:run
```

应用启动后会自动：
- 每5秒获取一次实时行情（交易时段）
- 每15分钟执行一次AI分析（交易时段）
- 通过WebSocket推送实时数据

## 📡 API接口文档

### 基础路径
```
http://localhost:8879/api/stock
```

所有接口需要JWT认证，请在请求头中添加：
```
Authorization: Bearer <your-jwt-token>
```

### 1. 获取股票实时行情

**请求：**
```http
GET /stock/quote/{stockCode}
```

**示例：**
```bash
curl -X GET "http://localhost:8879/api/stock/quote/sh600000" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "stockCode": "sh600000",
    "stockName": "浦发银行",
    "currentPrice": 8.35,
    "openPrice": 8.33,
    "closePrice": 8.34,
    "highPrice": 8.40,
    "lowPrice": 8.30,
    "volume": 12345678,
    "turnover": 103086515.00,
    "changeAmount": 0.01,
    "changePercent": 0.12,
    "quoteTime": "2025-11-27 14:30:00"
  }
}
```

### 2. 获取历史行情

**请求：**
```http
GET /stock/quote/{stockCode}/history?startDate=2025-11-01&endDate=2025-11-27
```

**示例：**
```bash
curl -X GET "http://localhost:8879/api/stock/quote/sh600000/history?startDate=2025-11-01&endDate=2025-11-27" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. 获取AI分析结果

**请求：**
```http
GET /stock/analysis/{stockCode}
```

**示例：**
```bash
curl -X GET "http://localhost:8879/api/stock/analysis/sh600000" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "stockCode": "sh600000",
    "stockName": "浦发银行",
    "analysisTime": "2025-11-27 14:30:00",
    "analysisContent": "根据最近20个交易日数据分析...",
    "trendType": "UP",
    "confidenceScore": 75.5,
    "predictedPrice": 8.50,
    "riskLevel": "MEDIUM",
    "aiProvider": "qwen"
  }
}
```

### 4. 关注股票

**请求：**
```http
POST /stock/watch/{stockCode}
```

**示例：**
```bash
curl -X POST "http://localhost:8879/api/stock/watch/sh600000" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 5. 取消关注

**请求：**
```http
DELETE /stock/watch/{stockCode}
```

### 6. 获取关注列表

**请求：**
```http
GET /stock/watch
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "stockCode": "sh600000",
      "stockName": "浦发银行",
      "watchTime": "2025-11-27 10:00:00",
      "alertEnabled": 1,
      "alertPrice": 8.50,
      "alertType": "UP",
      "latestQuote": { /* 最新行情 */ },
      "latestAnalysis": { /* 最新分析 */ }
    }
  ]
}
```

### 7. 设置价格提醒

**请求：**
```http
POST /stock/watch/{stockCode}/alert?alertPrice=8.50&alertType=UP
```

**参数：**
- `alertPrice`: 提醒价格
- `alertType`: 提醒类型（UP-涨到，DOWN-跌到）

### 8. 搜索股票

**请求：**
```http
GET /stock/search?keyword=银行
```

### 9. 获取所有股票

**请求：**
```http
GET /stock/list
```

## 🔌 WebSocket连接

### 连接地址
```
ws://localhost:8879/api/ws/stock
```

### 前端连接示例（使用SockJS和STOMP）

```javascript
// 1. 引入依赖
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>

// 2. 建立连接
const socket = new SockJS('http://localhost:8879/api/ws/stock');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('WebSocket连接成功');

    // 订阅股票实时行情
    stompClient.subscribe('/topic/stock/sh600000', function(message) {
        const quote = JSON.parse(message.body);
        console.log('收到行情推送:', quote);
        // 更新UI
        updateStockQuote(quote);
    });

    // 订阅AI分析结果
    stompClient.subscribe('/topic/analysis/sh600000', function(message) {
        const analysis = JSON.parse(message.body);
        console.log('收到分析推送:', analysis);
        // 更新UI
        updateAnalysis(analysis);
    });
});

// 3. 断开连接
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log('WebSocket已断开');
}
```

### 订阅主题说明

| 主题 | 描述 | 推送频率 |
|------|------|---------|
| `/topic/stock/{stockCode}` | 实时行情推送 | 每5秒（交易时段） |
| `/topic/analysis/{stockCode}` | AI分析结果推送 | 每15分钟（交易时段） |
| `/queue/alert/{userId}` | 价格提醒推送 | 触发时 |

## 📊 数据字典

### 趋势类型（TrendType）

| 代码 | 描述 | 说明 |
|------|------|------|
| UP | 上涨趋势 | 看涨 |
| DOWN | 下跌趋势 | 看跌 |
| STABLE | 平稳震荡 | 中性 |

### 风险等级（RiskLevel）

| 代码 | 名称 | 建议 |
|------|------|------|
| LOW | 低风险 | 适合稳健投资者 |
| MEDIUM | 中等风险 | 需谨慎关注 |
| HIGH | 高风险 | 不建议投资 |

### 市场类型（StockMarket）

| 代码 | 名称 | 前缀 |
|------|------|------|
| SH | 上海证券交易所 | sh |
| SZ | 深圳证券交易所 | sz |

## 🔧 定时任务说明

系统配置了以下定时任务：

### 1. 实时行情获取任务
- **Cron表达式：** `0/5 * 9-15 * * MON-FRI`
- **执行频率：** 每5秒一次
- **执行时间：** 周一至周五 9:00-15:59
- **实际限制：** 仅在 09:30-15:00 执行（交易时段）
- **功能：** 获取所有活跃股票的实时行情并推送

### 2. AI分析任务
- **Cron表达式：** `0 0/15 9-15 * * MON-FRI`
- **执行频率：** 每15分钟一次
- **执行时间：** 周一至周五 9:00-15:59
- **实际限制：** 仅在 09:30-15:00 执行（交易时段）
- **功能：** 对所有活跃股票进行AI分析并推送结果

### 3. 数据清理任务
- **Cron表达式：** `0 0 2 * * ?`
- **执行频率：** 每天凌晨2点
- **功能：** 清理30天前的历史行情数据和90天前的分析记录

## 🎯 使用场景示例

### 场景1：监控自选股实时行情

1. 登录系统获取JWT Token
2. 关注股票：`POST /stock/watch/sh600000`
3. 建立WebSocket连接
4. 订阅行情推送：`/topic/stock/sh600000`
5. 实时接收行情更新

### 场景2：获取AI分析建议

1. 调用分析接口：`GET /stock/analysis/sh600000`
2. 系统调用AI模型分析最近20个交易日数据
3. 返回趋势判断、价格预测和风险评估
4. 可通过WebSocket订阅后续分析推送

### 场景3：设置价格提醒

1. 关注股票：`POST /stock/watch/sh600519`
2. 设置提醒：`POST /stock/watch/sh600519/alert?alertPrice=1800&alertType=UP`
3. 当股票价格涨到1800元时，系统推送提醒消息

## 🔍 故障排查

### 1. 无法获取行情数据

**可能原因：**
- 新浪财经API访问受限
- 股票代码格式错误
- 数据库中无对应股票记录

**解决方案：**
```bash
# 检查股票是否存在
mysql> SELECT * FROM stocks WHERE stock_code = 'sh600000';

# 手动插入股票数据
INSERT INTO stocks (stock_code, stock_name, market, industry, status)
VALUES ('sh600000', '浦发银行', 'SH', '银行', 1);
```

### 2. WebSocket连接失败

**可能原因：**
- CORS配置问题
- 防火墙拦截
- 端口未开放

**解决方案：**
```yaml
# 检查配置
stock:
  websocket:
    allowed-origins: "*"  # 开发环境允许所有源
```

### 3. AI分析失败

**可能原因：**
- AI服务未配置API Key
- 行情数据不足（少于20条）
- AI服务调用超时

**解决方案：**
```bash
# 设置环境变量
export QWEN_API_KEY=sk-xxxxx

# 检查行情数据
mysql> SELECT COUNT(*) FROM stock_quotes WHERE stock_id = 1;
```

### 4. 定时任务未执行

**可能原因：**
- 定时任务被禁用
- 不在交易时段

**解决方案：**
```yaml
# 检查配置
stock:
  data:
    schedule-enabled: true  # 确保启用
```

## 📈 性能优化建议

### 1. 缓存优化
- 实时行情缓存5秒
- AI分析结果缓存15分钟
- 股票信息缓存1小时

### 2. 数据库优化
```sql
-- 创建复合索引
CREATE INDEX idx_stock_quote_time ON stock_quotes(stock_id, quote_time DESC);
CREATE INDEX idx_analysis_stock_time ON stock_analysis(stock_id, analysis_time DESC);

-- 定期清理历史数据
CALL sp_clean_old_quotes();      -- 清理30天前行情
CALL sp_clean_old_analysis();    -- 清理90天前分析
```

### 3. WebSocket连接池
```yaml
server:
  tomcat:
    max-connections: 11000  # 最大连接数
    threads:
      max: 200              # 最大线程数
```

## 🔐 安全建议

1. **生产环境配置：**
   - 修改WebSocket允许的源为具体域名
   - 使用HTTPS和WSS协议
   - 启用JWT Token过期机制

2. **API限流：**
   - 每用户每分钟最多60次请求
   - WebSocket连接数限制

3. **数据脱敏：**
   - 用户关注列表仅返回自己的数据
   - AI分析提示词中移除敏感信息

## 📞 技术支持

如遇到问题，请查看日志：

```bash
tail -f logs/spring-boot-test.log
```

日志级别配置：
```yaml
logging:
  level:
    com.example.springboottest.modules.stock: DEBUG
    org.springframework.web.socket: DEBUG
```

---

**版本：** 1.0.0
**更新时间：** 2025-11-27
**作者：** Spring Boot Team
