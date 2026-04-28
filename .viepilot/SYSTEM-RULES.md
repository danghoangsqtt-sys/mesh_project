# SYSTEM-RULES — Mesh Command Android App

## Architecture Rules

1. **MVVM strict**: View → ViewModel → Repository → DataSource. KHÔNG bypass layers.
2. **No Context in ViewModel**: ViewModel KHÔNG giữ reference đến Context, Activity, hoặc View.
3. **Repository = SSOT**: Repository là single source of truth. UI chỉ observe Room DB qua Flow.
4. **Coroutines everywhere**: Tất cả I/O operations PHẢI chạy trên `Dispatchers.IO`.
5. **StateFlow for state, SharedFlow for events**: UI state dùng `StateFlow`, one-time events dùng `SharedFlow`.
6. **Hilt DI**: Inject tất cả dependencies. KHÔNG tạo instance thủ công.
7. **Compose-only UI**: KHÔNG sử dụng XML layouts. 100% Jetpack Compose.

## Coding Rules

### Kotlin Style

```kotlin
// ✅ DO: Use data classes for models
data class SoldierInfo(
    val nodeId: Int,
    val latitude: Double,
    val longitude: Double,
    val heartRate: Int,
    val spo2: Int,
    val status: NodeStatus
)

// ❌ DON'T: Use mutable public properties
class SoldierInfo {
    var nodeId: Int = 0  // BAD
}
```

```kotlin
// ✅ DO: Use sealed class for states
sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data class Connected(val deviceName: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

// ❌ DON'T: Use Int constants for states
const val STATE_DISCONNECTED = 0  // BAD
```

```kotlin
// ✅ DO: Use callbackFlow for hardware bridges
fun observeSerialData(): Flow<ByteArray> = callbackFlow {
    val listener = object : SerialInputOutputManager.Listener {
        override fun onNewData(data: ByteArray) {
            trySend(data)
        }
        override fun onRunError(e: Exception) {
            close(e)
        }
    }
    awaitClose { /* cleanup */ }
}

// ❌ DON'T: Use callbacks directly in ViewModel
```

```kotlin
// ✅ DO: Collect with lifecycle awareness in Compose
val soldiers by viewModel.soldiers.collectAsStateWithLifecycle()

// ❌ DON'T: Use collectAsState() without lifecycle
val soldiers by viewModel.soldiers.collectAsState()  // BAD - leaks
```

### Binary Protocol Rules

```kotlin
// ✅ DO: Use ByteBuffer with LITTLE_ENDIAN for packet parsing
val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
val nodeId = buffer.short.toUShort().toInt()
val latitude = buffer.float

// ❌ DON'T: Parse bytes manually with bit shifting
val nodeId = (data[0].toInt() and 0xFF) or ((data[1].toInt() and 0xFF) shl 8)  // Error-prone
```

```kotlin
// ✅ DO: Validate CRC before processing
fun parsePacket(frame: ByteArray): SoldierPacket? {
    if (frame.size != PACKET_SIZE) return null
    if (!validateCRC16(frame)) return null  // Always validate first
    return deserialize(frame)
}

// ❌ DON'T: Skip CRC validation
```

### Map Rules

```kotlin
// ✅ DO: Update markers on background thread, post to main
withContext(Dispatchers.Default) {
    val geoJson = buildGeoJsonFromSoldiers(soldiers)
    withContext(Dispatchers.Main) {
        mapSource.setGeoJson(geoJson)
    }
}

// ❌ DON'T: Build GeoJSON on main thread with many markers
```

## Comment Standards

```kotlin
// ✅ GOOD: Explain WHY, not WHAT
// CRC is calculated on first 38 bytes (excluding the crc16 field itself)
// to match the firmware's packetCalculateCRC() in packet.h
val crc = crc16CCITT(data, 0, PACKET_SIZE - 2)

// ❌ BAD: Stating the obvious
// Calculate CRC
val crc = crc16CCITT(data, 0, PACKET_SIZE - 2)
```

## Versioning

- **Scheme**: SemVer (MAJOR.MINOR.PATCH)
- **Current**: 0.1.0
- **Version code**: MAJOR * 10000 + MINOR * 100 + PATCH

## Git Conventions

### Commit Messages (Conventional Commits)

```
feat(map): add soldier marker with health indicator
fix(serial): handle USB disconnect during data transfer
refactor(comm): extract PacketParser from UsbSerialManager
docs(readme): add build instructions
test(crc): add CRC16-CCITT validation tests
```

### Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Stable releases |
| `develop` | Integration branch |
| `feature/*` | Feature development |
| `fix/*` | Bug fixes |

## Changelog Standards

Follow [Keep a Changelog](https://keepachangelog.com/) format:

```markdown
## [0.1.0] - 2026-04-27
### Added
- USB OTG serial connection to Gateway
- Offline map with MapLibre + MBTiles
- Real-time soldier tracking on map
- Node status table with health indicators
```

## Quality Gates

| Gate | Tool | Threshold |
|------|------|-----------|
| Lint | Android Lint | 0 errors |
| Unit Tests | JUnit 5 + MockK | Must pass |
| CRC Test | Custom test | Must match firmware output |
| Packet Parse | Custom test | Must match Python server output |
| Compose Preview | Android Studio | All previews render |

## Stack-Specific Rules

### MapLibre

- **DO**: Load MBTiles from `context.filesDir`, never from assets directly
- **DO**: Use GeoJSON source for dynamic markers
- **DO**: Batch marker updates, don't update one by one
- **DON'T**: Use Mapbox SDK (different license, different API)
- **DON'T**: Make network requests for tiles (100% offline)

### Room

- **DO**: Use `suspend` for insert/update/delete
- **DO**: Return `Flow` for queries
- **DO**: Define indices on frequently queried columns (nodeId, timestamp)
- **DON'T**: Use `allowMainThreadQueries()` in production
- **DON'T**: Store binary blobs in Room (use file system for MBTiles)

### USB Serial

- **DO**: Request USB permission via `UsbManager.requestPermission()`
- **DO**: Handle `USB_DEVICE_ATTACHED` / `USB_DEVICE_DETACHED` intents
- **DO**: Use ForegroundService with `foregroundServiceType="connectedDevice"`
- **DON'T**: Keep serial port open without ForegroundService
- **DON'T**: Read serial on main thread
