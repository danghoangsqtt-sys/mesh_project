# Brainstorm Session: Mesh LilyGo Android Command App

**Date:** 2026-04-27
**Project:** `mesh_lilygo_app_android`
**Location:** `E:\data\MyProject\2026\mesh_lilygo_app_android`
**Type:** New project — Android tablet command & control application
**Mode:** Fully Offline / Field-deployable

---

## Tổng quan dự án

Xây dựng ứng dụng Android trên **máy tính bảng** để làm phần mềm **chỉ huy (Command & Control)** cho hệ thống mạng lưới Mesh LoRa. Ứng dụng hoạt động **100% offline**, sử dụng bản đồ offline, kết nối với Gateway node và các Soldier node thông qua LoRa + WiFi Mesh.

---

## Quyết định kiến trúc

### 1. Tech Stack: **Native Android (Kotlin)** ✅ QUYẾT ĐỊNH

**Lý do:**
- **USB OTG/Serial**: Kotlin + `usb-serial-for-android` (mik3y) cho phép giao tiếp trực tiếp với Gateway qua USB OTG — ổn định nhất trên chiến trường
- **WiFi Mesh**: Android native API hỗ trợ WiFi P2P và kết nối AP tốt nhất
- **Hiệu năng**: Không có overhead của framework trung gian (Flutter/React Native), quan trọng cho real-time tracking
- **Background Services**: ForegroundService native cho kết nối liên tục dù người dùng chuyển tab
- **Hardware Access**: Truy cập phần cứng sâu nhất (Bluetooth, USB, WiFi, GPS)
- **Offline**: Không phụ thuộc CDN/NPM/bất kỳ dịch vụ online nào

**Loại bỏ:**
- Flutter: Thêm layer trung gian, khó debug USB Serial
- React Native: Performance kém cho bản đồ + real-time, bridge overhead
- PWA/Capacitor: Hạn chế nghiêm trọng về USB OTG và WiFi mesh

### 2. Offline Map: **MapLibre Native** ✅ QUYẾT ĐỊNH

**Lý do:**
- OSMDroid đã **archived/maintenance mode** — rủi ro bảo mật và tương thích
- **Vector tiles** (MBTiles) — nhẹ hơn raster tiles rất nhiều, tiết kiệm bộ nhớ
- **Hardware-accelerated** rendering (OpenGL/Vulkan) — mượt khi có nhiều marker
- Hỗ trợ **3D terrain, pitch, bearing rotation** — lý tưởng cho phân tích địa hình
- **Dynamic styling** — thay đổi màu sắc bản đồ theo nhiệm vụ (ban ngày/ban đêm/chiến thuật)
- Offline tile packs hiệu quả hơn OSMDroid

**Tile source:** Download trước từ OpenStreetMap/OpenMapTiles → `.mbtiles` file lưu trong bộ nhớ thiết bị

### 3. Giao thức kết nối: **Dual-mode (USB OTG + WiFi AP)** ✅ QUYẾT ĐỊNH

**Kênh chính — USB OTG Serial:**
- Tablet cắm trực tiếp vào Gateway node qua cáp OTG
- Library: `usb-serial-for-android` (mik3y/usb-serial-for-android v3.9+)
- Protocol: Giữ nguyên binary packet protocol hiện tại (PKT_START=0xAA, PKT_END=0x55, 40-byte SoldierPacket, CRC16-CCITT)
- Ưu điểm: Ổn định nhất, không bị nhiễu RF, vừa cấp nguồn cho Gateway

**Kênh phụ — WiFi AP (soft-AP trên Gateway):**
- Gateway tạo WiFi Access Point → Tablet kết nối vào
- Giao tiếp qua TCP socket hoặc UDP
- Use case: Khi cần khoảng cách giữa tablet và gateway (ví dụ: tablet trong xe, gateway trên nóc)
- Cần firmware update cho gateway để hỗ trợ WiFi AP mode

**Tóm tắt:**
```
[Soldier Nodes] --LoRa--> [Gateway Node] --USB OTG/WiFi--> [Android Tablet App]
```

