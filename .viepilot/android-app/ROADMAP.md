# ROADMAP — Mesh Command Android App

> Lộ trình phát triển ứng dụng Android chỉ huy cho mạng Mesh LoRa.

---

## Phase 1 — Core Command Center

**Mục tiêu:** Thay thế desktop Python app, chạy đầy đủ trên tablet Android.

### Task 1.1 — Project Setup & Build System

**Mô tả:** Khởi tạo project Android Studio với Kotlin, Jetpack Compose, Hilt DI, và Gradle Version Catalog.

**Acceptance Criteria:**
- [ ] Project build thành công với `./gradlew assembleDebug`
- [ ] Gradle Version Catalog (`libs.versions.toml`) có tất cả dependencies
- [ ] Hilt DI cấu hình xong (`@HiltAndroidApp`, `@AndroidEntryPoint`)
- [ ] `AndroidManifest.xml` có: landscape-only, USB permissions, foreground service type
- [ ] Military green theme (Material 3 custom ColorScheme)

**Verification:**
```bash
./gradlew assembleDebug
./gradlew lint
```

---

### Task 1.2 — Binary Protocol Layer (comm/)

**Mô tả:** Implement packet parser, CRC16-CCITT, và frame extractor tương thích 100% với firmware C struct.

**Acceptance Criteria:**
- [ ] `CRC16.kt` — CRC16-CCITT (poly 0x1021, init 0xFFFF), output khớp firmware
- [ ] `PacketParser.kt` — Parse 40-byte SoldierPacket (ByteBuffer, LITTLE_ENDIAN)
- [ ] `PacketParser.kt` — Build SoldierPacket cho TX commands
- [ ] Frame extraction: tìm 0xAA, đọc 40 bytes, kiểm tra 0x55
- [ ] Handle gateway GPS sideband (`GW_GPS:lat,lon\n`)
- [ ] Unit tests với test vectors từ firmware actual output

**Verification:**
```bash
./gradlew test --tests "com.meshcommand.app.comm.*"
```

---

### Task 1.3 — USB OTG Serial Manager

**Mô tả:** Implement USB Serial communication với auto-detect ESP32 T-Beam.

**Acceptance Criteria:**
- [ ] `UsbSerialManager.kt` sử dụng `usb-serial-for-android`
- [ ] Auto-detect USB device (VID/PID filtering: CP210x=0x10C4:0xEA60, CH340=0x1A86:0x7523)
- [ ] Kết nối 115200 baud, 8N1
- [ ] `callbackFlow` bridge — serial bytes → Flow<ByteArray>
- [ ] Handle USB attach/detach intents
- [ ] Permission request flow via `UsbManager.requestPermission()`
- [ ] Reconnect logic khi USB bị rút rồi cắm lại

**Verification:**
- Test với ESP32 thật qua USB OTG
- Test mock mode cho development

---

### Task 1.4 — ForegroundService

**Mô tả:** Background service giữ kết nối USB Serial liên tục.

**Acceptance Criteria:**
- [ ] `MeshForegroundService.kt` với `foregroundServiceType="connectedDevice"`
- [ ] Persistent notification hiển thị connection status
- [ ] Service start/stop từ UI
- [ ] Serial data forwarding tới Repository qua Binder hoặc SharedFlow
- [ ] Handle service lifecycle (onCreate → onStartCommand → onDestroy)

**Verification:**
- App chạy background, serial vẫn nhận data
- Notification hiển thị đúng trạng thái

---

### Task 1.5 — Data Layer (Room DB + Repository)

**Mô tả:** Local database và Repository pattern cho soldier data.

**Acceptance Criteria:**
- [ ] `SoldierEntity` Room entity (nodeId, lat, lon, HR, SpO2, temp, battery, status, timestamp)
- [ ] `SoldierDao` với: insert/update (UPSERT), query all (Flow), query by nodeId
- [ ] `EventEntity` cho event log (timestamp, nodeId, eventType, message)
- [ ] `SoldierRepository` — bridge giữa serial data và Room DB
- [ ] Node timeout logic (30s → offline status)
- [ ] Alert level calculation (Level 0/1/2)

**Verification:**
```bash
./gradlew test --tests "com.meshcommand.app.data.*"
```

---

### Task 1.6 — MapLibre Offline Map

**Mô tả:** Tích hợp MapLibre Native với MBTiles offline.

**Acceptance Criteria:**
- [ ] MapLibre SDK tích hợp thành công
- [ ] Load MBTiles từ `context.filesDir/maps/`
- [ ] Custom military style JSON (green tones, high contrast)
- [ ] Soldier markers với icon theo trạng thái (OK/Warning/Critical/Offline)
- [ ] Gateway marker (hình khác biệt)
- [ ] Tap marker → hiển thị popup chi tiết
- [ ] Camera controls: zoom, pan, rotate, pitch
- [ ] Center map vào soldier khi tap từ table

