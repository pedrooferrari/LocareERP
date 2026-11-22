@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ========================================
echo Locare - inicializacao
echo ========================================
if not exist "config\db.properties" (
    echo ERRO: config\db.properties nao encontrado.
    echo Copie config\db.properties.example para config\db.properties e configure usuario e senha.
    pause
    exit /b 1
)
call mvn clean compile
if errorlevel 1 (
    echo ERRO: falha na compilacao.
    pause
    exit /b 1
)
call mvn exec:java
pause
