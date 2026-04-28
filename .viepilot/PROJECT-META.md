# PROJECT-META — Mesh Command Android App

## Project Information

| Field | Value |
|-------|-------|
| **Project Name** | Mesh Command |
| **Package ID** | com.meshcommand.app |
| **Version** | 0.1.0 |
| **Min SDK** | 29 (Android 10) |
| **Target SDK** | 35 (Android 15) |
| **Compile SDK** | 35 |
| **Language** | Kotlin 2.0+ |
| **Build System** | Gradle 8.x + KTS |
| **Architecture** | MVVM + Repository |

## Organization

| Field | Value |
|-------|-------|
| **Organization** | — |
| **Website** | — |
| **Repository** | — (local project) |

## Developer

| Field | Value |
|-------|-------|
| **Lead Developer** | dangh |
| **Email** | — |

## License

| Field | Value |
|-------|-------|
| **License** | MIT |
| **Inception Year** | 2026 |

## Package Structure

```
com.meshcommand.app
├── ui                  # Compose UI screens
│   ├── map             # MapLibre map composables
│   ├── tactical        # Node table, command panel
│   ├── settings        # Connection settings
│   └── theme           # Military theme, colors, typography
├── data
│   ├── model           # SoldierPacket, NodeInfo, GatewayInfo
│   ├── repository      # SoldierRepository, SettingsRepository
│   └── local           # Room database, DAOs, entities
├── comm
│   ├── UsbSerialManager.kt    # USB OTG serial communication
│   ├── WifiCommManager.kt     # WiFi AP communication (Phase 2)
│   ├── PacketParser.kt        # Binary packet parser/builder
│   └── CRC16.kt               # CRC16-CCITT implementation
├── service
│   └── MeshForegroundService.kt  # Persistent connection service
├── viewmodel
│   ├── MapViewModel.kt
│   ├── TacticalViewModel.kt
│   └── ConnectionViewModel.kt
└── di                  # Hilt dependency injection modules
    └── AppModule.kt
```

## File Header Template

```kotlin
/*
 * Mesh Command — Tactical Command & Control for LoRa Mesh Network
 * Copyright (c) 2026 — All rights reserved
 *
 * File: {filename}
 * Description: {description}
 */
```
