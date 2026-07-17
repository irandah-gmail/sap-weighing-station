# SAP Weighing Station — On-Prem Client PC Solution

A single-PC application that reads a weight scale, lets an operator enter
Production Order ID / Batch ID / User ID, calculates the total weight, and
posts the transaction to SAP S/4HANA Public Cloud via its OData API
(Communication Arrangement) — with a local retry queue so nothing is lost if
the internet connection drops.

## Architecture

```
Weight Scale --(RS232/USB/Modbus)--> Spring Boot app (embedded Tomcat, port 8080)
                                        |-- serves the Angular UI (static files)
                                        |-- reads the scale
                                        |-- stores transactions in local H2 DB
                                        |-- retries failed SAP posts on a schedule
                                        |-- posts to SAP S/4HANA Public Cloud (HTTPS/OData)
```

One process, one port, runs as a Windows Service so it survives reboots.

## Folder structure

- `backend/`  — Spring Boot app (Java 17). Builds to one runnable JAR.
- `frontend/` — Angular app. Built once, then copied into
  `backend/src/main/resources/static` so the JAR serves everything.
- `deployment/` — Windows Service wrapper (WinSW) config + install scripts.

## Build & deploy steps (on a build machine, or the client PC itself)

### 1. Build the Angular front end
```bash
cd frontend
npm install
npm run build
# copy the output into the backend's static resources
# (angular.json is already configured to output here directly)
```

### 2. Build the Spring Boot backend
```bash
cd backend
mvn clean package
# produces backend/target/weighing-station-1.0.0.jar
```

### 3. Configure `application.yml`
Edit `backend/src/main/resources/application.yml` (or, better, keep a copy
next to the JAR on the client PC and pass
`--spring.config.location=file:./application.yml` so you never rebuild the
JAR just to change config):
- Scale connection: COM port / Modbus host, baud rate
- SAP: base URL, Communication User credentials (or OAuth2 client credentials),
  the specific API path for your Communication Arrangement

### 4. Copy to the client PC
Copy `weighing-station-1.0.0.jar` + your `application.yml` + the `deployment/`
folder to e.g. `C:\WeighingStation\` on the client PC.

### 5. Install as a Windows Service
```
cd C:\WeighingStation\deployment
install-service.bat
```
This uses WinSW to run the JAR as a service that auto-starts on boot and
restarts if it crashes. See `deployment/README-deployment.md` for details.

### 6. Point the kiosk browser at the app
`http://localhost:8080` — set this as a browser shortcut launched in kiosk
mode (`chrome.exe --kiosk http://localhost:8080`) if using a touchscreen.

## Testing without real scale hardware

Set `scale.mode: simulated` in `application.yml`. The app will generate
plausible random weight readings instead of talking to a real serial port, so
you can test the full flow (UI → transaction → SAP call) before hardware is
wired up. Switch to `scale.mode: serial` or `scale.mode: modbus` once the
scale is connected.

## SAP side — what you need before this can post real data

1. In S/4HANA Public Cloud: **Communication Systems** app → create a system
   representing this PC/app.
2. **Communication User** app → create a technical user (Basic Auth or
   OAuth2 client credentials).
3. **Communication Arrangements** app → create an arrangement based on the
   Communication Scenario that bundles the API(s) you need, e.g.:
   - Production Order Confirmation
   - Goods Movement / Goods Receipt
   - Batch Management (net weight / batch characteristics)
4. From SAP API Business Hub (api.sap.com), get the exact OData service path
   and entity/field names for your system's release — these can vary by
   release and by which scenario you activate, so `SapClientService.java` is
   written to be easily adapted to the exact payload your Communication
   Arrangement expects (see the TODO comments in that file).
