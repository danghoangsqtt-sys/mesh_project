# PROJECT-META.md

## Project Information

| Field | Value |
|---|---|
| Project Name | Mesh Soldier Tracker |
| Short Name | mesh-soldier-tracker |
| Version | v1.0.0 |
| Inception Year | 2026 |
| Description | LoRa-based wearable mesh network for real-time soldier tracking with biometric and environmental monitoring |
| Repository | — |
| License | MIT |

## Organization

| Field | Value |
|---|---|
| Organization | Personal / Research |
| Website | — |
| Profile | none |

## Lead Developer

| Field | Value |
|---|---|
| Name | Dang Hoang |
| Email | danghoang.sqtt@gmail.com |
| GitHub | danghoang.sqtt |

## Modules

| Module | Language | Framework | Role |
|---|---|---|---|
| `gateway_node` | C++ | Arduino / ESP32 | LoRa gateway, serial bridge |
| `soldier_node_production` | C++ | Arduino / ESP32 + FreeRTOS | Sensor node, LoRa TX |
| `server` | Python | Flask 3.0.0 + gevent | Dashboard, REST API, alerts |

## File Header Template

```cpp
// Mesh Soldier Tracker — gateway_node
// Copyright (c) 2026 Dang Hoang
// SPDX-License-Identifier: MIT
```

```python
# Mesh Soldier Tracker — server
# Copyright (c) 2026 Dang Hoang
# SPDX-License-Identifier: MIT
```
