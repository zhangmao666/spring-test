# 支付宝/微信转账功能使用指南

## 概述

本模块提供了支付宝和微信支付的转账功能，支持向指定账户进行资金转账。

## 功能特性

- **支付宝转账**: 通过支付宝账号向用户转账
- **微信转账**: 通过微信OpenID向用户转账
- **订单查询**: 查询转账订单状态
- **异步通知**: 接收支付平台的转账结果通知

## 快速开始

### 1. 执行数据库脚本

```bash
# 执行SQL脚本创建转账订单表
mysql -u username -p database_name < sql/transfer_order.sql
```

### 2. 配置支付参数

在 `application-dev.yml` 或环境变量中配置支付参数：

#### 支付宝配置

```yaml
payment:
  alipay:
    app-id: 你的支付宝应用ID
    private-key: 你的商户私钥
    alipay-public-key: 支付宝公钥
    gateway-url: https://openapi.alipay.com/gateway.do  # 正式环境
    notify-url: http://your-domain/api/payment/notify/alipay
    return-url: http://your-domain/payment/result
```

#### 微信支付配置

```yaml
payment:
  wechat:
    merchant-id: 商户号
    private-key: 商户API私钥内容
    merchant-serial-number: 商户证书序列号
    api-v3-key: APIv3密钥
    app-id: 应用ID
    notify-url: http://your-domain/api/payment/notify/wechat
```

### 3. 更新Maven依赖

```bash
mvn clean install
```

## API接口

### 发起转账

**POST** `/api/payment/transfer`

**请求体:**
```json
{
    "channel": "alipay",
    "amount": 100.00,
    "payeeAccount": "收款人支付宝账号",
    "payeeName": "收款人姓名",
    "remark": "转账备注"
}
```

**参数说明:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| channel | String | 是 | 支付渠道: alipay/wechat |
| amount | BigDecimal | 是 | 转账金额(0.01-50000元) |
| payeeAccount | String | 是 | 收款账户(支付宝账号或微信OpenID) |
| payeeName | String | 是 | 收款人真实姓名 |
| remark | String | 否 | 转账备注(最长200字符) |

**响应示例:**
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "orderId": 1,
        "outTradeNo": "TF20231225120000ABCD1234",
        "tradeNo": "20231225...",
        "channel": "alipay",
        "amount": 100.00,
        "payeeAccount": "test@alipay.com",
        "payeeName": "张三",
        "status": 2,
        "statusDesc": "成功",
        "createTime": "2023-12-25 12:00:00",
        "finishTime": "2023-12-25 12:00:05"
    }
}
```

### 查询转账订单

**GET** `/api/payment/query/{outTradeNo}`

**响应示例:**
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "orderId": 1,
        "outTradeNo": "TF20231225120000ABCD1234",
        "status": 2,
        "statusDesc": "成功"
    }
}
```

### 查询转账订单列表

**GET** `/api/payment/orders`

**查询参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| outTradeNo | String | 否 | 商户订单号 |
| channel | String | 否 | 支付渠道 |
| status | Integer | 否 | 订单状态 |
| pageNum | Integer | 否 | 页码(默认1) |
| pageSize | Integer | 否 | 每页数量(默认10) |

## 转账状态说明

| 状态码 | 状态 | 说明 |
|--------|------|------|
| 0 | PENDING | 待处理 |
| 1 | PROCESSING | 处理中 |
| 2 | SUCCESS | 成功 |
| 3 | FAILED | 失败 |
| 4 | CLOSED | 已关闭 |

## 获取支付凭证

### 支付宝

1. 登录[支付宝开放平台](https://open.alipay.com/)
2. 创建应用并获取AppID
3. 在"开发设置"中配置密钥
4. 申请"单笔转账到支付宝账户"产品权限

### 微信支付

1. 登录[微信支付商户平台](https://pay.weixin.qq.com/)
2. 获取商户号(mch_id)
3. 在"API安全"中设置APIv3密钥
4. 申请API证书并获取证书序列号
5. 申请"商家转账到零钱"产品权限

## 注意事项

1. **实名认证**: 转账功能需要完成企业实名认证
2. **产品签约**: 需要在支付平台签约相关转账产品
3. **金额限制**: 单笔转账金额限制为0.01-50000元
4. **安全配置**: 私钥等敏感信息建议使用环境变量配置
5. **回调地址**: notify-url必须是公网可访问的HTTPS地址
6. **沙箱测试**: 建议先在沙箱环境测试，再切换到正式环境

## 项目结构

```
modules/payment/
├── config/
│   ├── AlipayProperties.java      # 支付宝配置属性
│   ├── WechatPayProperties.java   # 微信支付配置属性
│   └── PaymentConfig.java         # 支付配置类
├── controller/
│   └── PaymentController.java     # 支付控制器
├── dto/
│   ├── TransferRequest.java       # 转账请求DTO
│   ├── TransferResponse.java      # 转账响应DTO
│   └── TransferQueryRequest.java  # 查询请求DTO
├── entity/
│   └── TransferOrder.java         # 转账订单实体
├── enums/
│   ├── PaymentChannel.java        # 支付渠道枚举
│   └── TransferStatus.java        # 转账状态枚举
├── repository/
│   └── TransferOrderRepository.java # 数据访问层
└── service/
    ├── PaymentService.java        # 支付服务接口
    ├── PaymentServiceImpl.java    # 支付服务实现
    ├── AlipayTransferService.java # 支付宝转账服务
    └── WechatTransferService.java # 微信转账服务
```

## 常见问题

### Q: 为什么转账一直是处理中状态？
A: 微信转账是异步的，需要等待微信处理完成后通过回调通知结果，或主动查询订单状态。

### Q: 支付宝转账失败提示"收款方账户不存在"？
A: 请确认收款人支付宝账号正确，且收款人姓名与支付宝实名认证的姓名一致。

### Q: 如何在沙箱环境测试？
A: 
- 支付宝: 将gateway-url改为 `https://openapi-sandbox.dl.alipaydev.com/gateway.do`
- 微信支付: 微信支付暂无沙箱环境，建议使用小额测试

## 技术支持

如有问题，请参考官方文档：
- [支付宝开放平台文档](https://opendocs.alipay.com/open/02byuo)
- [微信支付开发文档](https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter4_3_1.shtml)
