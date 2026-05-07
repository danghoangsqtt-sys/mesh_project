# Mesh Soldier Tracker

Real-time field monitoring system for infantry soldiers using LoRa mesh networking.

A self-contained, offline-capable system — no cellular, no cloud, no internet required.

---

## System Components

| Component | Language | Role |
|---|---|---|
| [`gateway_node/`](gateway_node/) | C++ / Arduino | ESP32 gateway — receives LoRa packets, forwards via USB Serial |
| [`soldier_node_production/`](soldier_node_production/) | C++ / FreeRTOS | ESP32 wearable — reads sensors, transmits 40-byte LoRa packets |
| [`server/`](server/) | Python / Flask | Field laptop dashboard — parses serial, REST API, alert engine |

---

## Hardware Required

**Per soldier node (TTGO T-Beam ESP32):**
- MPU6050 — 6-axis IMU (accelerometer + gyroscope) @ I2C 0x68
- MAX30102 — Heart rate + SpO2 @ I2C 0x57
- BME280 — Temperature / humidity / pressure @ I2C 0x76
- GPS module — UART (RX: GPIO34, TX: GPIO12)
- LoRa SX1276/78 — 433 MHz (built-in on T-Beam)
- SSD1306 OLED — 128×64 @ I2C 0x3C

**Gateway (TTGO T-Beam ESP32):** LoRa only (no extra sensors needed).

---

## Quick Start

### 1. Flash Soldier Nodes

```bash
cd soldier_node_production
# Set unique NODE_ID for each device in include/config.h
# constexpr uint8_t NODE_ID = 1;  // 1, 2, 3 ...
platformio run --target upload
```

### 2. Flash Gateway

```bash
cd gateway_node
platformio run --target upload
```

### 3. Start Server

```bash
cd server
pip install -r requirements.txt
python app.py --port COM3    # Windows
python app.py --port /dev/ttyUSB0  # Linux/Mac
```

### 4. Open Dashboard

```
http://localhost:5000
```

---

## LoRa Parameters (CRITICAL)

All devices must use identical settings:

| Parameter | Value |
|---|---|
| Frequency | 433 MHz (EU) / 915 MHz (US) |
| Spreading Factor | 9 |
| Bandwidth | 125 kHz |
| Sync Word | 0x12 |
| Preamble | 8 |
| CRC | Enabled |

---

## Packet Format

40-byte binary struct (CRC-CCITT-16 protected):

```
[node_id u16][timestamp u32][lat f32][lon f32][heading f32]
[hr u8][spo2 u8][temp f32][humidity f32][pressure f32]
[battery f32][status_flags u16][crc16 u16]
```

Serial framing (gateway → server): `[0xAA][40 bytes][0x55]`

---

## Configuration

- **Alert thresholds:** `server/alerts.conf` (INI format — edit in field)
- **Team assignments:** `server/teams.conf` (INI format)
- **Node ID:** `soldier_node_production/include/config.h` → `cfg::NODE_ID`

---

## Running Tests

```bash
cd server
pytest -v
```

---

## License

MIT — see [LICENSE](LICENSE).
