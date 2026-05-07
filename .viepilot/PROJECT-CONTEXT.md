# PROJECT-CONTEXT.md — Mesh Soldier Tracker

## ViePilot Active Profile

Profile: none / not configured.

---

## Product Vision

<product_vision>

### Project Scope

Mesh Soldier Tracker is a self-contained, offline-capable field system for real-time monitoring of infantry soldiers. It requires no cellular network, no cloud infrastructure, and no internet connectivity. A single field laptop running the server + one gateway node can monitor up to 100 soldiers simultaneously.

### Phase Overview

| Phase | Name | Status |
|---|---|---|
| Phase 1 | Brownfield Import & Documentation | ✅ Done (this session) |
| Phase 2 | Code Quality Hardening | 🔄 Next |
| Phase 3 | Feature Extensions | ⏳ Planned |

### Anti-Goals

- No cloud sync or SaaS dashboard (scope: local LAN / USB only)
- No voice communication (scope: data telemetry only)
- No multi-gateway mesh routing (scope: single gateway per deployment)
- No over-the-air (OTA) firmware update in v1.0 (future phase)
- No persistent database (scope: in-memory state, reset on server restart)

</product_vision>

---

## Domain Knowledge

### LoRa Parameters (CRITICAL — must be identical on all devices)

```
Frequency:       433 MHz (EU) / 915 MHz (US — change LORA_FREQ)
Spreading Factor: 9
Bandwidth:       125 kHz
Sync Word:       0x12
Preamble:        8
CRC:             Enabled
```

Any mismatch on **any** parameter means devices cannot communicate. This is the #1 field issue.

### Sensor I2C Addresses

```
MPU6050 (IMU):       0x68
BME280/BME680 (Env): 0x76 or 0x77 (check jumper)
MAX30102 (HR/SpO2):  0x57
SSD1306 (OLED):      0x3C
```

I2C bus: SDA=GPIO21, SCL=GPIO22, 400 kHz.

### Dead Reckoning Logic

When GPS is unavailable:
1. `mpu6050_sensor` provides acceleration at 200 Hz
2. `navigation.cpp` detects steps via threshold on Z-axis acceleration magnitude
3. Step length is estimated from `MoveMode` (WALK=0.75m, JOG=1.1m, RUN=1.4m)
4. Heading from Madgwick AHRS filter on IMU data
5. EKF (`ekf.h`) fuses GPS position (when available) with dead-reckoning estimate

### Alert Thresholds

Configured in `server/alerts.conf` (INI format). Operators edit this file in the field.
Default critical conditions: HR <40 or >180 BPM, SpO2 <90%, MAN_DOWN flag set.

### Node ID Assignment

Each soldier node must have a unique `cfg::NODE_ID` (uint8, 1–254) set at compile time in `soldier_node_production/include/config.h`. Gateway tracks nodes by ID with 30-second timeout.

### Battery Monitoring

Battery voltage read via GPIO35 through a 2:1 resistor divider.
- `LOW_BATTERY` flag: < 3.5 V
- `CRITICAL_BATTERY` flag: < 3.3 V

### Position History

`SoldierState.position_history` keeps the last 1000 GPS/DR positions in RAM. On server restart all history is lost. KML export available via `/api/soldiers/<id>/kml`.

---

## Business Rules

1. A packet with invalid CRC16 is **silently dropped** at both gateway and server layers.
2. A soldier not heard from for 30 seconds is flagged **OFFLINE** — alert level CRITICAL.
3. `MAN_DOWN` status flag triggers **EMERGENCY** alert regardless of other sensor values.
4. Team assignments persist in `server/teams.conf` (INI format) across server restarts.
5. Alert thresholds are per-alert-type in `alerts.conf`; missing keys fall back to safe defaults.

---

## Conventions

### C++ (Firmware)

- Constants in `namespace cfg {}` in `include/config.h` (soldier) or `#define` in `main.cpp` (gateway — should be migrated)
- Pin numbers never hardcoded in `.cpp` files — always referenced via `cfg::` or `#define`
- FreeRTOS tasks use `vTaskDelayUntil` for periodic timing (not `delay()` or `vTaskDelay`)
- Packet struct uses `__attribute__((packed))` — do not add padding fields
- CRC always covers bytes 0 to `PACKET_SIZE - 3` (excludes last 2 bytes = CRC field itself)

### Python (Server)

- Flask routes in `app.py` are thin — business logic in `*_manager.py` modules
- All soldier data flows through `SoldierPacket.to_dict()` before leaving the server layer
- Config files (`.conf`) use Python `configparser` INI format
- No ORM — state is in-memory `dict` keyed by `node_id`

### Git Conventions

- Conventional Commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`
- Scope examples: `feat(gateway):`, `fix(server):`, `chore(soldier):`
- Branch: `main` (stable) / `dev` (integration) / `feature/*`

---

## Skills

*(No skills declared — brownfield import.)*

---

## Constraints

- ESP32 heap: ~300 KB free after FreeRTOS. Stack overflow is silent on ESP32 — always verify with `uxTaskGetStackHighWaterMark()`.
- LoRa duty cycle: 1% in EU 433 MHz band (1 Hz TX with 40-byte payload = ~11 ms air time = ~0.001% duty — well within limit).
- `SoldierPacket` struct size must stay **exactly 40 bytes** — server `struct.unpack` format string is hardcoded to `PACKET_SIZE = 40`.
- Server uses `gevent` for concurrency — do not use `threading.Thread` directly; use `gevent.spawn`.
