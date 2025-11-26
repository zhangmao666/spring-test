# 天气查询接口文档

## 概述
本项目新增了天气查询接口，支持根据城市名称查询天气信息。

## 接口说明

### 1. POST 请求查询天气
**接口地址**: `POST /api/weather/query`

**请求体**:
```json
{
    "city": "北京",
    "countryCode": "CN"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "city": "北京",
        "country": "中国",
        "temperature": 18.5,
        "feelsLike": 19.2,
        "description": "阳光明媚，天气晴朗",
        "main": "晴",
        "humidity": 65,
        "pressure": 1025.3,
        "windSpeed": 3.2,
        "windDirection": 120,
        "queryTime": "2025-09-11T17:58:30"
    }
}
```

### 2. GET 请求查询天气（路径参数）
**接口地址**: `GET /api/weather/city/{cityName}`

**示例**: `GET /api/weather/city/上海`

### 3. GET 请求查询天气（查询参数）
**接口地址**: `GET /api/weather?city=广州&countryCode=CN`

### 4. 获取支持的城市列表
**接口地址**: `GET /api/weather/cities`

**响应示例**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": ["北京", "上海", "广州", "深圳", "杭州", "成都", "西安", "武汉", "南京", "天津"]
}
```

## 支持的城市
目前支持以下城市的天气查询：
- 北京
- 上海  
- 广州
- 深圳
- 杭州
- 成都
- 西安
- 武汉
- 南京
- 天津

## 测试示例

### 使用 curl 测试

1. **POST 请求测试**:
```bash
curl -X POST http://localhost:8777/api/weather/query \
  -H "Content-Type: application/json" \
  -d '{
    "city": "北京",
    "countryCode": "CN"
  }'
```

2. **GET 请求测试（路径参数）**:
```bash
curl http://localhost:8777/api/weather/city/上海
```

3. **GET 请求测试（查询参数）**:
```bash
curl "http://localhost:8777/api/weather?city=广州&countryCode=CN"
```

4. **获取支持的城市列表**:
```bash
curl http://localhost:8777/api/weather/cities
```

## 注意事项

1. **无需认证**: 天气查询接口已配置为无需JWT认证，可直接访问
2. **模拟数据**: 当前实现使用模拟数据，实际生产环境中应接入真实的天气API服务
3. **数据验证**: 接口包含输入参数验证，确保数据安全性
4. **统一响应**: 遵循项目统一的`ApiResponse`响应格式
5. **日志记录**: 所有天气查询请求都会记录日志，便于调试和监控

## 技术实现

### 架构设计
- **Controller层**: `WeatherController` - 处理HTTP请求
- **Service层**: `WeatherService` 接口及 `WeatherServiceImpl` 实现
- **DTO层**: `WeatherRequest` 和 `WeatherResponse` 数据传输对象
- **配置层**: 更新 `SecurityConfig` 允许天气接口无需认证

### 特性
- 支持多种请求方式（POST、GET）
- 完整的参数验证
- 异常处理机制
- 统一响应格式
- 详细的日志记录
- Lombok 简化代码

## 扩展建议

1. **接入真实API**: 可接入OpenWeatherMap、和风天气等真实天气API
2. **缓存机制**: 添加Redis缓存减少API调用频率
3. **更多城市**: 扩展支持更多国内外城市
4. **历史数据**: 增加天气历史数据查询功能
5. **天气预报**: 支持多天天气预报查询