**Verification:**
- Map render MBTiles offline
- Markers di chuyển real-time

---

### Task 1.7 — Tactical Panel UI (Compose)

**Mô tả:** Bảng điều khiển chiến thuật bên phải màn hình.

**Acceptance Criteria:**
- [ ] **Connection Bar** — USB/WiFi toggle, status indicator, device name
- [ ] **Node Table** — LazyColumn với: ID, Team, Status (color-coded), HR, SpO2, Temp, Battery
- [ ] **Command Panel** — Message input, target selector (Broadcast/Team/Individual), Send button
- [ ] **Event Log** — Compact scrollable log hiển thị alerts
- [ ] **Status Bar** — Active nodes count, last RX time, GPS status
- [ ] Tap row → center map + highlight marker
- [ ] Military green theme nhất quán

**Verification:**
- UI render đúng trên tablet 10"+ landscape
- Data binding reactive khi data thay đổi

---

### Task 1.8 — Main Activity & Layout Assembly

**Mô tả:** Ghép nối toàn bộ components vào split-screen layout.

**Acceptance Criteria:**
- [ ] `MainActivity.kt` — Single activity + Compose
- [ ] Split-screen layout: Map (65%) + Tactical Panel (35%)
- [ ] ViewModels: `MapViewModel`, `TacticalViewModel`, `ConnectionViewModel`
- [ ] Wire: Service → Repository → ViewModel → Compose
- [ ] Landscape-only lock
- [ ] Splash screen (military style)

**Verification:**
- End-to-end: cắm USB OTG → data hiển thị trên map + table
- APK install và chạy trên tablet thật

---

## Phase 2 — Enhanced Situational Awareness

**Mục tiêu:** Nâng cao nhận thức tình huống cho chỉ huy.

### Task 2.1 — Path Trail

- [ ] Vẽ polyline đường di chuyển cho mỗi soldier
- [ ] Configurable: hiển thị trail 30 phút / 1 giờ / toàn bộ
- [ ] Màu trail theo team

### Task 2.2 — WiFi AP Communication

- [ ] `WifiCommManager.kt` — TCP socket client
- [ ] Kết nối WiFi AP của Gateway
- [ ] Same packet protocol qua TCP
- [ ] Auto-switch USB ↔ WiFi
- [ ] **Requires:** Gateway firmware update

### Task 2.3 — Night Mode

- [ ] Red-on-black theme cho sử dụng ban đêm
- [ ] Toggle nhanh Day/Night mode
- [ ] Map style dark variant
- [ ] Dim screen option

### Task 2.4 — Advanced Alert System

- [ ] Push notification cho SOS/Man Down
- [ ] Vibration pattern cho từng loại alert
- [ ] Alert sound (short beep)
- [ ] Alert history với timestamp
- [ ] Alert acknowledgment (dismiss/acknowledge)

### Task 2.5 — Team Management

- [ ] CRUD teams (Alpha, Bravo, Charlie...)
- [ ] Assign soldiers to teams
- [ ] Team colors trên map
- [ ] Filter map/table theo team
- [ ] Load/save team config (Room DB)

### Task 2.6 — Offline Map Manager

- [ ] UI để chọn MBTiles file
- [ ] Hiển thị danh sách available map packs
- [ ] File picker để import .mbtiles
- [ ] Map metadata (coverage area, zoom levels, size)

---

## Phase 3 — Advanced Tactical Features

**Mục tiêu:** Tính năng chiến thuật nâng cao.

### Task 3.1 — Geofencing

- [ ] Vẽ polygon trên map (vùng an toàn / cấm)
- [ ] Alert khi soldier ra ngoài vùng an toàn
- [ ] Alert khi soldier vào vùng cấm
- [ ] Persistent geofences (Room DB)

### Task 3.2 — Distance/Bearing Calculator

- [ ] Tap 2 điểm trên map → hiển thị khoảng cách + phương vị
- [ ] Ruler tool
- [ ] Haversine formula

### Task 3.3 — Waypoint System

- [ ] Đặt waypoint trên map
- [ ] Gửi tọa độ waypoint xuống soldier node
- [ ] Hiển thị hướng đi tới waypoint

### Task 3.4 — Mission Replay

- [ ] Record toàn bộ movement data (Room DB)
- [ ] Playback timeline với speed control
- [ ] Overlay quá khứ lên bản đồ hiện tại

### Task 3.5 — Multi-Gateway Support

- [ ] Kết nối nhiều Gateway cùng lúc (USB + WiFi)
- [ ] Merge data từ nhiều gateway
- [ ] De-duplicate packets (by nodeId + timestamp)

### Task 3.6 — Encrypted Communication

- [ ] AES-128 encryption cho packet payload
- [ ] Key exchange mechanism
- [ ] **Requires:** Firmware update trên tất cả nodes
