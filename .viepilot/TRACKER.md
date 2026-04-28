# TRACKER — Mesh Command Android App

## Current State

| Field | Value |
|-------|-------|
| **Phase** | Phase 2 — Enhanced Situational Awareness |
| **Status** | 🔵 In Progress |
| **Version** | 1.0.0 |
| **Last Updated** | 2026-04-27 |

## Progress Overview

### Phase 1 — Core Command Center (8/8 tasks) ✅ COMPLETE

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1.1 | Project Setup & Build System | ✅ Done | Initialized Compose and Hilt |
| 1.2 | Binary Protocol Layer | ✅ Done | Implemented CRC16, Parser, FrameExtractor |
| 1.3 | USB OTG Serial Manager | ✅ Done | callbackFlow bridge, VID/PID auto-detect |
| 1.4 | ForegroundService | ✅ Done | Service + Binder + notification |
| 1.5 | Data Layer (Room + Repository) | ✅ Done | Entities, DAOs, Repository with alerts |
| 1.6 | MapLibre Offline Map | ✅ Done | Military style, status-coded markers |
| 1.7 | Tactical Panel UI | ✅ Done | 8 composables, military theme |
| 1.8 | Main Activity & Layout | ✅ Done | Split-screen 65/35, Service wiring |

### Phase 2 — Enhanced Situational Awareness (6/6 tasks) ✅ COMPLETE

| # | Task | Status | Notes |
|---|------|--------|-------|
| 2.1 | Path Trail | ✅ Done | Position history DB, polyline rendering |
| 2.2 | WiFi AP Communication | ✅ Done | TCP socket client, auto-reconnect |
| 2.3 | Night Mode | ✅ Done | NightMode palette, AppColorPalette switcher |
| 2.4 | Advanced Alert System | ✅ Done | AlertManager, notifications, vibration |
| 2.5 | Team Management | ✅ Done | TeamEntity, TeamDao, CRUD operations |
| 2.6 | Offline Map Manager | ✅ Done | MBTiles scanner, import/delete |

### Phase 3 — Advanced Tactical Features (6/6 tasks) ✅ COMPLETE

| # | Task | Status | Notes |
|---|------|--------|-------|
| 3.1 | Geofencing | ✅ Done | GeofenceEntity, ray-casting algorithm |
| 3.2 | Distance/Bearing Calculator | ✅ Done | Haversine, compass bearing, grid ref |
| 3.3 | Waypoint System | ✅ Done | WaypointEntity, WaypointDao |
| 3.4 | Mission Replay | ✅ Done | MissionReplayEngine with speed control |
| 3.5 | Multi-Gateway Support | ✅ Done | Dedup manager, feedPacket API |
| 3.6 | Encrypted Communication | ✅ Done | AES-128-CBC, key generation |

## Backlog

### Pending Requests
| ID | Type | Title | Priority | Status |
|----|------|-------|----------|--------|
| FEAT-001 | ✨ | Desktop Parity: Gateway UI + Command Messaging + Sensor Display | high | new |

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-04-27 | Native Android (Kotlin) | USB OTG, hardware access, performance — best for offline military app |
| 2026-04-27 | MapLibre Native over OSMDroid | OSMDroid archived, MapLibre actively maintained, vector tiles, 3D |
| 2026-04-27 | USB OTG as primary connection | Most stable, no RF interference, powers gateway |
| 2026-04-27 | Keep existing 40-byte packet protocol | 100% firmware compatibility, no changes needed |
| 2026-04-27 | Split-screen layout (65/35) | Balance between map visibility and tactical data |
| 2026-04-27 | Jetpack Compose over XML | Modern declarative UI, less code, better state management |
| 2026-04-27 | Hilt for DI | Google recommended, lifecycle-aware, well documented |

## Version History

| Version | Date | Phase | Notes |
|---------|------|-------|-------|
| 0.1.0-dev | 2026-04-27 | Phase 1 | Project crystallized from brainstorm |
| 1.0.0 | 2026-04-28 | Phase 3 | All 3 phases complete (20 tasks) |
