@echo off
REM Run from an elevated command prompt in this folder.
echo Stopping and removing the SAP Weighing Station service...
WeighingStation.exe stop
WeighingStation.exe uninstall
echo Done.
pause
