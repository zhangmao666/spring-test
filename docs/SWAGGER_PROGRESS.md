# Controller Swagger注解添加说明文档

## 已完成添加@Tag注解的Controller（8个）

1. ✅ AuthController - 认证管理
2. ✅ UserController - 用户管理  
3. ✅ CourseController - 课程管理
4. ✅ FileController - 文件管理
5. ✅ AiChatController - AI聊天
6. ✅ HealthController - 系统健康检查
7. ✅ DictController - 字典管理
8. ✅ WeatherController - 天气查询

## 待添加@Tag注解的Controller（12个）

需要为以下Controller添加@Tag注解作为类级别的Swagger文档标识：

1. AiModelController - AI模型管理
2. CourseUserController - 课程用户关系
3. EnrollController - 课程选课
4. DeviceTypeController - 设备类型管理
5. LogController - 系统日志
6. QrCodeController - 二维码
7. StockController - 股票查询
8. StockWebSocketController - 股票WebSocket  
9. TaskController - 任务管理
10. TestController - 测试接口
11. OkHttpTestController - Http测试
12. ProgressController - 进度测试

## 添加步骤

为每个Controller添加：
1. 导入：`import io.swagger.v3.oas.annotations.tags.Tag;`
2. 类注解：`@Tag(name = "XX管理", description = "XX功能接口")`

## Swagger访问地址

应用启动后，访问：
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- API Docs: http://localhost:8080/v3/api-docs
