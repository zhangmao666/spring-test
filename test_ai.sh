#!/bin/bash

# AI功能测试脚本
BASE_URL="http://localhost:8777/api"

echo "=== Spring Boot AI 功能测试 ==="
echo

# 1. 首先登录获取JWT Token
echo "1. 用户登录获取Token..."
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')

# 提取token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token' 2>/dev/null)

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
    echo "❌ 登录失败，无法获取token"
    echo "登录响应: $LOGIN_RESPONSE"
    exit 1
fi

echo "✅ 登录成功，获取到token: ${TOKEN:0:20}..."
echo

# 2. 测试获取AI提供商列表
echo "2. 获取AI提供商列表..."
PROVIDERS_RESPONSE=$(curl -s -X GET "${BASE_URL}/ai/providers" \
  -H "Authorization: Bearer $TOKEN")

echo "✅ AI提供商列表:"
echo $PROVIDERS_RESPONSE | jq '.' 2>/dev/null || echo $PROVIDERS_RESPONSE
echo

# 3. 测试OpenAI提供商状态
echo "3. 检查OpenAI提供商状态..."
OPENAI_STATUS=$(curl -s -X GET "${BASE_URL}/ai/providers/openai/status" \
  -H "Authorization: Bearer $TOKEN")

echo "✅ OpenAI状态:"
echo $OPENAI_STATUS | jq '.' 2>/dev/null || echo $OPENAI_STATUS
echo

# 4. 测试AI聊天功能（不需要真实API密钥也能看到结构）
echo "4. 测试AI聊天功能..."
CHAT_RESPONSE=$(curl -s -X POST "${BASE_URL}/ai/chat" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你好，请介绍一下你自己",
    "provider": "openai",
    "model": "gpt-3.5-turbo"
  }')

echo "✅ AI聊天响应:"
echo $CHAT_RESPONSE | jq '.' 2>/dev/null || echo $CHAT_RESPONSE
echo

echo "=== 测试完成 ==="
echo
echo "注意：如果要测试真实的AI对话，请在application.yml中配置有效的API密钥："
echo "  - OPENAI_API_KEY"
echo "  - QWEN_API_KEY" 
echo "  - WENXIN_API_KEY & WENXIN_SECRET_KEY"