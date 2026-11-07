@echo off
echo ========================================
echo Deploying Money Manager (frontend + backend)
echo ========================================

pushd "%~dp0.."

echo [1/2] Building Docker images...
docker compose build
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    popd
    pause
    exit /b 1
)

echo [2/2] Starting containers...
docker compose up -d
if %errorlevel% neq 0 (
    echo ERROR: Startup failed!
    popd
    pause
    exit /b 1
)

echo ========================================
echo DEPLOYMENT COMPLETE! 🎉
echo ========================================
echo.
echo Running containers:
docker compose ps
echo.
echo Recent backend logs:
docker compose logs --tail 20 backend
echo.
echo Frontend available at: http://localhost:3000
echo API available at: http://localhost:8080
popd
pause
