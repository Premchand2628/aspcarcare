$ErrorActionPreference = 'Stop'

Write-Host "=== Step 1: Login to otploginauth to get RS256 JWT ==="
$body = @{ secretKey = "local-test-admin-key-123" } | ConvertTo-Json
$resp = Invoke-RestMethod -Uri "http://localhost:8081/auth/admin/login" -Method Post -Body $body -ContentType "application/json"
$token = $resp.token
$header = $token.Split('.')[0]
$padded = $header + ('=' * ((4 - $header.Length % 4) % 4))
$decoded = [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($padded.Replace('-', '+').Replace('_', '/')))
Write-Host "  Header: $decoded"

Write-Host ""
Write-Host "=== Step 2: Call bookingservice /admin/stats/overview with RS256 token ==="
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8082/admin/stats/overview" -Headers @{ Authorization = "Bearer $token" } -UseBasicParsing
    Write-Host "  Status: $($r.StatusCode) $($r.StatusDescription)"
    Write-Host "  Body: $($r.Content)"
    Write-Host ""
    Write-Host "  SUCCESS: bookingservice accepted the RS256-signed token!" -ForegroundColor Green
} catch {
    Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $sr = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "  Body: $($sr.ReadToEnd())"
    }
}

Write-Host ""
Write-Host "=== Step 3: Negative test - send garbage token, expect 401/403 ==="
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8082/admin/stats/overview" -Headers @{ Authorization = "Bearer notavalidtoken" } -UseBasicParsing
    Write-Host "  UNEXPECTED: $($r.StatusCode)" -ForegroundColor Red
} catch {
    Write-Host "  Got rejected as expected: $($_.Exception.Response.StatusCode.value__)"
}
