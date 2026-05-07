# STACKS.md — Project Stack Index

Maps detected stacks to global ViePilot cache locations.

| Stack | Cache Path | Status |
|---|---|---|
| C++17 / Arduino / ESP32 | `~/.viepilot/stacks/arduino-esp32/` | Not cached |
| FreeRTOS (via ESP-IDF) | `~/.viepilot/stacks/freertos-esp32/` | Not cached |
| PlatformIO | `~/.viepilot/stacks/platformio/` | Not cached |
| Python / Flask 3.0 | `~/.viepilot/stacks/flask3/` | Not cached |
| gevent | `~/.viepilot/stacks/gevent/` | Not cached |

Run `/vp-evolve` with `--research-stacks` to populate cache with official best-practice guidance.

## Key Guidance (Inline)

### ESP32 / FreeRTOS

- Use `vTaskDelayUntil` for periodic tasks — not `delay()` or `vTaskDelay`
- Always pin tasks to cores — `xTaskCreatePinnedToCore`
- ISR functions must be declared `IRAM_ATTR`
- Measure `uxTaskGetStackHighWaterMark()` after each structural task change
- Mutexes: use `portMUX_TYPE` for ISR-safe critical sections; `SemaphoreHandle_t` for task-to-task

### Python / Flask + gevent

- `debug=False` always — debug mode is single-threaded
- Use `gevent.spawn` not `threading.Thread`
- All shared state accessed from multiple greenlets needs gevent `RLock`
- Return `jsonify()` from all `/api/*` routes
