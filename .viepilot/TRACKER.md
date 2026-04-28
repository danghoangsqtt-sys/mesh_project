# TRACKER — Mesh Command Android App

## Current State

| Field | Value |
|-------|-------|
| **Phase** | Phase 1 — Core Command Center |
| **Status** | 🔵 In Progress |
| **Version** | 0.1.0-dev |
| **Last Updated** | 2026-04-27 |

## Progress Overview

### Phase 1 — Core Command Center (7/8 tasks)

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1.1 | Project Setup & Build System | ✅ Done | Initialized Compose and Hilt |
| 1.2 | Binary Protocol Layer | ✅ Done | Implemented CRC16, Parser, FrameExtractor |
| 1.3 | USB OTG Serial Manager | ✅ Done | callbackFlow bridge, VID/PID auto-detect |
| 1.4 | ForegroundService | ✅ Done | Service + Binder + notification |
| 1.5 | Data Layer (Room + Repository) | ✅ Done | Entities, DAOs, Repository with alerts |
| 1.6 | MapLibre Offline Map | ✅ Done | Military style, status-coded markers |
| 1.7 | Tactical Panel UI | ✅ Done | 8 composables, military theme |
| 1.8 | Main Activity & Layout | ⬜ Not Started | — |

### Phase 2 — Enhanced Situational Awareness (0/6 tasks)

| # | Task | Status | Notes |
|---|------|--------|-------|
| 2.1 | Path Trail | ⬜ Not Started | — |
| 2.2 | WiFi AP Communication | ⬜ Not Started | Requires firmware update |
| 2.3 | Night Mode | ⬜ Not Started | — |
| 2.4 | Advanced Alert System | ⬜ Not Started | — |
| 2.5 | Team Management | ⬜ Not Started | — |
| 2.6 | Offline Map Manager | ⬜ Not Started | — |

### Phase 3 — Advanced Tactical Features (0/6 tasks)

| # | Task | Status | Notes |
|---|------|--------|-------|
| 3.1 | Geofencing | ⬜ Not Started | — |
| 3.2 | Distance/Bearing Calculator | ⬜ Not Started | — |
| 3.3 | Waypoint System | ⬜ Not Started | — |
| 3.4 | Mission Replay | ⬜ Not Started | — |
| 3.5 | Multi-Gateway Support | ⬜ Not Started | — |
| 3.6 | Encrypted Communication | ⬜ Not Started | Requires firmware update |

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
