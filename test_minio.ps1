# MinIO 文件服务测试脚本

# 配置变量
$baseUrl = "http://localhost:8777/api"
$loginUrl = "$baseUrl/auth/login"
$filesUrl = "$baseUrl/files"

# 测试用户凭据
$loginData = @{
    username = "admin"
    password = "123456"
} | ConvertTo-Json

Write-Host "=== MinIO 文件服务集成测试 ===" -ForegroundColor Green

# 1. 用户登录获取Token
Write-Host "`n1. 正在登录获取访问令牌..." -ForegroundColor Yellow
try {
    $loginResponse = Invoke-RestMethod -Uri $loginUrl -Method POST -Body $loginData -ContentType "application/json"
    if ($loginResponse.code -eq 200) {
        $token = $loginResponse.data.token
        Write-Host "   登录成功，令牌获取完成" -ForegroundColor Green
    } else {
        Write-Host "   登录失败: $($loginResponse.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "   登录请求失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   请确保服务器正在运行在 http://localhost:8777" -ForegroundColor Yellow
    exit 1
}

# 设置认证头
$headers = @{
    "Authorization" = "Bearer $token"
    "Accept" = "application/json"
}

# 2. 测试文件上传 (创建一个测试文件)
Write-Host "`n2. 正在测试文件上传..." -ForegroundColor Yellow

# 创建测试文件
$testContent = @"
这是一个MinIO集成测试文件
创建时间: $(Get-Date)
用途: 验证文件上传下载功能

测试内容:
- 文件上传功能
- 文件下载功能  
- 文件列表功能
- 文件删除功能
"@

$testFileName = "test-file-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"
$testFilePath = "$env:TEMP\$testFileName"
$testContent | Out-File -FilePath $testFilePath -Encoding UTF8

try {
    # 使用 multipart/form-data 上传文件
    $boundary = [System.Guid]::NewGuid().ToString()
    $LF = "`r`n"
    
    $fileBytes = [System.IO.File]::ReadAllBytes($testFilePath)
    $fileName = [System.IO.Path]::GetFileName($testFilePath)
    
    $bodyLines = (
        "--$boundary",
        "Content-Disposition: form-data; name=`"file`"; filename=`"$fileName`"",
        "Content-Type: text/plain$LF",
        [System.Text.Encoding]::UTF8.GetString($fileBytes),
        "--$boundary",
        "Content-Disposition: form-data; name=`"description`"",
        "",
        "MinIO集成测试文件",
        "--$boundary",
        "Content-Disposition: form-data; name=`"category`"",
        "",
        "test",
        "--$boundary--$LF"
    ) -join $LF
    
    $uploadHeaders = $headers.Clone()
    $uploadHeaders["Content-Type"] = "multipart/form-data; boundary=$boundary"
    
    $uploadResponse = Invoke-RestMethod -Uri "$filesUrl/upload" -Method POST -Body $bodyLines -Headers $uploadHeaders
    
    if ($uploadResponse.code -eq 200) {
        $uploadedFileName = $uploadResponse.data.fileId
        Write-Host "   文件上传成功!" -ForegroundColor Green
        Write-Host "   原始文件名: $($uploadResponse.data.originalName)" -ForegroundColor Cyan
        Write-Host "   存储文件名: $($uploadResponse.data.storedName)" -ForegroundColor Cyan
        Write-Host "   文件大小: $($uploadResponse.data.fileSize) 字节" -ForegroundColor Cyan
    } else {
        Write-Host "   文件上传失败: $($uploadResponse.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "   文件上传失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   请确保MinIO服务器正在运行" -ForegroundColor Yellow
    exit 1
}

# 3. 测试文件列表
Write-Host "`n3. 正在测试文件列表功能..." -ForegroundColor Yellow
try {
    $listResponse = Invoke-RestMethod -Uri "$filesUrl/list" -Method GET -Headers $headers
    if ($listResponse.code -eq 200) {
        Write-Host "   文件列表获取成功!" -ForegroundColor Green
        Write-Host "   总文件数: $($listResponse.data.total)" -ForegroundColor Cyan
        if ($listResponse.data.files.Count -gt 0) {
            Write-Host "   最新文件: $($listResponse.data.files[0].fileName)" -ForegroundColor Cyan
        }
    } else {
        Write-Host "   获取文件列表失败: $($listResponse.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "   获取文件列表失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. 测试文件存在性检查
Write-Host "`n4. 正在测试文件存在性检查..." -ForegroundColor Yellow
try {
    $existsResponse = Invoke-RestMethod -Uri "$filesUrl/exists/$uploadedFileName" -Method GET -Headers $headers
    if ($existsResponse.code -eq 200 -and $existsResponse.data -eq $true) {
        Write-Host "   文件存在性检查成功!" -ForegroundColor Green
    } else {
        Write-Host "   文件存在性检查失败" -ForegroundColor Red
    }
} catch {
    Write-Host "   文件存在性检查失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. 测试获取文件URL
Write-Host "`n5. 正在测试获取文件URL..." -ForegroundColor Yellow
try {
    $urlResponse = Invoke-RestMethod -Uri "$filesUrl/url/$uploadedFileName" -Method GET -Headers $headers
    if ($urlResponse.code -eq 200) {
        Write-Host "   文件URL获取成功!" -ForegroundColor Green
        Write-Host "   访问URL: $($urlResponse.data)" -ForegroundColor Cyan
    } else {
        Write-Host "   文件URL获取失败: $($urlResponse.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "   文件URL获取失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. 测试文件下载
Write-Host "`n6. 正在测试文件下载..." -ForegroundColor Yellow
try {
    $downloadPath = "$env:TEMP\downloaded-$uploadedFileName"
    Invoke-RestMethod -Uri "$filesUrl/download/$uploadedFileName" -Method GET -Headers $headers -OutFile $downloadPath
    
    if (Test-Path $downloadPath) {
        $downloadedSize = (Get-Item $downloadPath).Length
        Write-Host "   文件下载成功!" -ForegroundColor Green
        Write-Host "   下载文件大小: $downloadedSize 字节" -ForegroundColor Cyan
        Write-Host "   下载路径: $downloadPath" -ForegroundColor Cyan
        
        # 清理下载的文件
        Remove-Item $downloadPath -Force
    } else {
        Write-Host "   文件下载失败" -ForegroundColor Red
    }
} catch {
    Write-Host "   文件下载失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. 测试文件删除
Write-Host "`n7. 正在测试文件删除..." -ForegroundColor Yellow
try {
    $deleteResponse = Invoke-RestMethod -Uri "$filesUrl/$uploadedFileName" -Method DELETE -Headers $headers
    if ($deleteResponse.code -eq 200) {
        Write-Host "   文件删除成功!" -ForegroundColor Green
    } else {
        Write-Host "   文件删除失败: $($deleteResponse.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "   文件删除失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 清理测试文件
if (Test-Path $testFilePath) {
    Remove-Item $testFilePath -Force
}

Write-Host "`n=== MinIO 文件服务集成测试完成 ===" -ForegroundColor Green
Write-Host "`n测试结果说明:" -ForegroundColor Yellow
Write-Host "- 如果所有测试都显示'成功'，说明MinIO集成工作正常" -ForegroundColor White
Write-Host "- 如果有测试失败，请检查:" -ForegroundColor White
Write-Host "  1. MinIO服务器是否正在运行" -ForegroundColor White
Write-Host "  2. 配置文件中的MinIO连接信息是否正确" -ForegroundColor White
Write-Host "  3. 网络连接是否正常" -ForegroundColor White
Write-Host "`n有关详细配置信息，请参考 MINIO_INTEGRATION_GUIDE.md" -ForegroundColor Cyan