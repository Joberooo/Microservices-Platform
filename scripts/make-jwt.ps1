param(
  [string]$Secret,
  [string]$Sub,
  [int]$ExpiresInSeconds
)

$ErrorActionPreference = "Stop"

if (-not $Secret) { $Secret = $env:JWT_SECRET }
if (-not $Sub) { $Sub = "tester" }
if (-not $ExpiresInSeconds) { $ExpiresInSeconds = 3600 }
if (-not $Secret) { Write-Error "Provide -Secret or set JWT_SECRET env var." }

function b64url([byte[]]$bytes) {
  $s = [System.Convert]::ToBase64String($bytes)
  $s = $s.TrimEnd('=') -replace '\+','-' -replace '/','_'
  return $s
}

$headerObj = @{ alg = "HS256"; typ = "JWT" }
$now = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$payloadObj = @{ sub = $Sub; iat = $now; exp = ($now + $ExpiresInSeconds) }

$headerJson = ($headerObj | ConvertTo-Json -Compress)
$payloadJson = ($payloadObj | ConvertTo-Json -Compress)

$headerB64 = b64url([System.Text.Encoding]::UTF8.GetBytes($headerJson))
$payloadB64 = b64url([System.Text.Encoding]::UTF8.GetBytes($payloadJson))
$toSign = "$headerB64.$payloadB64"

$hmac = [System.Security.Cryptography.HMACSHA256]::new([System.Text.Encoding]::UTF8.GetBytes($Secret))
$sigBytes = $hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($toSign))
$sigB64 = b64url($sigBytes)

$token = "$toSign.$sigB64"
Write-Output $token
