# SPEC.md — Phase 4: Critical Bug Fixes

## Overview
This phase addresses 7 bugs identified in the `project_analysis.md` audit report.

## Task Specifications

### Task 4.1: Fix BUG-001 — Serial Frame Format
- **Description:** Gateway sends `[0xAA][40 bytes][0x55]` (42 bytes), but the server `README.md` describes `[0xAA][40 bytes][CRC16 lo][CRC16 hi][0x55]` (44 bytes).
- **Plan:** Decide on the correct frame format. Since the gateway validates CRC before forwarding to the PC, the PC can trust the payload. The 42-byte frame is more efficient. We will update `README.md` and server packet structure documentation to match the 42-byte actual implementation, ensuring consistency.

### Task 4.2: Fix BUG-002 — Battery Voltage Reading
- **Description:** Soldier node reads battery voltage via `analogRead(35)` which is incorrect since AXP2101 PMU manages the battery.
- **Plan:** Update `soldier_node_production/src/lora_manager.cpp` (or `power_manager.cpp`) to use `pmu.getBattVoltage()` instead of analogRead.

### Task 4.3: Fix BUG-003 — GPS BT Blocking Setup
- **Description:** The `while(!btgps::isConnected())` loop in `setup()` blocks the entire soldier node from starting if BT GPS is not connected.
- **Plan:** Add a timeout (e.g., 30 seconds) to the `setup()` blocking loop. After timeout, the node should continue booting and rely on dead-reckoning.

### Task 4.4: Fix BUG-004 — EKF `updateGPS()`
- **Description:** In `navigation.cpp`, when GPS is fixed, `ekf.setState()` is called instead of `ekf.updateGPS()`.
- **Plan:** Replace `ekf.setState(lon, lat)` with proper `ekf.updateGPS()` call so the measurement update step runs correctly.

### Task 4.5: Fix BUG-005 — `pollReceive()` Blocking 800ms
- **Description:** `pollReceive()` in `loraTask` uses a blocking `while` loop for 800ms, disrupting the 1000ms task cycle.
- **Plan:** Change `pollReceive()` to check for LoRa packets non-blockingly, or configure LoRa interrupt (`onReceive`) for the soldier node, similar to the gateway. Alternatively, reduce the poll duration or yield with `vTaskDelay`.

### Task 4.6: Fix BUG-006 — Downlink Packet Identification
- **Description:** Soldier node identifies 32-byte downlink messages by checking `buf[0] == 0xFF`. This is prone to false positives.
- **Plan:** Define a specific `PacketType` or header byte structure for downlink messages instead of a single magic byte.

### Task 4.7: Fix BUG-007 — `activeNodes` Array Cleanup
- **Description:** Gateway's `activeNodes` array never removes timed-out nodes, eventually filling up the 100-node capacity.
- **Plan:** In the gateway's `updateNodeList()` function (or a separate periodic cleanup task), remove nodes whose `lastSeen` exceeds the timeout to free up slots.
