@echo off
chcp 65001 >nul
echo.
echo =================================================================
echo    WILDFIRE MANAGEMENT MULTI-AGENT SYSTEM - CUSTOM RESOURCES
echo =================================================================
echo.
echo This will start the simulation with custom resource configuration.
echo Default values: 4 Fire Trucks, 2 Aircraft, 1 Helicopter, 6 Ground Crews
echo.
echo Press Enter to use defaults, or specify custom numbers:
echo.

set /p trucks="Number of Fire Trucks (default 4): "
if "%trucks%"=="" set trucks=4

set /p aircraft="Number of Aircraft (default 2): "
if "%aircraft%"=="" set aircraft=2

set /p helicopters="Number of Helicopters (default 1): "
if "%helicopters%"=="" set helicopters=1

set /p crews="Number of Ground Crews (default 6): "
if "%crews%"=="" set crews=6

echo.
echo =================================================================
echo Starting simulation with:
echo - Fire Trucks: %trucks%
echo - Aircraft: %aircraft%
echo - Helicopters: %helicopters%
echo - Ground Crews: %crews%
echo =================================================================
echo.

java -Xmx8g -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.language=en -Duser.country=US -Dswing.systemlaf=javax.swing.plaf.nimbus.NimbusLookAndFeel -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -cp bin;lib/jade.jar MainContainer custom %trucks% %aircraft% %helicopters% %crews%

echo.
echo Simulation ended. Press any key to exit...
pause >nul