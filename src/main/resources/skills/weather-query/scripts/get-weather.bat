@echo off
REM 天气查询脚本 - 使用城市编码直接调用 API
REM 用法: get-weather.bat <城市编码>
REM 城市编码对照: 北京=101010100 上海=101020100 广州=101280101 等
set CITYCODE=%~1
if "%CITYCODE%"=="" set CITYCODE=101010100

echo === Weather Query ===
echo CityCode: %CITYCODE%

REM 使用 sojson 免费天气 API（国内可正常访问，无需 API Key）
powershell -NoProfile -Command "try { $r = Invoke-RestMethod -Uri 'http://t.weather.sojson.com/api/weather/city/%CITYCODE%' -TimeoutSec 10; $r | ConvertTo-Json -Depth 3 } catch { Write-Host 'ERROR:' $_.Exception.Message }"

echo === End ===
