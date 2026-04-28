# Kiến trúc Hệ thống (Architecture)

Ứng dụng **Mesh Command** áp dụng mô hình **MVVM (Model-View-ViewModel)** thuần túy kết hợp cùng **Clean Architecture** sử dụng bộ thư viện Jetpack (Compose, Room, Coroutines) và Hilt cho Dependency Injection.

## 1. Thành phần Giao tiếp Phần cứng (Hardware Comm)
- **`MeshForegroundService`**: Service chạy ngầm chịu trách nhiệm giữ kết nối với USB OTG ngay cả khi thu nhỏ App. Nó đăng ký `BroadcastReceiver` để nhận biết sự kiện cắm/rút USB.
- **`UsbSerialManager`**: Giao tiếp trực tiếp với vi điều khiển (CP210x, CH340) thông qua API thư viện `hoho.android.usbserial`. Cung cấp dòng chảy (Flow) ByteArray cho Service phân giải.
- **`PacketParser`**: Nhận luồng chuỗi từ USB, dùng regex và logic bóc tách thành các đối tượng `SoldierPacket`, `CommandPacket`...

## 2. Tầng Quản lý Dữ liệu (Repository & Room)
- **`SoldierRepository`**: Đây là Single Source of Truth (SSOT).
  - Thu thập `SoldierPacket` từ Service.
  - Sử dụng `GeofenceChecker` để chạy kiểm tra tọa độ thời gian thực (Geofencing logic).
  - Ghi đối tượng vào `MeshDatabase` qua `SoldierDao`, `EventDao`, `PositionHistoryDao`.
- **Luồng hai chiều (Two-way)**: `SoldierRepository` duy trì một `MutableSharedFlow<String>` chứa các lệnh Outgoing. Service sẽ collect luồng này và đẩy byte xuống USB.

## 3. Tầng Giao diện (Presentation & UI)
- **`MapViewModel` / `TacticalViewModel`**: Thu thập dữ liệu từ Repository (thông qua StateFlow / Kotlin Coroutines). Trạng thái (State) luôn phản ánh chính xác cấu trúc CSDL hiện tại.
- **Jetpack Compose UI (`MapScreen`, `TacticalPanel`)**:
  - `AndroidView`: Wrap SDK MapLibre Native bên trong Compose.
  - Tích hợp GeoJsonSource (để render Marker, Cluster, Heatmap) và PolygonOptions để vẽ vùng chiến thuật.
  - Dữ liệu luôn cập nhật (Recomposition) khi State Flow thay đổi.

## Sơ đồ Luồng Dữ liệu Điển hình:
`USB Device -> UsbSerialManager -> MeshForegroundService -> SoldierRepository -> (Room DB + GeofenceChecker) -> MapViewModel -> Jetpack Compose UI`
