# 二维码生成功能使用指南

## 功能介绍

本项目集成了二维码生成功能，支持生成二维码图片和Base64编码格式的二维码。使用Google ZXing库实现二维码生成。

## API接口说明

### 1. 生成二维码图片 (GET方式)

**请求URL**: `GET /api/qrcode/generate`

**请求参数**:
- `content` (必需): 二维码内容
- `width` (可选): 图片宽度，默认300
- `height` (可选): 图片高度，默认300

**响应**: PNG格式的二维码图片

**示例**:
```bash
curl -o qrcode.png "http://localhost:8879/api/qrcode/generate?content=HelloWorld&width=400&height=400"
```

### 2. 生成二维码图片 (POST方式)

**请求URL**: `POST /api/qrcode/generate`

**请求体** (JSON格式):
```json
{
  "content": "HelloWorld",
  "width": 400,
  "height": 400
}
```

**响应**: PNG格式的二维码图片

**示例**:
```bash
curl -X POST -H "Content-Type: application/json" -d '{"content":"HelloWorld","width":400,"height":400}' http://localhost:8879/api/qrcode/generate --output qrcode.png
```

### 3. 生成Base64编码的二维码 (GET方式)

**请求URL**: `GET /api/qrcode/generate-base64`

**请求参数**:
- `content` (必需): 二维码内容
- `width` (可选): 图片宽度，默认300
- `height` (可选): 图片高度，默认300

**响应**: JSON格式，包含Base64编码的二维码图片

**示例**:
```bash
curl "http://localhost:8879/api/qrcode/generate-base64?content=HelloWorld"
```

### 4. 生成Base64编码的二维码 (POST方式)

**请求URL**: `POST /api/qrcode/generate-base64`

**请求体** (JSON格式):
```json
{
  "content": "HelloWorld",
  "width": 400,
  "height": 400
}
```

**响应**: JSON格式，包含Base64编码的二维码图片

**示例**:
```bash
curl -X POST -H "Content-Type: application/json" -d '{"content":"HelloWorld","width":400,"height":400}' http://localhost:8879/api/qrcode/generate-base64
```

## 使用示例

### 前端页面测试

项目提供了一个简单的测试页面，可以直接在浏览器中访问:
[http://localhost:8879/qrcode-test.html](http://localhost:8879/qrcode-test.html)

### Java代码调用

```java
@Autowired
private QrCodeService qrCodeService;

// 生成二维码图片字节数组
byte[] qrCodeImage = qrCodeService.generateQrCode("Hello World", 300, 300);
```

## 技术实现

- 使用Google ZXing库生成二维码
- 支持自定义二维码尺寸
- 提供多种接口调用方式（GET/POST）
- 支持直接返回图片或Base64编码

## 注意事项

1. 二维码内容长度不宜过长，否则可能影响识别效果
2. 图片尺寸建议在100-1000像素之间
3. Base64编码的图片可以直接在HTML中使用，适合前端直接展示