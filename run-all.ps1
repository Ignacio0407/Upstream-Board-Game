# Script de arranque consolidado para Upstream (Backend Spring Boot + Frontend Vite)
$Root = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $Root

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Iniciando Plataforma Upstream (Spring Boot 3 + React 18) " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Arrancar Backend Spring Boot en una nueva ventana de consola
Write-Host "[1/2] Levantando Backend (https://localhost:8080)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$Root'; Write-Host '=== BACKEND: Spring Boot 3 ===' -ForegroundColor Green; .\mvnw.cmd spring-boot:run"

# 2. Arrancar Frontend Vite en una nueva ventana de consola
Write-Host "[2/2] Levantando Frontend (http://localhost:3000)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$Root\frontend'; Write-Host '=== FRONTEND: React 18 + Vite ===' -ForegroundColor Yellow; npm run dev"

Write-Host ""
Write-Host "¡Servicios lanzados con exito en consolas dedicadas!" -ForegroundColor Cyan
Write-Host "- Backend API / H2 / Swagger: https://localhost:8080" -ForegroundColor Gray
Write-Host "- Frontend Web App:           http://localhost:3000" -ForegroundColor Gray
Write-Host "==========================================================" -ForegroundColor Cyan
