# ROADMAP.md — Mesh Soldier Tracker

Version: v1.0.0 | Imported: 2026-04-23

---

## Phase 1 — Brownfield Import & Documentation ✅

> Goal: Bootstrap ViePilot tracking on the existing codebase; establish project documentation baseline.

| # | Task | Acceptance Criteria | Status |
|---|---|---|---|
| 1.1 | Generate `.viepilot/` scaffold (crystallize) | All VP artifacts present | ✅ Done |
| 1.2 | Write root `README.md` | Covers system overview, setup, components | ✅ Done |
| 1.3 | Write `CHANGELOG.md` | v1.0.0 entry documents existing features | ✅ Done |
| 1.4 | Add root `.gitignore` | Covers `.pio/`, `.venv/`, `__pycache__/`, `*.bak` | ✅ Done |
| 1.5 | Add `LICENSE` (MIT) | License file present at root | ✅ Done |

**Verification:**
```bash
ls .viepilot/ && cat README.md && cat LICENSE
```

---

## Phase 2 — Code Quality Hardening 🔄

> Goal: Fix all HIGH/MEDIUM audit findings before adding new features.

| # | Task | Acceptance Criteria | Status |
|---|---|---|---|
| 2.1 | Delete all `.bak` files | `find . -name "*.bak"` returns nothing (excl. `.pio`) | ⏳ |
| 2.2 | Add mutex to `gateway_node` shared globals | `activeNodes[]` and `totalMessagesReceived` protected with `portMUX_TYPE` | ⏳ |
| 2.3 | Replace `delay()` with `vTaskDelay` in task context | `grep -n "delay(" gateway_node/src/main.cpp` shows only setup-phase calls | ⏳ |
| 2.4 | Add bounds check on `activeNodes[100]` | Overflow logs a warning and skips insert | ⏳ |
| 2.5 | Bump `gpsTask` stack to 3072 words | `xTaskCreatePinnedToCore(gpsTask, "GPS", 3072, ...)` | ⏳ |
| 2.6 | Add `pytest.ini` and fixture for packet parsing | `pytest server/` discovers and runs all 4 test files | ⏳ |
| 2.7 | Migrate gateway `#define` constants to `namespace cfg {}` | All pin/timing constants use typed `constexpr` | ⏳ |

**Verification:**
```bash
pio run -d gateway_node 2>&1 | grep -E "warning|error"
pio run -d soldier_node_production 2>&1 | grep -E "warning|error"
cd server && pytest -v
```

---

## Phase 3 — Feature Extensions ⏳

> Goal: Extend capabilities for longer deployments and better field usability.

| # | Task | Acceptance Criteria | Status |
|---|---|---|---|
| 3.1 | Soldier node: light sleep between LoRa TX | Battery life improves >30%; sensors wake on FreeRTOS tick | ⏳ |
| 3.2 | Server: persist state to SQLite on shutdown | Soldier history survives server restart | ⏳ |
| 3.3 | Server: WebSocket push (replace polling) | Dashboard updates in <200 ms; no `/api/soldiers` polling loop | ⏳ |
| 3.4 | Multi-node ID configuration (runtime, not compile-time) | NODE_ID set via LoRa broadcast from gateway | ⏳ |
| 3.5 | Gateway: multi-gateway aggregation support | Two gateways forward to same server; dedup by node_id+timestamp | ⏳ |
| 3.6 | Server: KML/GPX export UI button | Download track file from dashboard without curl | ⏳ |

**Verification:**
```bash
# Per-task verification commands defined at task start
```

---

## Phase 4 — Critical Bug Fixes ✅

> Goal: Resolve the critical and medium bugs identified in the project analysis report.

| # | Task | Acceptance Criteria | Status |
|---|---|---|---|
| 4.1 | Fix BUG-001: Serial frame format | Server & Gateway frame parsing matches README | ✅ Done |
| 4.2 | Fix BUG-002: Battery voltage reading | `pmu.getBattVoltage()` used instead of `analogRead` | ✅ Done |
| 4.3 | Fix BUG-003: GPS BT blocking setup | Add 30s timeout to BT GPS connection loop | ✅ Done |
| 4.4 | Fix BUG-004: EKF `updateGPS()` | `ekf.updateGPS()` called when GPS is fixed | ✅ Done |
| 4.5 | Fix BUG-005: `pollReceive()` blocking | Use smaller delay or interrupt for LoRa RX | ✅ Done |
| 4.6 | Fix BUG-006: Downlink packet identification | Add more robust magic byte or packet type field | ✅ Done |
| 4.7 | Fix BUG-007: `activeNodes` array cleanup | Clear timed-out nodes from the array to free space | ✅ Done |

---

## Phase 5 — GUI Modernization 🔄

> Goal: Convert the Flask Web Dashboard to a native PySide6 Desktop App with iOS styling.

| # | Task | Acceptance Criteria | Status |
|---|---|---|---|
| 5.1 | Setup PySide6 | Environment configured with PySide6 & dependencies | ✅ Done |
| 5.2 | Extract Map UI | Leaflet map is a static resource centered on Nha Trang | ✅ Done |
| 5.3 | Build Native Widgets | Reusable iOS-styled PyQt components (Sidebar, Card) | ✅ Done |
| 5.5 | Clean up old framework | Remove Flask server code | ✅ Done |

### Phase 6: Military Green Redesign (ENH-001)
**Goal:** Complete UI overhaul to 100% bright, military green theme with Node Table layout.

| # | Task | Acceptance Criteria | Status |
|---|---|---|---|
| 6.1 | Update Global QSS | Military green theme applied, white text | ✅ Done |
| 6.2 | Refactor Layout | Implement QTableWidget for nodes instead of cards | ✅ Done |
| 6.3 | Fix Map Rendering | OpenStreetMap tiles render brightly, matching green markers | ✅ Done |

**Verification:**
```bash
python server/main_app.py --port MOCK
```
# Per-task verification commands defined at task start
```

---

## Phases Inventory

```yaml
phases_inventory:
  - id: 1
    name: Brownfield Import & Documentation
    status: complete
    tasks: [1.1, 1.2, 1.3, 1.4, 1.5]
  - id: 2
    name: Code Quality Hardening
    status: in_progress
    tasks: [2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7]
  - id: 3
    name: Feature Extensions
    status: planned
    tasks: [3.1, 3.2, 3.3, 3.4, 3.5, 3.6]
  - id: 4
    name: Critical Bug Fixes
    status: complete
    tasks: [4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7]
  - id: 5
    name: GUI Modernization
    status: complete
    tasks: [5.1, 5.2, 5.3, 5.4, 5.5]
  - id: 6
    name: Military Green Redesign
    status: complete
    tasks: [6.1, 6.2, 6.3]
```
