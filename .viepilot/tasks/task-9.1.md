# Task 9.1 — Management Demo Simulation Environment

## Objective
Establish a completely isolated "Demo" variant of the Mesh LilyGo Android App that does not rely on real hardware. It simulates a Gateway and 6-8 NodeMeshes with moving GPS coordinates to simulate real-world tactical scenarios (safe zones, restricted zones, alerts) for management demonstrations.

## Paths
- `app/build.gradle.kts`
- `app/src/main/java/com/meshcommand/app/di/SerialModule.kt` (or wherever DI is configured for the USB/Serial data source)
- `app/src/live/java/com/meshcommand/app/data/LiveSerialManager.kt`
- `app/src/demo/java/com/meshcommand/app/data/MockSerialManager.kt`
- `app/src/main/java/com/meshcommand/app/data/SerialManager.kt` (Interface)

## File-Level Plan
1. **`app/build.gradle.kts`**: Add `flavorDimensions("env")` and create two productFlavors: `live` and `demo`. Set `applicationIdSuffix = ".demo"` for the demo flavor.
2. **`SerialManager` Abstraction**: Ensure the app depends on an interface `SerialManager` (or similar) instead of a concrete USB class.
3. **`LiveSerialManager`**: Move the real USB OTG serial reading logic into the `live` source set.
4. **`MockSerialManager`**: Implement a mock data generator in the `demo` source set. 
   - State for 8 nodes (ID 1 to 8).
   - Use Coroutines to emit updated GPS locations every few seconds.
   - GPS locations should move flexibly on the map (e.g. circle around a gateway coordinate or random walk).
   - Add random events (SOS, boundary crossed).
5. **DI Binding**: Adjust Hilt DI so that the `demo` flavor provides `MockSerialManager` and the `live` flavor provides `LiveSerialManager`.

## Quality Gate
- App builds successfully for both `liveDebug` and `demoDebug` variants.
- The `demo` variant can be installed alongside the `live` variant.
- The `demo` variant shows 8 nodes moving on the map without any USB Gateway attached.
