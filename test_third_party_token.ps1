# ========================================
# 第三方公司获取Token完整示例
# ========================================

$baseUrl = "http://localhost:8879/api"
$ErrorActionPreference = "Stop"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  第三方OAuth2认证测试" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# ========================================
# 方式1: 使用现有的OAuth2客户端（推荐）
# ========================================

Write-Host "【方式1】使用OAuth2标准协议获取Token" -ForegroundColor Yellow
Write-Host "----------------------------------------`n" -ForegroundColor Yellow

# 客户端凭证（需要从数据库api_clients表获取）
$clientId = "client_test_demo_001"
$clientSecret = "123456"

Write-Host "步骤1: 准备客户端凭证" -ForegroundColor Green
Write-Host "  Client ID: $clientId" -ForegroundColor Gray
Write-Host "  Client Secret: $clientSecret" -ForegroundColor Gray

# 生成Basic认证头
$credentials = "${clientId}:${clientSecret}"
$basicAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($credentials))

Write-Host "`n步骤2: 请求OAuth2 Token..." -ForegroundColor Green

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/oauth2/token" `
        -Method Post `
        -Headers @{
            "Authorization" = "Basic $basicAuth"
            "Content-Type" = "application/x-www-form-urlencoded"
        } `
        -Body "grant_type=client_credentials"
    
    $accessToken = $response.access_token
    $tokenType = $response.token_type
    $expiresIn = $response.expires_in
    $scope = $response.scope
    
    Write-Host "✓ Token获取成功！" -ForegroundColor Green
    Write-Host "`n【OAuth2 Token信息】" -ForegroundColor Cyan
    Write-Host "  Token类型: $tokenType" -ForegroundColor White
    Write-Host "  有效期: $expiresIn 秒 ($([math]::Round($expiresIn/60, 1)) 分钟)" -ForegroundColor White
    Write-Host "  权限范围: $scope" -ForegroundColor White
    Write-Host "  Access Token (前50字符): $($accessToken.Substring(0, [Math]::Min(50, $accessToken.Length)))..." -ForegroundColor White
    
    # 步骤3: 使用Token调用API
    Write-Host "`n步骤3: 使用Token调用受保护的API..." -ForegroundColor Green
    
    try {
        $apiResponse = Invoke-RestMethod -Uri "$baseUrl/weather/cities" `
            -Method Get `
            -Headers @{
                "Authorization" = "Bearer $accessToken"
            }
        
        Write-Host "✓ API调用成功！" -ForegroundColor Green
        Write-Host "`n【API响应】" -ForegroundColor Cyan
        Write-Host ($apiResponse | ConvertTo-Json -Depth 3) -ForegroundColor White
        
    } catch {
        Write-Host "✗ API调用失败" -ForegroundColor Red
        Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
        
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            Write-Host "HTTP状态码: $statusCode" -ForegroundColor Red
            
            switch ($statusCode) {
                401 { Write-Host "提示: Token可能已过期或无效" -ForegroundColor Yellow }
                403 { Write-Host "提示: 权限不足，请检查scope配置" -ForegroundColor Yellow }
            }
        }
    }
    
} catch {
    Write-Host "✗ Token获取失败" -ForegroundColor Red
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "HTTP状态码: $statusCode" -ForegroundColor Red
        
        switch ($statusCode) {
            401 { Write-Host "提示: Client ID或Secret错误" -ForegroundColor Yellow }
            400 { Write-Host "提示: 请求参数错误，检查grant_type是否为client_credentials" -ForegroundColor Yellow }
        }
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan

# ========================================
# 方式2: 使用POST参数传递（备选）
# ========================================

Write-Host "`n【方式2】使用POST参数传递凭证（备选方式）" -ForegroundColor Yellow
Write-Host "----------------------------------------`n" -ForegroundColor Yellow

Write-Host "步骤1: 构建请求参数..." -ForegroundColor Green

$postBody = @{
    grant_type = "client_credentials"
    client_id = $clientId
    client_secret = $clientSecret
}

try {
    $response2 = Invoke-RestMethod -Uri "$baseUrl/oauth2/token" `
        -Method Post `
        -ContentType "application/x-www-form-urlencoded" `
        -Body $postBody
    
    Write-Host "✓ Token获取成功（方式2）！" -ForegroundColor Green
    Write-Host "  Access Token (前50字符): $($response2.access_token.Substring(0, [Math]::Min(50, $response2.access_token.Length)))..." -ForegroundColor White
    
} catch {
    Write-Host "✗ 方式2失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan

# ========================================
# 完整代码示例（可复制使用）
# ========================================

Write-Host "`n【完整代码模板】" -ForegroundColor Yellow
Write-Host "----------------------------------------`n" -ForegroundColor Yellow

$codeExample = @"
# 第三方获取Token代码模板
`$baseUrl = "http://localhost:8879/api"
`$clientId = "your_client_id"
`$clientSecret = "your_client_secret"

# 生成Basic认证
`$basicAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("`${clientId}:`${clientSecret}"))

# 获取Token
`$tokenResponse = Invoke-RestMethod -Uri "`$baseUrl/oauth2/token" ``
    -Method Post ``
    -Headers @{
        "Authorization" = "Basic `$basicAuth"
        "Content-Type" = "application/x-www-form-urlencoded"
    } ``
    -Body "grant_type=client_credentials"

`$token = `$tokenResponse.access_token

# 调用API
`$apiResponse = Invoke-RestMethod -Uri "`$baseUrl/your-api-endpoint" ``
    -Headers @{"Authorization" = "Bearer `$token"}
"@

Write-Host $codeExample -ForegroundColor Gray

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  测试完成！" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# ========================================
# 如何获取客户端凭证
# ========================================

Write-Host "`n💡 如何获取Client ID和Secret？" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow
Write-Host "1. 联系系统管理员申请" -ForegroundColor White
Write-Host "2. 查看数据库 api_clients 表" -ForegroundColor White
Write-Host "3. 使用管理接口创建新客户端（需要管理员权限）" -ForegroundColor White
Write-Host ""
Write-Host "测试用的客户端凭证:" -ForegroundColor White
Write-Host "  Client ID: client_test_demo_001" -ForegroundColor Gray
Write-Host "  Secret: 123456" -ForegroundColor Gray
Write-Host ""
