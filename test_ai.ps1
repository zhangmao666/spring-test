# AI功能测试脚本 (PowerShell版本)
$BaseUrl = "http://localhost:8777/api"

Write-Host "=== Spring Boot AI 功能测试 ===" -ForegroundColor Green
Write-Host

# 1. 首先登录获取JWT Token
Write-Host "1. 用户登录获取Token..." -ForegroundColor Yellow

$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.data.token
    
    if ($token) {
        Write-Host "✅ 登录成功，获取到token: $($token.Substring(0, [Math]::Min(20, $token.Length)))..." -ForegroundColor Green
    } else {
        Write-Host "❌ 登录失败，无法获取token" -ForegroundColor Red
        Write-Host "登录响应: $($loginResponse | ConvertTo-Json)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ 登录请求失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host

# 2. 测试获取AI提供商列表
Write-Host "2. 获取AI提供商列表..." -ForegroundColor Yellow

try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    
    $providersResponse = Invoke-RestMethod -Uri "$BaseUrl/ai/providers" -Method Get -Headers $headers
    Write-Host "✅ AI提供商列表:" -ForegroundColor Green
    Write-Host ($providersResponse | ConvertTo-Json -Depth 3)
} catch {
    Write-Host "❌ 获取提供商列表失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host

# 3. 测试OpenAI提供商状态
Write-Host "3. 检查OpenAI提供商状态..." -ForegroundColor Yellow

try {
    $openaiStatusResponse = Invoke-RestMethod -Uri "$BaseUrl/ai/providers/openai/status" -Method Get -Headers $headers
    Write-Host "✅ OpenAI状态:" -ForegroundColor Green
    Write-Host ($openaiStatusResponse | ConvertTo-Json)
} catch {
    Write-Host "❌ 检查OpenAI状态失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host

# 4. 测试AI聊天功能
Write-Host "4. 测试AI聊天功能..." -ForegroundColor Yellow

$chatBody = @{
    message = "你好，请介绍一下你自己"
    provider = "openai"
    model = "gpt-3.5-turbo"
} | ConvertTo-Json

try {
    $chatResponse = Invoke-RestMethod -Uri "$BaseUrl/ai/chat" -Method Post -Body $chatBody -ContentType "application/json" -Headers $headers
    Write-Host "✅ AI聊天响应:" -ForegroundColor Green
    Write-Host ($chatResponse | ConvertTo-Json -Depth 3)
} catch {
    Write-Host "❌ AI聊天请求失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "错误详情: $errorBody" -ForegroundColor Red
    }
}

Write-Host
Write-Host "=== 测试完成 ===" -ForegroundColor Green
Write-Host
Write-Host "注意：如果要测试真实的AI对话，请在application.yml中配置有效的API密钥：" -ForegroundColor Cyan
Write-Host "  - OPENAI_API_KEY" -ForegroundColor Cyan
Write-Host "  - QWEN_API_KEY" -ForegroundColor Cyan
Write-Host "  - WENXIN_API_KEY and WENXIN_SECRET_KEY" -ForegroundColor Cyan