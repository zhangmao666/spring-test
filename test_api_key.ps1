# ========================================
# API Key Authentication Test Script
# ========================================

$baseUrl = "http://localhost:8879/api"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  API Key Authentication Test" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# ========================================
# Step 1: Get test API Key from database
# ========================================

Write-Host "Step 1: Fetching test API Key from database..." -ForegroundColor Yellow
Write-Host "Please execute the SQL migration first:" -ForegroundColor Gray
Write-Host "  mysql < add_api_key_column.sql" -ForegroundColor Gray
Write-Host ""

# For testing, use the generated API Key
# You should replace this with the actual API Key from database
$apiKey = Read-Host "Enter your API Key (or press Enter to use test key)"

if ([string]::IsNullOrWhiteSpace($apiKey)) {
    Write-Host "Using test API Key (you need to get this from database)" -ForegroundColor Yellow
    Write-Host "Run this SQL to see your API Key:" -ForegroundColor Gray
    Write-Host "  SELECT client_id, LEFT(api_key, 50) FROM api_clients;" -ForegroundColor Gray
    Write-Host ""
    
    # Prompt for confirmation
    $continue = Read-Host "Have you run the SQL migration? (Y/N)"
    if ($continue -ne "Y" -and $continue -ne "y") {
        Write-Host "`nPlease run the migration SQL first!" -ForegroundColor Red
        Write-Host "  mysql -h 118.24.128.221 -u admin -p spring_boot_test < add_api_key_column.sql" -ForegroundColor Gray
        exit
    }
    
    $apiKey = Read-Host "Enter the API Key from database"
}

if ([string]::IsNullOrWhiteSpace($apiKey)) {
    Write-Host "`nError: API Key is required!" -ForegroundColor Red
    exit
}

Write-Host "Using API Key: $($apiKey.Substring(0, [Math]::Min(20, $apiKey.Length)))..." -ForegroundColor Green

# ========================================
# Step 2: Test API call with API Key
# ========================================

Write-Host "`nStep 2: Testing API call with API Key..." -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/weather/cities" `
        -Method Get `
        -Headers @{
            "X-API-Key" = $apiKey
        }
    
    Write-Host "SUCCESS: API call completed!" -ForegroundColor Green
    Write-Host "`nAPI Response:" -ForegroundColor Cyan
    Write-Host ($response | ConvertTo-Json -Depth 3) -ForegroundColor White
    
} catch {
    Write-Host "ERROR: API call failed" -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "HTTP Status: $statusCode" -ForegroundColor Red
        
        switch ($statusCode) {
            401 { 
                Write-Host "`nTroubleshooting:" -ForegroundColor Yellow
                Write-Host "  1. Check if API Key is correct" -ForegroundColor Gray
                Write-Host "  2. Verify the X-API-Key header is set" -ForegroundColor Gray
                Write-Host "  3. Check client status in database" -ForegroundColor Gray
            }
            403 { 
                Write-Host "`nTroubleshooting:" -ForegroundColor Yellow
                Write-Host "  1. Client may be disabled" -ForegroundColor Gray
                Write-Host "  2. Check scopes/permissions" -ForegroundColor Gray
            }
            404 { 
                Write-Host "`nTroubleshooting:" -ForegroundColor Yellow
                Write-Host "  1. Check if API endpoint exists" -ForegroundColor Gray
                Write-Host "  2. Verify base URL is correct" -ForegroundColor Gray
            }
        }
    }
}

# ========================================
# Step 3: Test another endpoint
# ========================================

Write-Host "`nStep 3: Testing another endpoint..." -ForegroundColor Yellow

try {
    $response2 = Invoke-RestMethod -Uri "$baseUrl/health" `
        -Method Get `
        -Headers @{
            "X-API-Key" = $apiKey
        }
    
    Write-Host "SUCCESS: Health check passed!" -ForegroundColor Green
    Write-Host ($response2 | ConvertTo-Json) -ForegroundColor White
    
} catch {
    Write-Host "Health check failed: $($_.Exception.Message)" -ForegroundColor Red
}

# ========================================
# Summary
# ========================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Test Complete!" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "API Key Format: apk_<32 hex characters>" -ForegroundColor Gray
Write-Host "Header Name: X-API-Key" -ForegroundColor Gray
Write-Host ""
Write-Host "For more information, see API_KEY_GUIDE.md" -ForegroundColor Yellow
Write-Host ""
