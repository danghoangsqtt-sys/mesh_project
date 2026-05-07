# Changelog

All notable changes to Mesh Soldier Tracker are documented here.

Format: [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
Versioning: [SemVer](https://semver.org/)

---

## [Unreleased]

### Fixed
- BUG-001: Serial frame format discrepancy between Gateway and Server
- BUG-002: Battery voltage reading incorrectly via GPIO on Soldier Node
- BUG-003: GPS BT connection loop blocking `setup()` on Soldier Node
- BUG-004: EKF missing `updateGPS()` measurement integration step
- BUG-005: `pollReceive()` blocking for 800ms disrupting LoRa timing
- BUG-006: Inaccurate downlink packet identification using single magic byte
- BUG-007: `activeNodes` array filling up without timeout cleanup on Gateway

## [1.0.0] — 2026-04-23

### Added

**Soldier Node (`soldier_node_production`)**
- FreeRTOS multi-threaded architecture: `sensorTask`, `gpsTask`, `fusionTask`, `loraTask`, `displayTask`
- MPU6050 IMU reading at 200 Hz with Madgwick AHRS filter
- MAX30102 heart rate and SpO2 monitoring at 25 Hz sampling
- BME280 environmental sensor (temperature, humidity, pressure) at 1 Hz
- GPS NMEA parsing via TinyGPS++ at 10 Hz
- Extended Kalman Filter (EKF) for GPS/dead-reckoning fusion
- Step detection and dead reckoning (WALK / JOG / RUN modes)
- LoRa transmission at 1 Hz (SF9, 433 MHz, 125 kHz BW, sync 0x12)
- 40-byte `SoldierPacket` with CRC-CCITT-16 integrity check
- SSD1306 OLED display with real-time status at 2 Hz
- AXP2101 power management (XPowersLib)
- Battery voltage monitoring with LOW / CRITICAL flags
- 13-bit `statusFlags` bitmask (GPS_FIX, IMU_VALID, HR_VALID, SPO2_VALID, TEMP_VALID, HUMIDITY_VALID, PRESSURE_VALID, LOW_BATTERY, CRITICAL_BATTERY, SENSOR_ERROR, ALERT, MAN_DOWN, HEAT_STRESS)

**Gateway Node (`gateway_node`)**
- LoRa receive with interrupt-driven `onReceive` callback
- CRC-CCITT-16 validation on every received packet
- USB Serial forwarding with 0xAA…0x55 framing
- Up to 100 concurrent node tracking with 30-second timeout
- GPS position for gateway self-location
- OLED display: active node count, last packet, gateway GPS
- AXP2101 power management

**Server (`server`)**
- Flask 3.0.0 REST API with gevent concurrency
- Binary serial frame parser (`packet_handler.py`)
- In-memory soldier state with 1000-position history (`soldier_manager.py`)
- Configurable alert engine via `alerts.conf` (`alert_manager.py`)
- Alert levels: OK / WARNING / CRITICAL / EMERGENCY
- Alert types: heart rate, SpO2, temperature, humidity, pressure, battery, MAN_DOWN, HEAT_STRESS, GPS invalid, OFFLINE
- Team management via `teams.conf` (`team_manager.py`)
- REST endpoints: `/api/soldiers`, `/api/teams`, `/api/send_message`, KML export
- Web dashboard (`templates/index.html`) with real-time map
- Hardware simulator (`simulate_hardware.py`) for development without physical devices
- Serial debug tool (`debug_serial.py`)
