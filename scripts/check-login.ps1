param(
    [string]$Email = "admin+local@example.com",
    [string]$Password = "P@ssw0rd1",
    [string]$Endpoint = "https://x8ki-letl-twmt.n7.xano.io/api:sanos-y-salvos-auth/login"
)

$ErrorActionPreference = "Stop"

$body = @{
    email = $Email
    password = $Password
} | ConvertTo-Json

Write-Output "Checking login endpoint: $Endpoint"
Write-Output "Using email: $Email"

try {
    $response = Invoke-RestMethod -Method Post -Uri $Endpoint -ContentType "application/json" -Body $body

    if ([string]::IsNullOrWhiteSpace($response.token)) {
        Write-Error "Login succeeded but token was empty."
        exit 2
    }

    $role = if ([string]::IsNullOrWhiteSpace($response.role)) { "USER" } else { $response.role }
    Write-Output "Login OK"
    Write-Output "userId: $($response.userId)"
    Write-Output "role: $role"
    Write-Output "tokenLength: $($response.token.Length)"
    exit 0
} catch {
    if ($_.Exception.Response) {
        $status = [int]$_.Exception.Response.StatusCode
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $payload = $reader.ReadToEnd()
        Write-Error "Login check failed. HTTP $status. Body: $payload"
        exit 1
    }

    Write-Error "Login check failed: $($_.Exception.Message)"
    exit 1
}
