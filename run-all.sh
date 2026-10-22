#!/usr/bin/env bash
# Script de arranque consolidado para Upstream (Linux/macOS/Git Bash)
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

echo "=========================================================="
echo "  Iniciando Plataforma Upstream (Spring Boot 3 + React 18) "
echo "=========================================================="

echo "[1/2] Levantando Backend en segundo plano (https://localhost:8080)..."
./mvnw spring-boot:run &
BACKEND_PID=$!

echo "[2/2] Levantando Frontend en segundo plano (http://localhost:3000)..."
(cd frontend && npm run dev) &
FRONTEND_PID=$!

trap "echo ''; echo 'Deteniendo servicios...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit" SIGINT SIGTERM EXIT

echo ""
echo "¡Servicios lanzados con exito!"
echo "- Backend API / Swagger: https://localhost:8080"
echo "- Frontend Web App:      http://localhost:3000"
echo "Presiona Ctrl+C para detener ambos servicios."
echo "=========================================================="

wait
