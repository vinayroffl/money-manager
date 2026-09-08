@echo off
echo ========================================
echo Deploying Money Manager Backend
echo ========================================

echo [1/3] Building JAR...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo [2/3] Rebuilding Docker image...
docker-compose build backend

echo [3/3] Restarting containers...
docker-compose up -d backend

echo ========================================
echo DEPLOYMENT COMPLETE! 🎉
echo ========================================
echo.
echo Running containers:
docker-compose ps
echo.
echo Recent logs:
docker-compose logs --tail 20 backend
echo.
echo API available at: http://localhost:8080
pause