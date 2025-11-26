# Third-Party OAuth2 Token Test
# Get token using client credentials

$baseUrl = "http://localhost:8879/api"
$clientId = "client_test_demo_001"
$clientSecret = "123456"

Write-Host "`n=== OAuth2 Client Credentials Flow ===" -ForegroundColor Cyan

# Step 1: Prepare Basic Auth
Write-Host "`nStep 1: Preparing credentials..." -ForegroundColor Yellow
$credentials = "${clientId}:${clientSecret}"
$basicAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($credentials))

Write-Host "  Client ID: $clientId" -ForegroundColor Gray
Write-Host "  Basic Auth: $basicAuth" -ForegroundColor Gray

# Step 2: Get Token
Write-Host "`nStep 2: Requesting OAuth2 token..." -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/oauth2/token" `
        -Method Post `
        -Headers @{
            "Authorization" = "Basic $basicAuth"
            "Content-Type" = "application/x-www-form-urlencoded"
        } `
        -Body "grant_type=client_credentials"
    
    Write-Host "SUCCESS: Token received!" -ForegroundColor Green
    Write-Host "`nToken Info:" -ForegroundColor Cyan
    Write-Host "  Token Type: $($response.token_type)" -ForegroundColor White
    Write-Host "  Expires In: $($response.expires_in) seconds" -ForegroundColor White
    Write-Host "  Scope: $($response.scope)" -ForegroundColor White
    Write-Host "  Access Token (first 80 chars):" -ForegroundColor White
    Write-Host "  $($response.access_token.Substring(0, [Math]::Min(80, $response.access_token.Length)))..." -ForegroundColor Gray
    
    $token = $response.access_token
    
    # Step 3: Test API call
    Write-Host "`nStep 3: Testing API call with token..." -ForegroundColor Yellow
    
    try {
        $apiResponse = Invoke-RestMethod -Uri "$baseUrl/weather/cities" `
            -Method Get `
            -Headers @{
                "Authorization" = "Bearer $token"
            }
        
        Write-Host "SUCCESS: API call completed!" -ForegroundColor Green
        Write-Host "`nAPI Response:" -ForegroundColor Cyan
        Write-Host ($apiResponse | ConvertTo-Json -Depth 2) -ForegroundColor White
        
    } catch {
        Write-Host "ERROR: API call failed" -ForegroundColor Red
        Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
        
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorBody = $reader.ReadToEnd()
            Write-Host "Response: $errorBody" -ForegroundColor Red
        }
    }
    
} catch {
    Write-Host "ERROR: Failed to get token" -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "HTTP Status: $statusCode" -ForegroundColor Red
        
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "Response: $errorBody" -ForegroundColor Red
    }
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan

# Alternative Method: POST parameters
Write-Host "`n--- Alternative: Using POST parameters ---" -ForegroundColor Yellow

try {
    $response2 = Invoke-RestMethod -Uri "$baseUrl/oauth2/token" `
        -Method Post `
        -ContentType "application/x-www-form-urlencoded" `
        -Body @{
            grant_type = "client_credentials"
            client_id = $clientId
            client_secret = $clientSecret
        }
    
    Write-Host "SUCCESS: Alternative method also works!" -ForegroundColor Green
    Write-Host "Token (first 50 chars): $($response2.access_token.Substring(0, 50))..." -ForegroundColor Gray
    
} catch {
    Write-Host "Alternative method failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
