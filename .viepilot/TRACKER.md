# TRACKER — Mesh Command Android App

## Current State

| Field | Value |
|-------|-------|
| **Milestone** | Milestone 2 (v2.0.0) |
| **Phase** | Phase 9 — Management Demo Simulation |
| **Status** | 🚧 In Progress |
| **Version** | 2.0.0-dev |
| **Last Updated** | 2026-04-29 |

## Progress Overview

### Phase 5 — Advanced Mapping & Visualization (5/5 tasks)
| # | Task | Status | Notes |
|---|------|--------|-------|
| 5.1 | Offline Heatmap Overlay | ✅ Done | MapLibre HeatmapLayer |
| 5.2 | Marker Clustering | ✅ Done | GeoJsonSource clusters |
| 5.3 | 3D Terrain | ✅ Done | RasterDemSource & tilt |
| 5.4 | Compass Widget | ✅ Done | MapLibre UI settings |
| 5.5 | GPS Accuracy Circles | ✅ Done | CircleLayer under nodes |

### Phase 6 — Advanced Communication & OTA (3/3 tasks)
| # | Task | Status | Notes |
|---|------|--------|-------|
| 6.1 | Two-way Command Protocol | ✅ Done | Added ACK/CMD packet formats |
| 6.2 | Remote Node Config | ✅ Done | NVS Preferences + SF Dropdown |
| 6.3 | OTA Firmware qua Gateway | ✅ Done | Chunked transfer, OtaManager |

### Phase 7 — Quality Assurance & Testing (0/3 tasks)
| # | Task | Status | Notes |
|---|------|--------|-------|
| 7.1 | Unit Testing | ✅ Done | JUnit 5, MockK |
| 7.2 | Mock Serial & UI Tests | ✅ Done | Compose testing |
| 7.3 | Integration Test Suite | ✅ Done | E2E Data flow |

### Phase 8 — Tactical Geofencing (0/3 tasks)
| # | Task | Status | Notes |
|---|------|--------|-------|
| 8.1 | Interactive Geofence Drawing | ⬜ Not Started | MapLibre polygons |
| 8.2 | Real-time Geofence Checking | ⬜ Not Started | Point-in-polygon logic |
| 8.3 | Gateway Alert Sync | ⬜ Not Started | Broadcast alert |

### Phase 9 — Management Demo Simulation (0/1 tasks)
| # | Task | Status | Notes |
|---|------|--------|-------|
| 9.1 | Management Demo Env | 🚧 In Progress | Setup flavors & mock data |

## Backlog

### Pending Requests
| ID | Type | Title | Priority | Status |
|----|------|-------|----------|--------|
| FEAT-004 | ✨ | Management Demo Env | high | in_progress |
| FEAT-002 | ✨ | Offline Map Downloader & Modern Icon | high | ✅ Done |
| ENH-003 | 🔧 | Tactical Panel Scroll & Missing UI | high | ✅ Done |

## Version History

| Version | Date | Phase | Notes |
|---------|------|-------|-------|
| 1.0.0 | 2026-04-28 | Phase 1-3 | M1 completed |
| 1.1.0 | 2026-04-28 | Phase 4 | Desktop Parity + UI Fixes |
| 2.0.0-dev | 2026-04-28 | Phase 5 | Started Milestone 2 |
