# Brownfield Import — Mesh Soldier Tracker

Imported by ViePilot `/vp-crystallize --brownfield` on 2026-04-23.

## Scan Report

```yaml
project_name: "Mesh Soldier Tracker"
primary_language: "C++ / Python"
gap_tier: ASSUMED
version: "v1.0.0"
inception_year: 2026

modules:
  - name: gateway_node
    primary_language: C++
    framework: Arduino/ESP32
    entry_point: src/main.cpp
    purpose: LoRa gateway — receives soldier packets over LoRa, forwards via USB Serial to PC
    gap_tier: DETECTED

  - name: soldier_node_production
    primary_language: C++
    framework: Arduino/ESP32 + FreeRTOS
    entry_point: src/main.cpp
    purpose: Wearable soldier node — reads sensors (IMU/HR/SpO2/GPS/env), transmits 40-byte LoRa packets
    gap_tier: DETECTED

  - name: server
    primary_language: Python
    framework: Flask 3.0.0 + gevent
    entry_point: app.py
    purpose: PC dashboard — parses serial, REST API, alert engine, team management, web UI
    gap_tier: DETECTED

stacks:
  - C++17 / Arduino framework
  - ESP32 (TTGO T-Beam) / FreeRTOS
  - PlatformIO build system
  - LoRa SX1276 @ 433 MHz
  - Python 3.x / Flask 3.0.0 / gevent
  - Sensors: MPU6050, MAX30102, BME680, GPS (TinyGPS++), SSD1306 OLED
  - XPowersLib (AXP2101 PMU)

polyrepo_hints: []
open_questions: []
```