### 4. Giao diện: **Split-screen (Map-dominant)** ✅ QUYẾT ĐỊNH

**Layout cho tablet (landscape mode bắt buộc):**
```
┌─────────────────────────────────────────────────────────────┐
│ ⚙ CONNECTION BAR: [USB/WiFi toggle] [Status] [Battery]     │
├────────────────────────────┬────────────────────────────────┤
│                            │  📊 TACTICAL PANEL             │
│                            │  ┌──────────────────────────┐  │
│    🗺️ MAPLIBRE MAP          │  │  Node Table (scrollable) │  │
│    (65% width)             │  │  ID|Team|Status|HR|SpO2   │  │
│                            │  └──────────────────────────┘  │
│    - Soldier markers       │  ┌──────────────────────────┐  │
│    - Gateway marker        │  │  📡 Command & Control     │  │
│    - Path trails           │  │  [Message input]          │  │
│    - Alert overlays        │  │  [Broadcast/Team/Node]    │  │
│                            │  │  [SEND]                   │  │
│                            │  └──────────────────────────┘  │
│                            │  ┌──────────────────────────┐  │
│                            │  │  📋 Event Log (compact)   │  │
│                            │  └──────────────────────────┘  │
├────────────────────────────┴────────────────────────────────┤
│ STATUS BAR: [Active Nodes: 5] [Last RX: 2s ago] [GPS: OK]  │
└─────────────────────────────────────────────────────────────┘
```

