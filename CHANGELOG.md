# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed
- **Critical:** Fixed `websocket_handler.py` importing non-existent `async_session_maker` (correct: `async_session`) — crashed the entire real-time event pipeline on first client connect.
- **Critical:** Fixed `websocket_handler.py` calling non-existent `manager.broadcast_json()` (correct: `manager.broadcast_event()`) — prevented all AI alerts (Geofencing, Vitals, Vision) from reaching frontend clients.
- Synced `APP_VERSION` in `config.py` to `1.0.0`.

## [1.0.0] - 2026-05-05
### Added
- **Phase 8:** PMTiles Downloader Backend API (background download from HTTP directly to Pi).
- **Phase 8:** Map Manager Frontend UI for bounding box selection and progress tracking.
- **Phase 8:** Tactical UI Redesign applying modern dark military theme and glow effects.
- **Phase 5:** AI Pathfinding (A* Algorithm) using `networkx` on PMTiles offline map data.
- **Phase 5:** Tactical Geofencing with `shapely` for real-time Point-in-Polygon boundary alerts.
- **Phase 6:** Backend structural mock/stub for future Camera/YOLO Vision Integration.
- **Phase 7:** Internationalization (i18n) setup for UI (English / Vietnamese).
- **Phase 7:** Extended Vitals Alerts (Heart Rate <60, SpO2 <90) and Power Loss detection.
- **Phase 7:** Auto-timeout Connection Lost alert with Last Known Location logic.
- **Docs:** Thêm hướng dẫn Cài đặt lên Pi 5 (`docs/dev/deployment.md`).
- **Docs:** Thêm kịch bản kiểm thử thiết bị (`docs/user/testing-guide.md`).

### Changed
- Refactored `EventLog` UI to include clear operational instructions and i18n support.
- Updated `TacticalMap` to dynamically poll target coordinates and render click-to-draw A* paths.

## [0.1.0] - 2026-05-04

### Added
- Project initialized from brainstorm session.
- ViePilot project structure created (ARCHITECTURE, ROADMAP, TRACKER, etc.).
- Database schema designed (soldiers, position_history, events, commands, geofences).
- 6-phase roadmap defined: Foundation → Dashboard → Commands → Deploy → AI Path → AI Vision.
