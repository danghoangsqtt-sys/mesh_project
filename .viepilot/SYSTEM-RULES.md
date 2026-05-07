# SYSTEM-RULES.md — Mesh Soldier Tracker

## Architecture Rules

1. **No logic in `main.cpp` beyond task creation and hardware init.** All business logic lives in dedicated modules (`.cpp`/`.h` pairs or Python `*_manager.py`).
2. **Shared globals between tasks/ISR must be protected.** Use `SemaphoreHandle_t` (binary or mutex) or `portENTER_CRITICAL` for ISR context.
3. **Packet struct is frozen at 40 bytes.** Any addition requires a version field and coordinated update of firmware + gateway + server simultaneously.
4. **CRC must be validated before any packet processing.** Silent drop on failure — no partial processing.
5. **Gateway node is receive-only by default.** Broadcast messages are opt-in from the server via explicit API call.

## Coding Rules — C++ (Firmware)

### Timing

```cpp
// ✅ DO — use vTaskDelayUntil for periodic tasks
TickType_t wake = xTaskGetTickCount();
while (true) {
    doWork();
    vTaskDelayUntil(&wake, pdMS_TO_TICKS(cfg::LORA_PERIOD_MS));
}

// ❌ DON'T — delay() blocks the scheduler core
while (true) {
    doWork();
    delay(1000);
}
```

### Shared State

```cpp
// ✅ DO — protect shared globals
portENTER_CRITICAL(&mux);
totalMessagesReceived++;
portEXIT_CRITICAL(&mux);

// ❌ DON'T — bare increment from multiple tasks/ISR
totalMessagesReceived++;  // data race
```

### Stack Sizing

- Never guess stack size. After adding functionality, read `uxTaskGetStackHighWaterMark(NULL)` and keep ≥25% headroom.
- Minimum recommendations: GPS task 3072 words, sensor/fusion tasks 4096 words.

### Constants

```cpp
// ✅ DO — typed constexpr in namespace
namespace cfg { constexpr uint32_t LORA_PERIOD_MS = 1000; }

// ❌ DON'T — untyped macro
#define LORA_PERIOD_MS 1000
```

## Coding Rules — Python (Server)

### Concurrency

```python
# ✅ DO — use gevent primitives
import gevent
greenlet = gevent.spawn(my_function)

# ❌ DON'T — mix threading.Thread with gevent
import threading
t = threading.Thread(target=my_function)  # breaks gevent cooperative scheduling
```

### Error handling

```python
# ✅ DO — catch specific exceptions at boundaries
try:
    packet = parse_packet(raw_bytes)
except struct.error as e:
    logger.warning("Malformed packet: %s", e)
    return

# ❌ DON'T — bare except or swallowed errors
try:
    packet = parse_packet(raw_bytes)
except:
    pass
```

### Config

- All thresholds in `.conf` files — never hardcode health alert values in Python.
- Use `configparser` with `.getfloat()` / `.getint()` with explicit fallback defaults.

## Comment Standards

```cpp
// ✅ Good comment — explains the WHY (non-obvious invariant)
// CRC covers bytes 0..(PACKET_SIZE-3); last 2 bytes ARE the CRC
pkt->crc16 = crc16_ccitt((uint8_t*)pkt, PACKET_SIZE - 2);

// ❌ Bad comment — restates the code
// Calculate CRC and store in packet
pkt->crc16 = crc16_ccitt((uint8_t*)pkt, PACKET_SIZE - 2);
```

No multi-line comment blocks. No docstring novels.

## Versioning

SemVer: `MAJOR.MINOR.PATCH`
- MAJOR: breaking packet format change (requires coordinated flash of all devices)
- MINOR: new feature (backward-compatible)
- PATCH: bug fix

## Git Conventions

Conventional Commits:
```
feat(soldier): add humidity flag to status bitmask
fix(gateway): add mutex around activeNodes array
docs(server): update alert threshold documentation
refactor(soldier): migrate gpsTask to 3072-word stack
test(server): add pytest config and fixture for packet parsing
chore: add root .gitignore
```

## Quality Gates

Before any PR / merge:
- [ ] `pio run` compiles without warnings for both `gateway_node` and `soldier_node_production`
- [ ] `pytest server/` passes (when test suite is configured)
- [ ] No `.bak` files committed
- [ ] No `delay()` calls inside FreeRTOS task loops
- [ ] Stack high-water marks checked after structural task changes
- [ ] `PACKET_SIZE` still equals 40 bytes after any struct change

## Stack-Specific Rules

### ESP32 / FreeRTOS

- Pin `sensorTask` and `fusionTask` to **Core 0**; `loraTask` and `displayTask` to **Core 1** (avoids Wi-Fi coexistence issues even if Wi-Fi not used).
- `loop()` runs on Core 1 — keep it lightweight (polling only).
- ISR callbacks must be in IRAM: `IRAM_ATTR void onReceive(int packetSize)`.
- Do not call `Serial.print` from ISR context.

### Flask / gevent

- `app.run(debug=False)` in all environments — debug mode is single-threaded and incompatible with gevent.
- Use `Response(stream_with_context(...))` for any streaming endpoint.
- All `/api/*` routes return JSON via `jsonify()`.
