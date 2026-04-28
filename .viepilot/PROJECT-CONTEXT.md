# PROJECT-CONTEXT — Mesh Command Android App

## Domain Knowledge

### Bối cảnh chiến trường

Ứng dụng này phục vụ chỉ huy quân sự trên chiến trường, nơi:
- **Không có Internet** — Mọi thứ phải hoạt động offline
- **Điều kiện khắc nghiệt** — Ánh sáng mạnh (ban ngày), tối hoàn toàn (ban đêm)
- **Độ tin cậy tối đa** — Lỗi phần mềm có thể dẫn đến mất mạng người
- **Thời gian thực** — Vị trí và tình trạng lính phải cập nhật liên tục

### Hệ thống Mesh LoRa

- **Soldier Node** (T-Beam V1.2): GPS + IMU + Heart Rate + SpO2 + BME280 → truyền qua LoRa 433MHz
- **Gateway Node** (T-Beam V1.2): Nhận LoRa → chuyển tiếp qua USB Serial → Ứng dụng chỉ huy
- **Protocol**: Binary packed struct 40 bytes, CRC16-CCITT, frame markers 0xAA/0x55
- **Tần số**: 433 MHz, SF9, BW 125kHz, Sync Word 0x12

### Vai trò người dùng

| Vai trò | Mô tả |
|---------|--------|
| **Commander (Chỉ huy)** | Người dùng duy nhất của tablet, theo dõi toàn bộ đơn vị |

### Luồng hoạt động chính

1. Chỉ huy bật tablet, cắm cáp OTG vào Gateway
2. App tự động detect USB device và bắt đầu nhận dữ liệu
3. Bản đồ hiển thị vị trí real-time của tất cả soldier nodes
4. Bảng bên phải hiển thị chi tiết sức khỏe và trạng thái
5. Chỉ huy gửi lệnh (broadcast hoặc individual) qua command panel
6. Alerts tự động khi phát hiện SOS, Man Down, Low Battery

## Business Rules

1. **Node timeout**: Node không gửi dữ liệu trong 30 giây → đánh dấu Offline
2. **Alert levels**:
   - Level 0 (OK): Tất cả thông số bình thường
   - Level 1 (Warning): HR > 120 hoặc < 50, SpO2 < 95%, Temp > 38°C, Low Battery
   - Level 2 (Critical): HR > 150 hoặc < 40, SpO2 < 90%, MAN_DOWN, ALERT flag
3. **Battery thresholds**: Low Battery < 3.3V, Critical Battery < 3.0V
4. **GPS precision**: Hiển thị 6 chữ số thập phân (precision ~0.1m)
5. **Message priority**: SOS/Man Down alerts phải hiển thị ngay lập tức với âm thanh/rung

## Conventions

### Naming

| Context | Convention | Example |
|---------|-----------|---------|
| Kotlin files | PascalCase | `SoldierRepository.kt` |
| Compose | PascalCase function | `TacticalPanel()` |
| Variables | camelCase | `soldierList` |
| Constants | SCREAMING_SNAKE | `PACKET_SIZE` |
| Room entities | PascalCase + Entity suffix | `SoldierEntity` |
| Room DAOs | PascalCase + Dao suffix | `SoldierDao` |
| ViewModels | PascalCase + ViewModel suffix | `MapViewModel` |
| Package names | lowercase | `com.meshcommand.app` |

### Color System (Military Green)

| Token | Hex | Usage |
|-------|-----|-------|
| `MIL_BG_DARK` | #3B5323 | Nền chính |
| `MIL_BG_LIGHT` | #4A5D23 | Card/Surface |
| `MIL_ACCENT` | #556B2F | Viền, divider |
| `MIL_TEXT_PRIMARY` | #FFFFFF | Text chính |
| `MIL_TEXT_SECONDARY` | #E0E0E0 | Text phụ |
| `STATUS_OK` | #4CAF50 | Trạng thái OK |
| `STATUS_WARN` | #FFC107 | Cảnh báo |
| `STATUS_CRIT` | #F44336 | Nguy hiểm |
| `STATUS_OFFLINE` | #9E9E9E | Mất kết nối |

## Constraints

1. **Offline-only**: KHÔNG có network call nào trong toàn bộ app
2. **Landscape-only**: `screenOrientation="landscape"` trong manifest
3. **Single activity**: Sử dụng single Activity + Compose navigation
4. **Min SDK 29**: Android 10+ (phần lớn tablet quân sự từ 2020+)
5. **No Google Play Services**: App không phụ thuộc GMS
6. **Battery conscious**: Optimize cho battery life (tablet có thể hoạt động nhiều giờ)
7. **Binary compatibility**: Packet format PHẢI khớp 100% với firmware C struct

## Product Vision

### Scope

Ứng dụng Android tablet thay thế phần mềm chỉ huy desktop (PySide6) hiện tại, cho phép chỉ huy mang theo máy tính bảng trên chiến trường thay vì phải ngồi cố định trước laptop.

### Phase Overview

| Phase | Tên | Mục tiêu |
|-------|-----|----------|
| Phase 1 | Core Command Center | Thay thế desktop app: USB Serial + Map + Table + Commands |
| Phase 2 | Enhanced Situational Awareness | Path trails, WiFi, Night mode, Alerts nâng cao |
| Phase 3 | Advanced Tactical Features | Geofencing, Waypoints, Mission Replay, Encryption |

### Anti-goals

- **KHÔNG** xây dựng social features hoặc chat app
- **KHÔNG** phụ thuộc cloud/server/internet
- **KHÔNG** hỗ trợ phone (chỉ tablet 10"+)
- **KHÔNG** thay đổi firmware protocol (tương thích ngược 100%)
- **KHÔNG** build cho iOS (chỉ Android)
