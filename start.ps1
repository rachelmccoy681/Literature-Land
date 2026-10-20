# starts kafka, the backend services and the frontend, then opens the site in the browser
# usage: .\start.cmd                          (or .\start.ps1)
#        .\start.cmd -Services stock,cart     (start only some services)
#        .\start.cmd -SkipKafka -NoBrowser
# stop everything with: .\stop.cmd

param(
    # services the frontend talks to; add payment, chatbot or customer when needed
    [string[]]$Services = @("stock", "cart", "order"),
    [switch]$SkipKafka,
    [switch]$NoBrowser,
    [int]$FrontendPort = 5500
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$runDir = Join-Path $root ".run"
$pidFile = Join-Path $runDir "pids.txt"

$ports = @{
    stock    = 8081
    cart     = 8082
    order    = 8083
    payment  = 8084
    chatbot  = 8085
    customer = 8080
}

function Test-Port([int]$port) {
    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $connect = $client.BeginConnect("127.0.0.1", $port, $null, $null)
        return $connect.AsyncWaitHandle.WaitOne(300) -and $client.Connected
    } catch {
        return $false
    } finally {
        $client.Close()
    }
}

function Wait-Port([int]$port, [string]$name, [int]$timeoutSeconds = 420) {
    $deadline = (Get-Date).AddSeconds($timeoutSeconds)
    while (-not (Test-Port $port)) {
        if ((Get-Date) -gt $deadline) {
            Write-Warning "$name did not start on port $port within $timeoutSeconds seconds - check its window for errors"
            return $false
        }
        Start-Sleep -Seconds 2
    }
    Write-Host "  $name is up on port $port" -ForegroundColor Green
    return $true
}

# opens a new powershell window running the command and remembers its process id
function Start-Window([string]$title, [string]$workingDir, [string]$command) {
    $script = "`$host.UI.RawUI.WindowTitle = '$title'; Set-Location '$workingDir'; $command"
    $process = Start-Process powershell -PassThru -WorkingDirectory $workingDir `
        -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-Command", $script
    Add-Content -Path $pidFile -Value $process.Id
}

# "stock,cart" arrives as one string when passed through start.cmd
$Services = @($Services -split "," | ForEach-Object { $_.Trim().ToLower() } | Where-Object { $_ })

foreach ($service in $Services) {
    if (-not $ports.ContainsKey($service)) {
        throw "unknown service '$service' - choose from: $($ports.Keys -join ', ')"
    }
}

New-Item -ItemType Directory -Force $runDir | Out-Null

# 1. kafka (optional: without it trending books and the order service's stock copy stop updating)
if (-not $SkipKafka) {
    Write-Host "Starting Kafka..." -ForegroundColor Cyan
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-Warning "docker was not found - continuing without Kafka"
        $SkipKafka = $true
    } else {
        docker compose -f (Join-Path $root "docker-compose.yml") up -d --wait
        if ($LASTEXITCODE -ne 0) {
            Write-Warning "Kafka failed to start (is Docker Desktop running?) - continuing without it"
            $SkipKafka = $true
        }
    }
}
# the service windows inherit this, and the stock service then skips sending events
$env:KAFKA_ENABLED = if ($SkipKafka) { "false" } else { "true" }

# 2. backend services, each in its own window so its logs stay visible
Write-Host "Starting services: $($Services -join ', ')" -ForegroundColor Cyan
foreach ($service in $Services) {
    $port = $ports[$service]
    if (Test-Port $port) {
        Write-Host "  $service is already running on port $port, skipping" -ForegroundColor Yellow
        continue
    }
    $serviceDir = Join-Path $root "services\$service-service"
    Start-Window "Literature Land - $service-service" $serviceDir ".\mvnw.cmd spring-boot:run"
}

# 3. frontend on port 5500, the origin the services allow
Write-Host "Starting frontend on port $FrontendPort..." -ForegroundColor Cyan
$frontendDir = Join-Path $root "frontend"
if (Test-Port $FrontendPort) {
    Write-Host "  something is already serving port $FrontendPort (Live Server?), using it" -ForegroundColor Yellow
} elseif (Get-Command python -ErrorAction SilentlyContinue) {
    Start-Window "Literature Land - frontend" $frontendDir "python -m http.server $FrontendPort --bind 127.0.0.1"
} elseif (Get-Command npx -ErrorAction SilentlyContinue) {
    Start-Window "Literature Land - frontend" $frontendDir "npx --yes http-server -p $FrontendPort -a 127.0.0.1 -c-1"
} else {
    throw "python or node is needed to serve the frontend"
}

# 4. wait until everything answers, then open the site
Write-Host "Waiting for services (the first run downloads dependencies and can take a few minutes)..." -ForegroundColor Cyan
Wait-Port $FrontendPort "frontend" 30 | Out-Null
foreach ($service in $Services) {
    Wait-Port $ports[$service] "$service-service" | Out-Null
}

$url = "http://127.0.0.1:$FrontendPort/home/home.html"
Write-Host ""
Write-Host "Literature Land is running at $url" -ForegroundColor Green
Write-Host "Stop everything with .\stop.cmd"
if (-not $NoBrowser) {
    Start-Process $url
}
