@echo off
REM Run this from an elevated (Administrator) command prompt, from inside
REM the deployment folder that also contains WinSW.exe (renamed download)
REM and WeighingStation.xml.
REM
REM Download WinSW.exe from: https://github.com/winsw/winsw/releases
REM Rename the downloaded exe to WeighingStation.exe and place it in this folder,
REM next to WeighingStation.xml (WinSW expects matching file names).

echo Installing SAP Weighing Station as a Windows Service...
WeighingStation.exe install

echo Starting the service...
WeighingStation.exe start

echo Done. Check status with: WeighingStation.exe status
echo Logs will appear in the .\logs folder.
pause
