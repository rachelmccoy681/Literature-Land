# stops the windows opened by start.ps1 and shuts down kafka
# usage: .\stop.cmd            (or .\stop.ps1)
#        .\stop.cmd -KeepKafka

param(
    [switch]$KeepKafka
)

$root = $PSScriptRoot
$pidFile = Join-Path $root ".run\pids.txt"

if (Test-Path $pidFile) {
    Write-Host "Stopping services and frontend..." -ForegroundColor Cyan
    foreach ($id in Get-Content $pidFile) {
        # /T also stops the java and python processes started inside each window
        taskkill /T /F /PID $id 2>$null | Out-Null
    }
    Remove-Item $pidFile
} else {
    Write-Host "No services were started by start.ps1" -ForegroundColor Yellow
}

if (-not $KeepKafka -and (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "Stopping Kafka..." -ForegroundColor Cyan
    docker compose -f (Join-Path $root "docker-compose.yml") down
}
