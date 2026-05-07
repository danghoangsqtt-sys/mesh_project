# AI-GUIDE.md — Mesh Soldier Tracker

Quick navigation for AI assistants working on this repository.

## Quick Context

| Field | Value |
|---|---|
| Project | Mesh Soldier Tracker |
| Version | v1.0.0 |
| VP Profile | none |
| Current Phase | 2 — Code Quality Hardening |
| Next Task | 2.1 — Delete .bak files |
| Mode | Brownfield import |

## Repository Layout

```
firmware_mesh_lilygo/
├── gateway_node/               # ESP32 gateway firmware (C++/Arduino)
│   ├── src/main.cpp            # Entry point — LoRa receive + serial forward
│   ├── include/packet.h        # Shared SoldierPacket struct + CRC
│   └── platformio.ini          # Board: ttgo-t-beam, lib deps
├── soldier_node_production/    # ESP32 soldier firmware (C++/FreeRTOS)
│   ├── src/
│   │   ├── main.cpp            # Task creation + setup
│   │   ├── lora_manager.cpp    # LoRa TX
│   │   ├── gps_manager.cpp     # GPS NMEA parsing
│   │   ├── mpu6050_sensor.cpp  # IMU 200Hz
│   │   ├── max30102_sensor.cpp # HR/SpO2
│   │   ├── bme280_sensor.cpp   # Env sensor
│   │   ├── navigation.cpp      # Dead reckoning + step detection
│   │   └── display_manager.cpp # OLED
│   ├── include/
│   │   ├── config.h            # ALL constants (pins, rates, thresholds)
│   │   ├── packet.h            # Shared SoldierPacket struct
│   │   ├── ekf.h               # Extended Kalman Filter
│   │   └── madgwick.h          # AHRS filter
│   └── platformio.ini
├── server/                     # Python Flask dashboard
│   ├── app.py                  # Flask app + REST routes
│   ├── packet_handler.py       # Binary frame parsing (struct.unpack)
│   ├── serial_comm.py          # Serial port reader thread
│   ├── soldier_manager.py      # In-memory soldier state
│   ├── alert_manager.py        # Threshold-based alert engine
│   ├── team_manager.py         # Team grouping
│   ├── templates/index.html    # Web dashboard (single page)
│   ├── alerts.conf             # Alert thresholds (INI)
│   └── teams.conf              # Team assignments (INI)
├── .viepilot/                  # ViePilot artifacts
│   ├── ROADMAP.md              # Phases + tasks
│   ├── TRACKER.md              # Progress + decisions
│   ├── HANDOFF.json            # Machine-readable state
│   ├── ARCHITECTURE.md         # System design + diagrams
│   ├── PROJECT-CONTEXT.md      # Domain knowledge + rules
│   ├── SYSTEM-RULES.md         # Coding standards
│   └── architecture/           # Mermaid sidecar files
├── README.md
├── CHANGELOG.md
└── LICENSE
```

## Context Loading Strategy

**For any firmware task:**
1. `.viepilot/SYSTEM-RULES.md` — coding rules
2. `soldier_node_production/include/config.h` — all constants
3. `soldier_node_production/include/packet.h` — packet format
4. Target `.cpp`/`.h` file

**For any server task:**
1. `.viepilot/PROJECT-CONTEXT.md` — business rules + conventions
2. `server/packet_handler.py` — packet schema mirror
3. `server/app.py` — route inventory
4. Target `*_manager.py` file

**For architecture decisions:**
1. `.viepilot/ARCHITECTURE.md`
2. `.viepilot/PROJECT-CONTEXT.md` § Constraints

## Critical Invariants

- `PACKET_SIZE = 40` bytes in **both** C++ (`packet.h`) and Python (`packet_handler.py`) — must stay in sync
- LoRa params (SF=9, BW=125kHz, Freq=433MHz, Sync=0x12) must be **identical** in gateway and soldier
- CRC covers bytes `[0, PACKET_SIZE-3]` inclusive — do not change without updating both sides
- Never call `delay()` inside a FreeRTOS task loop — use `vTaskDelayUntil`

## Open Audit Findings (Phase 2 backlog)

| ID | Severity | Location | Issue |
|---|---|---|---|
| mutex-gateway-globals | HIGH | `gateway_node/src/main.cpp:~50` | `activeNodes[]` unprotected |
| delay-in-task-context | HIGH | `gateway_node/src/main.cpp:179,208` | `delay()` inside task/setup |
| gpsTask-stack-size | MEDIUM | `soldier_node_production/src/main.cpp:173` | Stack 2048 → needs 3072 |
| activeNodes-bounds | MEDIUM | `gateway_node/src/main.cpp:~55` | No overflow guard at 100 |
| pytest-config | LOW | `server/pytest.ini` | Missing test runner config |
| bak-files | HIGH | repo root | 4 `.bak` files to delete |
