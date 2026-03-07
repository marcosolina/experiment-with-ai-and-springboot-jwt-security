$ErrorActionPreference = "Stop"
$repo = "https://github.com/marcosolina/experiment-with-ai-and-springboot-jwt-security.git"
$folder = "experiment-with-ai-and-springboot-jwt-security"

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  POC Security - JWT Auth Demo" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Check prerequisites
$missing = @()
if (-not (Get-Command git -ErrorAction SilentlyContinue)) { $missing += "git" }
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { $missing += "docker" }

if ($missing.Count -gt 0) {
    Write-Host "Missing required tools: $($missing -join ', ')" -ForegroundColor Red
    Write-Host "Please install them and try again." -ForegroundColor Red
    exit 1
}

# Check Docker is running
$dockerInfo = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker is not running. Please start Docker Desktop and try again." -ForegroundColor Red
    exit 1
}

Write-Host "[1/3] Cloning repository..." -ForegroundColor Yellow

if (Test-Path $folder) {
    Write-Host "  Folder '$folder' already exists. Pulling latest changes..."
    Push-Location $folder
    git pull --quiet
    Pop-Location
} else {
    git clone --quiet $repo
}

Write-Host "[2/3] Building and starting services with Docker..." -ForegroundColor Yellow
Write-Host "  This may take a few minutes on the first run." -ForegroundColor Gray
Write-Host ""

Push-Location $folder
docker compose up --build -d

Write-Host ""
Write-Host "[3/3] Services are starting..." -ForegroundColor Yellow
Write-Host ""
Write-Host "  Waiting for tests to complete. Follow the logs with:" -ForegroundColor Gray
Write-Host "    docker compose logs -f e2e-tests" -ForegroundColor White
Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Host "  Test report: http://localhost:9323" -ForegroundColor Green
Write-Host "  Frontend:    http://localhost:5173" -ForegroundColor Green
Write-Host "  Supervisor:  http://localhost:8081" -ForegroundColor Green
Write-Host "  Messages:    http://localhost:8082" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Login with: admin / admin123" -ForegroundColor Cyan
Write-Host ""
Write-Host "  To stop:  docker compose down" -ForegroundColor Gray
Write-Host "  To logs:  docker compose logs -f" -ForegroundColor Gray
Write-Host ""

Pop-Location
