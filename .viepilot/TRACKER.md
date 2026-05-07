# TRACKER.md — Mesh Soldier Tracker

Last updated: 2026-04-23 | Version: v1.0.0

---

## Brownfield Import

```yaml
brownfield_import: true
import_date: 2026-04-23
scan_tier: ASSUMED
modules_detected: [gateway_node, soldier_node_production, server]
```

---

## Current State

```yaml
current_phase: 7
current_task: "7.4 — Phase 7 Complete"
phase_status: complete
overall_status: in_progress
```

---

## Phase Progress

| Phase | Name | Progress | Status |
|---|---|---|---|
| 1 | Brownfield Import & Documentation | 5/5 | ✅ Complete |
| 2 | Code Quality Hardening | 0/7 | ⏳ Planned |
| 3 | Feature Extensions | 0/6 | ⏳ Planned |
| 4 | Critical Bug Fixes | 7/7 | ✅ Complete |
| 5 | GUI Modernization | 5/5 | ✅ Complete |
| 6 | Military Green Redesign | 3/3 | ✅ Complete |
| 7 | Hardware Control & Log Integration | 3/3 | ✅ Complete |
**Overall: 23/36 tasks (63%)**

---

## Backlog

### Pending Requests
| ID | Type | Title | Priority | Status |
|----|------|-------|----------|--------|
| FEAT-001 | ✨ | Native Military Desktop App Redesign | high | done |
| ENH-001 | 🔧 | Bright Military Green UI Redesign | high | done |
| ENH-002 | 🔧 | Tăng độ tương phản UI Control Panel | medium | done |

---

## Decision Log

| Date | Decision | Rationale | Status |
|---|---|---|---|
| 2026-04-23 | LoRa SF9 @ 433 MHz | Better range vs SF7; acceptable data rate at 1 Hz | 🔒 Locked |
| 2026-04-23 | Fixed 40-byte packet | Simplifies parsing; covers all required fields | 🔒 Locked |
| 2026-04-23 | CRC-CCITT-16 | Standard, lightweight, sufficient for 38-byte payload | 🔒 Locked |
| 2026-04-23 | Flask + gevent (no cloud) | Self-contained field deployment; no internet dependency | 🔒 Locked |
| 2026-04-23 | In-memory state (no DB) | Simplicity for v1.0; SQLite planned for Phase 3 | 🔒 Locked |
| 2026-04-23 | MIT License | Open/personal project | 🔒 Locked |

---

## Version History

| Version | Date | Notes |
|---|---|---|
| v1.0.0 | 2026-04-23 | Initial brownfield import; full sensor stack, EKF, alert engine, web dashboard |
