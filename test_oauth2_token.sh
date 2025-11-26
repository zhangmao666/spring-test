#!/bin/bash
# OAuth2 Token测试脚本 (Bash)
# 测试Spring Authorization Server的/oauth2/token端点

echo "=================================="
echo "OAuth2 Token端点测试"
echo "=================================="
echo ""

BASE_URL="http://localhost:8879/api"
TOKEN_ENDPOINT="$BASE_URL/oauth2/token"

# 测试客户端凭证
CLIENT_ID="client_test_demo_001"
CLIENT_SECRET="123456"

echo "测试配置:"
echo "  端点: $TOKEN_ENDPOINT"
echo "  客户端ID: $CLIENT_ID"
echo "  客户端密钥: $CLIENT_SECRET"
echo ""

# 方法1: 使用Basic认证 (推荐)
echo "方法1: 使用HTTP Basic认证"
echo "----------------------------------------"

echo "发送请求..."
response=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
  -X POST "$TOKEN_ENDPOINT" \
  -u "$CLIENT_ID:$CLIENT_SECRET" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials")

http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
body=$(echo "$response" | sed '/HTTP_CODE:/d')

if [ "$http_code" = "200" ]; then
    echo "✓ 认证成功!"
    echo ""
    echo "响应数据:"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
    echo ""
else
    echo "✗ 请求失败! HTTP状态码: $http_code"
    echo "响应内容: $body"
fi

echo ""
echo "----------------------------------------"
echo ""

# 方法2: 使用POST参数
echo "方法2: 使用POST参数传递凭证"
echo "----------------------------------------"

echo "发送请求..."
response2=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
  -X POST "$TOKEN_ENDPOINT" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET")

http_code2=$(echo "$response2" | grep "HTTP_CODE:" | cut -d: -f2)
body2=$(echo "$response2" | sed '/HTTP_CODE:/d')

if [ "$http_code2" = "200" ]; then
    echo "✓ 认证成功!"
    echo ""
    echo "响应数据:"
    echo "$body2" | jq '.' 2>/dev/null || echo "$body2"
    echo ""
else
    echo "✗ 请求失败! HTTP状态码: $http_code2"
    echo "响应内容: $body2"
fi

echo ""
echo "=================================="
echo "测试完成!"
echo "=================================="

