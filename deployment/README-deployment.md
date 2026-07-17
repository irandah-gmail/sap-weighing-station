# Deploying to the Client PC

## Prerequisites on the client PC
- **Java 17+** (JRE is enough) — install e.g. Eclipse Temurin.
- The scale connected via RS-232/USB-serial or on the plant network (Modbus TCP).
- Network access from this PC to your SAP S/4HANA Public Cloud tenant (HTTPS,
  outbound only — no inbound ports need to be opened on this PC).

## Folder layout on the client PC
Copy these into e.g. `C:\WeighingStation\`:
```
C:\WeighingStation\
  weighing-station-1.0.0.jar     <- from backend/target/ after `mvn package`
  application.yml                <- your copy, edited with real scale/SAP settings
  deployment\
    WeighingStation.xml
    WeighingStation.exe          <- WinSW, downloaded separately (see below)
    install-service.bat
    uninstall-service.bat
  data\                          <- created automatically (H2 database files)
  logs\                          <- created automatically (app + service logs)
```

## Step 1 — Get WinSW
Download the latest `WinSW-x64.exe` from
https://github.com/winsw/winsw/releases, rename it to `WeighingStation.exe`,
and place it in the `deployment` folder (WinSW matches the `.exe` name to the
`.xml` config file name).

## Step 2 — Edit application.yml
Set the real COM port / Modbus address for your scale, and the real SAP
Communication Arrangement details (base URL, service path, credentials).
Keep this file next to the JAR, not inside it, so you can change settings
without rebuilding.

## Step 3 — Install the service
Open an **Administrator** command prompt:
```
cd C:\WeighingStation\deployment
install-service.bat
```

## Step 4 — Verify
```
WeighingStation.exe status
```
Then open a browser to `http://localhost:8080` — you should see the
weighing station UI. Check `logs\weighing-station.log` for backend logs, and
`logs\WeighingStation.out.log` / `.err.log` for service-level logs.

## Step 5 — Kiosk mode (optional, for a touchscreen)
Create a shortcut with target:
```
"C:\Program Files\Google\Chrome\Application\chrome.exe" --kiosk http://localhost:8080
```
and place it in the Startup folder (`shell:startup`) so it launches
automatically after the service starts on boot.

## Updating the app later
1. Stop the service: `WeighingStation.exe stop`
2. Replace `weighing-station-1.0.0.jar` with the new build
3. Start it again: `WeighingStation.exe start`

Your `application.yml` and the local H2 database in `data\` are untouched by
this process, since they live outside the JAR.
