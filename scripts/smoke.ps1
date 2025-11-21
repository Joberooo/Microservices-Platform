$ErrorActionPreference = "Stop"

# Load .env JWT_SECRET into current session if not set
if (-not $env:JWT_SECRET) {
  $line = Get-Content ".env" | Where-Object { $_ -match '^JWT_SECRET=' } | Select-Object -First 1
  if (-not $line) { throw "JWT_SECRET not found in .env and not set in environment" }
  $env:JWT_SECRET = $line.Split('=')[1].Trim()
}

# Generate token
Write-Host "Generating JWT..."
$TOKEN = ./scripts/make-jwt.ps1
if (-not $?) { throw "Failed to generate JWT" }
Write-Host ("Token prefix: {0}..." -f $TOKEN.Substring(0,20))

$base = "http://localhost:${env:GATEWAY_PORT}"; if (-not $env:GATEWAY_PORT) { $base = "http://localhost:8080" }

function Invoke-Check($name, $url, $headerValue) {
  Write-Host ("-- {0}" -f $name)
  if ($headerValue) {
    $code = curl.exe -s -o NUL -w "%{http_code}\n" -H $headerValue $url
  } else {
    $code = curl.exe -s -o NUL -w "%{http_code}\n" $url
  }
  Write-Host ("   {0} -> {1}" -f $url, $code)
  return $code
}

# Health
Invoke-Check "Gateway health" "$base/actuator/health" $null | Out-Null

# Swagger UI
Invoke-Check "Swagger UI" "$base/swagger-ui/index.html" $null | Out-Null

# OpenAPI docs (retry a couple of times)
$docsOk = $false
for ($i=0; $i -lt 3 -and -not $docsOk; $i++) {
  $code = Invoke-Check "OpenAPI docs" "$base/v3/api-docs" $null
  if ($code -eq "200") { $docsOk = $true } else { Start-Sleep -Seconds 2 }
}
if (-not $docsOk) { Write-Warning "OpenAPI docs not reachable (expected 200). Swagger UI may not show operations." }

# Unauthorized check
Invoke-Check "Products without token (expect 401)" "$base/api/products" $null | Out-Null

# Authorized check
$authHeader = "Authorization: Bearer $TOKEN"
Invoke-Check "Products with token (expect 200)" "$base/api/products?page=0`&size=2`&sort=id,desc" $authHeader | Out-Null

# Rate limit burst on health
Write-Host "-- Rate limit burst on /actuator/health"
$statusCodes = @()
for ($i=0; $i -lt 40; $i++) {
  $code = curl.exe -s -o NUL -w "%{http_code}\n" "$base/actuator/health"
  $statusCodes += $code
}
$statusCodes | Group-Object | Sort-Object Name | Format-Table -AutoSize

# Chaos endpoint basic probe
for ($i = 0; $i -lt 10; $i++) {
    Invoke-Check "[$i]Chaos (delay 200ms, 0.5 error)" "$base/api/dev/chaos?delayMs=200`&errorRate=0.5" $authHeader | Out-Null
}

Write-Host "Smoke tests completed."