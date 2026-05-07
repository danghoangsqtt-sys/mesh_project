# Mesh Node Hardware Profiles & Capabilities
Date: 2026-05-07
Topic: Handling LORA32 (Environmental/Relay Node) vs T-Beam (Soldier Node)

## Overview
The user reported that Node ID 2 (LORA32) does not have a heart rate (HR) or SpO2 sensor. It only has Environmental sensors (Temperature, Humidity, Pressure) and GPS.
Currently, the Tactical UI expects all nodes to be "Soldier Nodes" and might display `0 BPM` or `0% SpO2` which is misleading for a node that simply lacks the hardware.

## Goals
- Define a way to categorize nodes based on their hardware capabilities or `status_flags`.
- Update the Frontend UI (Tactical Panel, Map Markers) to gracefully hide or show `N/A` for missing biometric sensors.
- Ensure the Backend correctly parses and forwards `flags` indicating missing hardware without triggering alerts (e.g. `MAN_DOWN` or `HR_CRITICAL`).

## Phases
### Phase 1: UI Adaptation for Missing Sensors
- Read the `hr_valid` and `spo2_valid` flags from the node's `status_flags`.
- If false, display `--` or `N/A` instead of `0` in the Tactical Panel.

### Phase 2: Node Roles / Types
- Assign an icon or role based on available sensors (e.g., if no biometrics, it's a "Vehicle" or "Relay" or "Environmental Sensor" rather than a "Soldier").
- Reflect this role in the map marker (e.g., a weather station icon instead of a soldier icon).
