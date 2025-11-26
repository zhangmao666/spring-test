# OAuth2 Token测试脚本 (PowerShell)
# 测试Spring Authorization Server的/oauth2/token端点

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "OAuth2 Token端点测试" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8879/api"
$tokenEndpoint = "$baseUrl/oauth2/token"

# 测试客户端凭证
$clientId = "client_test_demo_001"
$clientSecret = "123456"

Write-Host "测试配置:" -ForegroundColor Yellow
Write-Host "  端点: $tokenEndpoint" -ForegroundColor White
Write-Host "  客户端ID: $clientId" -ForegroundColor White
Write-Host "  客户端密钥: $clientSecret" -ForegroundColor White
Write-Host ""

# 方法1: 使用Basic认证 (推荐)
Write-Host "方法1: 使用HTTP Basic认证" -ForegroundColor Green
Write-Host "----------------------------------------" -ForegroundColor Gray

$basicAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${clientId}:${clientSecret}"))

$headers = @{
    "Authorization" = "Basic $basicAuth"
    "Content-Type" = "application/x-www-form-urlencoded"
}

$body = "grant_type=client_credentials"

try {
    Write-Host "发送请求..." -ForegroundColor White
    $response = Invoke-RestMethod -Uri $tokenEndpoint -Method Post -Headers $headers -Body $body
    
    Write-Host "✓ 认证成功!" -ForegroundColor Green
    Write-Host ""
    Write-Host "响应数据:" -ForegroundColor Yellow
    Write-Host ($response | ConvertTo-Json -Depth 10) -ForegroundColor White
    Write-Host ""
    
    # 保存token用于后续测试
    $global:accessToken = $response.access_token
    Write-Host "Access Token已保存到变量: `$accessToken" -ForegroundColor Cyan
    Write-Host ""
} catch {
    Write-Host "✗ 请求失败!" -ForegroundColor Red
    Write-Host "错误信息: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应内容: $responseBody" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "----------------------------------------" -ForegroundColor Gray
Write-Host ""

# 方法2: 使用POST参数
Write-Host "方法2: 使用POST参数传递凭证" -ForegroundColor Green
Write-Host "----------------------------------------" -ForegroundColor Gray

$headers2 = @{
    "Content-Type" = "application/x-www-form-urlencoded"
}

$body2 = "grant_type=client_credentials&client_id=$clientId&client_secret=$clientSecret"

try {
    Write-Host "发送请求..." -ForegroundColor White
    $response2 = Invoke-RestMethod -Uri $tokenEndpoint -Method Post -Headers $headers2 -Body $body2
    
    Write-Host "✓ 认证成功!" -ForegroundColor Green
    Write-Host ""
    Write-Host "响应数据:" -ForegroundColor Yellow
    Write-Host ($response2 | ConvertTo-Json -Depth 10) -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "✗ 请求失败!" -ForegroundColor Red
    Write-Host "错误信息: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应内容: $responseBody" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "测试完成!" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan

