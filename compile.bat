@echo off
echo Cleaning bin folder...
if exist bin rmdir /s /q bin
mkdir bin

echo Compiling project...
javac -encoding UTF-8 --release 20 -cp "lib\webcam-capture-0.3.12.jar;lib\slf4j-api-1.7.2.jar;lib\bridj-0.6.2.jar" -d bin src\common\*.java src\server\*.java src\client\*.java

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo Available programs:
    echo   - runServer.bat      : Start chat server
    echo   - runClient.bat      : Start chat client
    echo   - runWebcamTest.bat  : Test webcam
    echo.
) else (
    echo.
    echo ========================================
    echo   BUILD FAILED!
    echo ========================================
    pause
)