- Bản đồ chiếm **65% chiều ngang** — focus chính
- Panel bên phải **35%** — tactical control
- **Landscape mode cố định** — tối ưu cho tablet
- Military green theme (#3b5323, #4a5d23, #556b2f) — giữ nhất quán với desktop app
- **Night mode** (red-on-black) cho sử dụng ban đêm

---

## Packet Protocol (Tương thích firmware hiện tại)

```
SoldierPacket (40 bytes, packed):
├─ nodeId          : uint16  (2 bytes)
├─ timestamp       : uint32  (4 bytes)
├─ latitude        : float32 (4 bytes)
├─ longitude       : float32 (4 bytes)
├─ heading         : float32 (4 bytes)
├─ heartRate       : uint8   (1 byte)
├─ spo2            : uint8   (1 byte)
├─ temperature     : float32 (4 bytes)
├─ humidity        : float32 (4 bytes)
├─ pressure        : float32 (4 bytes)
├─ batteryVoltage  : float32 (4 bytes)
├─ statusFlags     : uint16  (2 bytes)
└─ crc16           : uint16  (2 bytes)

Frame: [0xAA] [40-byte packet] [0x55]
CRC: CRC16-CCITT (poly 0x1021, init 0xFFFF)
```

**KHÔNG cần thay đổi firmware** — Android app sẽ parse đúng định dạng hiện có.

---

## Phases

### Phase 1 — Core Command Center (MVP)
**Mục tiêu:** Thay thế được desktop Python app, chạy trên tablet

1. **USB OTG Serial Communication**
   - Kết nối Gateway qua USB OTG
   - Parse SoldierPacket binary protocol (40 bytes)
   - CRC16-CCITT validation
   - Auto-detect USB device (VID/PID filtering cho ESP32/CP210x)

2. **Offline Map với MapLibre**
   - Load MBTiles từ bộ nhớ nội bộ
   - Hiển thị soldier markers real-time (vị trí GPS)
   - Gateway marker
   - Zoom/pan/rotate
   - Quân xanh/quân đỏ marker styles

3. **Node Table (Tactical Panel)**
   - Danh sách soldier nodes với: ID, Team, Status, HR, SpO2, Temp, Battery
   - Color-coded status (OK/Warning/Critical/Offline)
   - Tap để center map vào soldier

4. **Command & Control Panel**
   - Gửi tin nhắn Broadcast/Team/Individual
   - Tin nhắn được chuyển qua serial → Gateway → LoRa broadcast

5. **Event Log**
   - Alert log (SOS, Man Down, Low Battery)
   - Connection status changes

6. **Military Green Theme**
   - Giữ nguyên color scheme từ desktop app
   - Landscape-only orientation

### Phase 2 — Enhanced Situational Awareness
1. **Path Trail** — Vẽ đường di chuyển của mỗi soldier trên bản đồ
2. **WiFi AP Mode** — Kết nối gateway qua WiFi (cần firmware update)
3. **Night Mode** — Red-on-black theme cho tầm nhìn ban đêm
4. **Alert System nâng cao** — Push notification + vibration + sound cho SOS/Man Down
5. **Team Management** — Quản lý nhóm, gán màu, filter theo team
6. **Offline Map Manager** — Download và quản lý MBTiles tile packs

### Phase 3 — Advanced Tactical Features
1. **Geofencing** — Vẽ vùng an toàn/cấm trên bản đồ, cảnh báo khi soldier ra ngoài
2. **Distance/Bearing Calculator** — Tính khoảng cách và phương vị giữa 2 điểm
3. **Waypoint System** — Đặt điểm đích, gửi tọa độ xuống soldier node
4. **Mission Replay** — Phát lại lộ trình di chuyển từ log data
5. **Multi-Gateway** — Hỗ trợ nhiều gateway cùng lúc
6. **Encrypted Communication** — Mã hóa AES-128 cho packet

---

## Cấu trúc dự án đề xuất

```
mesh_lilygo_app_android/
├── app/
│   ├── src/main/
│   │   ├── java/com/meshcommand/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── ui/
│   │   │   │   ├── map/           # MapLibre fragment
│   │   │   │   ├── tactical/      # Node table, command panel
│   │   │   │   ├── settings/      # Connection settings
│   │   │   │   └── theme/         # Military theme, colors
│   │   │   ├── data/
│   │   │   │   ├── model/         # SoldierPacket, NodeInfo
│   │   │   │   ├── repository/    # SoldierRepository
│   │   │   │   └── local/         # Room DB for history
│   │   │   ├── comm/
│   │   │   │   ├── UsbSerialManager.kt    # USB OTG communication
│   │   │   │   ├── WifiCommManager.kt     # WiFi AP communication
│   │   │   │   ├── PacketParser.kt        # Binary packet parser
│   │   │   │   └── CRC16.kt              # CRC16-CCITT
│   │   │   ├── service/
│   │   │   │   └── MeshForegroundService.kt  # Background connection
│   │   │   └── viewmodel/
│   │   │       ├── MapViewModel.kt
│   │   │       └── TacticalViewModel.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── values/            # Military colors, strings
│   │   │   └── raw/               # Default MBTiles
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── docs/
```

---

## Dependencies chính

| Library | Version | Purpose |
|---------|---------|---------|
| `org.maplibre.gl:android-sdk` | 11.x | Offline vector map |
| `com.github.mik3y:usb-serial-for-android` | 3.9+ | USB OTG serial |
| `androidx.lifecycle:lifecycle-viewmodel-ktx` | 2.8+ | MVVM architecture |
| `androidx.room:room-runtime` | 2.6+ | Local SQLite DB |
| `org.jetbrains.kotlinx:kotlinx-coroutines` | 1.8+ | Async/reactive |
| `com.google.android.material` | 1.12+ | Material Design 3 |

---

## Open Questions

1. **Tablet model target?** — Cần biết để tối ưu resolution và test USB OTG compatibility
2. **MBTiles khu vực nào?** — Cần download trước bản đồ vùng hoạt động (Việt Nam? khu vực cụ thể?)
3. **Gateway firmware update cho WiFi AP** — Phase 2, cần lên lịch riêng
4. **Bảo mật packet** — Hiện tại chưa mã hóa, Phase 3 sẽ thêm AES-128

---

## Action Items

- [ ] Tạo project Android Studio (Kotlin, Jetpack Compose hoặc XML Views)
- [ ] Implement USB Serial manager với packet parser
- [ ] Tích hợp MapLibre + MBTiles offline
- [ ] Thiết kế Military Green theme cho Android
- [ ] Tạo Node table và Command panel
- [ ] Test với Gateway hardware thực tế

---

## Next Steps

→ `/vp-crystallize` để chuyển brainstorm thành tài liệu kiến trúc và implementation plan
