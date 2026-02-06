# scripts/reset-db.ps1
# Resets the Postgres data for local demos/tests (keeps schema/migrations).
# Usage (run from project root):
#   powershell -ExecutionPolicy Bypass -File .\scripts\reset-db.ps1

$ErrorActionPreference = "Stop"

$dbUser = "fraud"
$dbName = "fraud"
$serviceName = "db"                 # docker-compose service name
$fallbackContainerName = "fraudruleengine-db"  # only used if compose isn't running

Write-Host "Resetting database..." -ForegroundColor Cyan

# SQL (CASCADE handles FK dependencies safely)
$sql = @"
TRUNCATE TABLE rule_hits, fraud_cases, transactions
RESTART IDENTITY
CASCADE;
"@

# Prefer docker compose exec against the *service* (most reliable)
try {
  $dbContainerId = (docker compose ps -q $serviceName) 2>$null
} catch {
  $dbContainerId = ""
}

if (-not [string]::IsNullOrWhiteSpace($dbContainerId)) {
  # Use -T to avoid interactive TTY issues in CI / non-interactive shells
  docker compose exec -T $serviceName psql -U $dbUser -d $dbName -v ON_ERROR_STOP=1 -c $sql | Out-Host
  Write-Host "Done. Tables truncated and identities reset." -ForegroundColor Green
  exit 0
}

# Fallback: if compose isn't available, try direct docker exec by container name
$running = docker ps --format "{{.Names}}" | Select-String -SimpleMatch $fallbackContainerName
if (-not $running) {
  Write-Host "Could not find a running DB container." -ForegroundColor Yellow
  Write-Host "Start it with: docker compose up -d" -ForegroundColor Yellow
  exit 1
}

docker exec -i $fallbackContainerName psql -U $dbUser -d $dbName -v ON_ERROR_STOP=1 -c $sql | Out-Host

Write-Host "Done. Tables truncated and identities reset." -ForegroundColor Green
