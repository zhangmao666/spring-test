# 股票实时分析功能 - 快速入门

## ✨ 新增功能

本次更新添加了完整的股票实时分析系统，包括：

- 📈 **实时行情** - 东方财富/新浪财经数据源
- 🤖 **AI智能分析** - 趋势判断、价格预测、风险评估
- 🔔 **WebSocket推送** - 实时行情和分析结果推送
- ⏰ **定时任务** - 自动获取行情（5秒/次）和AI分析（15分钟/次）
- 👀 **用户关注** - 自选股管理和价格提醒

## 🚀 快速开始

### 1. 初始化数据库

```bash
mysql -u admin -p spring_boot_test < sql/stock_system.sql
```

### 2. 配置AI服务（可选）

如需使用AI分析功能，请设置环境变量：

```bash
# 通义千问（推荐）
export QWEN_API_KEY=sk-xxxxx

# 或 OpenAI
export OPENAI_API_KEY=sk-xxxxx
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

### 4. 测试接口

```bash
# 1. 登录获取Token
TOKEN=$(curl -X POST "http://localhost:8879/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' \
  | jq -r '.data.token')

# 2. 获取股票行情
curl -X GET "http://localhost:8879/api/stock/quote/sh600000" \
  -H "Authorization: Bearer $TOKEN"

# 3. 请求AI分析
curl -X GET "http://localhost:8879/api/stock/analysis/sh600000" \
  -H "Authorization: Bearer $TOKEN"

# 4. 关注股票
curl -X POST "http://localhost:8879/api/stock/watch/sh600000" \
  -H "Authorization: Bearer $TOKEN"

# 5. 查看关注列表
curl -X GET "http://localhost:8879/api/stock/watch" \
  -H "Authorization: Bearer $TOKEN"
```

## 📁 新增文件列表

### 配置文件
- `sql/stock_system.sql` - 数据库表结构和初始数据

### 实体类（4个）
- `modules/stock/entity/Stock.java` - 股票基本信息
- `modules/stock/entity/StockQuote.java` - 实时行情
- `modules/stock/entity/StockAnalysis.java` - AI分析记录
- `modules/stock/entity/UserStockWatch.java` - 用户关注

### Repository（4个）
- `modules/stock/repository/StockRepository.java`
- `modules/stock/repository/StockQuoteRepository.java`
- `modules/stock/repository/StockAnalysisRepository.java`
- `modules/stock/repository/UserStockWatchRepository.java`

### DTO（4个）
- `modules/stock/dto/StockInfoDTO.java`
- `modules/stock/dto/StockQuoteDTO.java`
- `modules/stock/dto/StockAnalysisDTO.java`
- `modules/stock/dto/UserStockWatchDTO.java`

### 枚举（3个）
- `modules/stock/enums/StockMarket.java` - 市场类型
- `modules/stock/enums/TrendType.java` - 趋势类型
- `modules/stock/enums/RiskLevel.java` - 风险等级

### 配置类（3个）
- `modules/stock/config/WebSocketConfig.java` - WebSocket配置
- `modules/stock/config/StockScheduleConfig.java` - 定时任务配置
- `modules/stock/config/StockProperties.java` - 股票配置属性

### 服务类（4个）
- `modules/stock/service/StockDataService.java` - 股票数据获取
- `modules/stock/service/StockAnalysisService.java` - AI分析
- `modules/stock/service/StockQuoteService.java` - 行情服务
- `modules/stock/service/StockWatchService.java` - 关注服务

### 控制器（2个）
- `modules/stock/controller/StockController.java` - REST API
- `modules/stock/controller/StockWebSocketController.java` - WebSocket推送

### 定时任务（1个）
- `modules/stock/task/StockDataFetchTask.java` - 定时获取行情和分析

### 文档
- `docs/STOCK_SYSTEM_GUIDE.md` - 完整使用指南

**总计：** 约25个文件

## 🔧 配置说明

新增配置位于 `application-dev.yml`：

```yaml
stock:
  data:
    source: sina                    # 数据源
    fetch-interval: 5               # 获取间隔（秒）
    schedule-enabled: true          # 启用定时任务

  analysis:
    interval: 15                    # 分析间隔（分钟）
    provider: qwen                  # AI提供商
    enabled: true                   # 启用AI分析
```

## 🎯 核心API

| 接口 | 方法 | 描述 |
|------|------|------|
| `/stock/quote/{code}` | GET | 获取实时行情 |
| `/stock/quote/{code}/history` | GET | 获取历史行情 |
| `/stock/analysis/{code}` | GET | AI分析 |
| `/stock/watch/{code}` | POST | 关注股票 |
| `/stock/watch/{code}` | DELETE | 取消关注 |
| `/stock/watch` | GET | 关注列表 |
| `/stock/search` | GET | 搜索股票 |

## 📡 WebSocket订阅

```javascript
// 连接
const socket = new SockJS('http://localhost:8879/api/ws/stock');
const client = Stomp.over(socket);

client.connect({}, function() {
    // 订阅实时行情
    client.subscribe('/topic/stock/sh600000', function(msg) {
        console.log('行情推送:', JSON.parse(msg.body));
    });

    // 订阅AI分析
    client.subscribe('/topic/analysis/sh600000', function(msg) {
        console.log('分析推送:', JSON.parse(msg.body));
    });
});
```

## ⏰ 定时任务

系统会在交易时段（周一至周五 9:30-15:00）自动执行：

- **每5秒** - 获取所有股票实时行情并推送
- **每15分钟** - 执行AI分析并推送结果
- **每天凌晨2点** - 清理30天前的历史数据

## 🗄️ 数据库表

系统创建了4张新表：

1. **stocks** - 股票基本信息（已预置8只股票）
2. **stock_quotes** - 实时行情记录
3. **stock_analysis** - AI分析记录
4. **user_stock_watch** - 用户关注列表

初始数据包含：
- 浦发银行（sh600000）
- 招商银行（sh600036）
- 贵州茅台（sh600519）
- 中国平安（sh601318）
- 平安银行（sz000001）
- 万科A（sz000002）
- 五粮液（sz000858）
- 宁德时代（sz300750）

## 📊 功能特性

### 1. 实时行情
- 数据源：新浪财经免费API
- 更新频率：5秒/次（交易时段）
- 缓存策略：5秒过期
- WebSocket实时推送

### 2. AI智能分析
- AI模型：OpenAI GPT / 通义千问
- 分析维度：
  - 趋势判断（UP/DOWN/STABLE）
  - 置信度评分（0-100）
  - 价格预测
  - 风险等级（LOW/MEDIUM/HIGH）
- 分析频率：15分钟/次
- 基于最近20个交易日数据

### 3. 用户关注
- 添加/移除自选股
- 设置价格提醒
- 查看关注列表（含最新行情和分析）

### 4. 技术栈
- Spring Boot 3.2.0
- WebSocket + STOMP
- MyBatis-Plus
- Caffeine缓存
- OkHttp客户端
- JWT认证

## 🔒 安全性

- 所有API需要JWT认证
- 用户只能访问自己的关注列表
- WebSocket支持跨域配置
- SQL注入防护（MyBatis-Plus）

## 📖 详细文档

完整使用指南请查看：[docs/STOCK_SYSTEM_GUIDE.md](docs/STOCK_SYSTEM_GUIDE.md)

包含：
- 详细API文档
- WebSocket连接示例
- 故障排查指南
- 性能优化建议

## 🐛 常见问题

### Q: 无法获取行情数据？
A: 检查股票代码格式是否正确（sh开头表示上海，sz开头表示深圳），或查看数据库中是否有该股票记录。

### Q: AI分析失败？
A: 确保已配置AI API密钥，且股票有足够的历史行情数据（至少20条）。

### Q: WebSocket连接失败？
A: 检查CORS配置，确保 `allowed-origins` 包含前端域名。

### Q: 定时任务没有执行？
A: 检查当前时间是否在交易时段（9:30-15:00），且 `schedule-enabled` 为 `true`。

## 📝 更新日志

### v1.0.0 (2025-11-27)
- ✨ 新增股票实时行情功能
- ✨ 新增AI智能分析功能
- ✨ 新增WebSocket实时推送
- ✨ 新增用户关注列表
- ✨ 新增定时任务自动更新
- 📝 完善文档和使用指南

---

**需要帮助？** 查看完整文档或提交Issue
