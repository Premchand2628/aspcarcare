$body = @{ secretKey = "local-test-admin-key-123" } | ConvertTo-Json
$resp = Invoke-RestMethod -Uri "http://localhost:8081/auth/admin/login" -Method Post -Body $body -ContentType "application/json"
Write-Host "success=$($resp.success)"
$token = $resp.token
Write-Host "token_len=$($token.Length)"
$header = $token.Split('.')[0]
$padded = $header + ('=' * ((4 - $header.Length % 4) % 4))
$decoded = [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($padded.Replace('-', '+').Replace('_', '/')))
Write-Host "HEADER: $decoded"
