# SonarQube Scan Script with Health Check
param(
    [string]$SonarToken = $env:SONAR_TOKEN,
    [int]$MaxWaitSeconds = 120,
    [int]$CheckIntervalSeconds = 2
)

if (-not $SonarToken) {
    Write-Error "SONAR_TOKEN environment variable not set or passed as parameter"
    exit 1
}

Write-Host "🔧 Cleaning up orphaned containers..." -ForegroundColor Cyan
docker-compose --profile scan down --remove-orphans 2>&1 | Out-Null

Write-Host "🚀 Starting SonarQube services..." -ForegroundColor Cyan
$env:SONAR_TOKEN = $SonarToken
docker-compose --profile scan up -d sonar-db sonarqube

Write-Host "⏳ Waiting for SonarQube to be fully initialized..." -ForegroundColor Yellow
$startTime = Get-Date
$maxWaitTime = [timespan]::FromSeconds($MaxWaitSeconds)
$sonarReady = $false
$elapsed = 0

while ($elapsed -lt $MaxWaitSeconds) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:9000/api/system/health" -ErrorAction SilentlyContinue -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            $body = $response.Content | ConvertFrom-Json
            if ($body.status -eq "UP") {
                Write-Host "✅ SonarQube is ready!" -ForegroundColor Green
                $sonarReady = $true
                break
            }
        }
    }
    catch {
        # SonarQube not ready yet
    }
    
    $elapsed = [int]((Get-Date) - $startTime).TotalSeconds
    Write-Host "  Checking... ($elapsed/$MaxWaitSeconds seconds)" -ForegroundColor Gray
    Start-Sleep -Seconds $CheckIntervalSeconds
}

if (-not $sonarReady) {
    Write-Warning "SonarQube did not become ready within $MaxWaitSeconds seconds. Attempting scan anyway..."
}

Write-Host "🔍 Running SonarScanner..." -ForegroundColor Cyan
docker-compose --profile scan run sonar-scan

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ SonarQube scan completed successfully!" -ForegroundColor Green
} else {
    Write-Host "❌ SonarQube scan failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}
