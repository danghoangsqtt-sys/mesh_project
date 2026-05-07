# AI-GUIDE — Mesh Command Android App

> Quick-reference for AI agents working on this codebase.

## Project Identity

| Field | Value |
|-------|-------|
| **Name** | Mesh Command (mesh_lilygo_app_android) |
| **Type** | Native Android (Kotlin) — Tablet command & control |
| **Platform** | Android 10+ (API 29+), Tablet landscape-only |
| **Parent Project** | firmware_mesh_lilygo (LoRa Mesh Soldier Tracker) |

## Context Loading Order

1. **This file** — Quick orientation
2. **ARCHITECTURE.md** — System design, data flow, tech decisions
3. **PROJECT-CONTEXT.md** — Domain knowledge, constraints, product vision
4. **SYSTEM-RULES.md** — Coding standards, conventions
5. **ROADMAP.md** — Current phase, tasks, acceptance criteria

## Key File Relationships

```
comm/PacketParser.kt ←→ gateway_node/include/packet.h   (MUST stay in sync)
comm/CRC16.kt        ←→ gateway_node/include/packet.h   (Same CRC16-CCITT algorithm)
comm/UsbSerialManager ←→ server/serial_comm.py           (Same framing protocol)
data/model/           ←→ server/soldier_manager.py       (Same domain model)
```

## Critical Constraints

- **100% OFFLINE** — No network calls, no CDN, no analytics, no crash reporting
- **Binary protocol** — 40-byte packed struct, little-endian, CRC16 validated
- **Frame markers** — 0xAA start, 0x55 end — MUST NOT change (firmware compatibility)
- **Military context** — UI must be high-contrast, readable in direct sunlight
- **Landscape-only** — All layouts designed for tablet landscape orientation

## Architecture Pattern

```
View (Compose) → ViewModel (StateFlow) → Repository → [Room DB / USB Serial Service]
```

- **MVVM + Repository** with Kotlin Coroutines/Flow
- **ForegroundService** for persistent USB/WiFi connection
- **Room** for local history/logging (SSOT pattern)
- **MapLibre Native** for offline vector maps (MBTiles)

## Quick Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Lint check
./gradlew lint
```
