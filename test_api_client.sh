#!/bin/bash

# 第三方API客户端认证测试脚本
# 使用方法: bash test_api_client.sh

# 配置
API_BASE_URL="http://localhost:8878/api"
CLIENT_ID="client_test_demo_001"
CLIENT_SECRET="123456"

echo "============================================"
echo "第三方API客户端认证测试"
echo "============================================"
echo ""

# 步骤1: 获取访问Token
echo "步骤1: 获取访问Token..."
echo "请求地址: ${API_BASE_URL}/api/auth/token"
echo ""

RESPONSE=$(curl -s -X POST "${API_BASE_URL}/api/auth/token" \
  -H "Content-Type: application/json" \
  -d "{
    \"clientId\": \"${CLIENT_ID}\",
    \"clientSecret\": \"${CLIENT_SECRET}\"
  }")

echo "响应结果:"
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""

# 提取accessToken
ACCESS_TOKEN=$(echo "$RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ 获取Token失败！"
    exit 1
fi

echo "✅ 成功获取Token!"
echo "Token前缀: ${ACCESS_TOKEN:0:30}..."
echo ""

# 步骤2: 使用Token访问受保护的API
echo "============================================"
echo "步骤2: 使用Token访问受保护的API"
echo "============================================"
echo ""

echo "测试接口: ${API_BASE_URL}/files/list"
API_RESPONSE=$(curl -s -X GET "${API_BASE_URL}/files/list" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}")

echo "响应结果:"
echo "$API_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$API_RESPONSE"
echo ""

if echo "$API_RESPONSE" | grep -q '"code":200'; then
    echo "✅ API调用成功！"
else
    echo "⚠️  API调用返回非200状态"
fi

echo ""
echo "============================================"
echo "测试完成"
echo "============================================"

