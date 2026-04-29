# FEAT: Management Demo Simulation Environment

## Meta
- **ID**: FEAT-004
- **Type**: Feature
- **Status**: in_progress
- **Priority**: high
- **Created**: 2026-04-29
- **Reporter**: User
- **Assignee**: AI (Antigravity)

## Summary
The goal is to create a mock/simulation environment of the Mesh LilyGo Android App for a management demo. The mock version should simulate a Gateway and 6-8 NodeMeshes running live, sending telemetry data (GPS, battery, events) without requiring actual hardware.

## Details
- Thiết lập Product Flavors (`live` và `demo`) trong Gradle để tạo 2 app độc lập (App thật và App demo).
- Vị trí GPS của các node sẽ di chuyển vòng quanh Gateway để mô phỏng.
- Có các sự kiện như vào vùng cấm (Geofence), báo động (SOS).

## Acceptance Criteria
- [ ] Product Flavors configured in `app/build.gradle.kts`.
- [ ] `MockSerialManager` implemented in `demo` source set.
- [ ] Dependency injection handles routing interfaces appropriately.
- [ ] 6-8 mock nodes move automatically on the map.

## Related
- Phase: Phase 9
- Files: `app/build.gradle.kts`, `app/src/demo/java/com/meshcommand/app/comm/MockSerialManager.kt`
- Dependencies: none

## Discussion
- The user requested Option 1 (Product flavors) and asked for node movement around Gateway.

## Resolution
