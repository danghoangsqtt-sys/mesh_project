# ROADMAP — Mesh Command Android App (Milestone 2 - v2.0.0)

> Lộ trình phát triển ứng dụng Android chỉ huy mạng Mesh LoRa — Milestone 2: Advanced Field Deployment & Pro.

---

## Phase 5 — Advanced Mapping & Visualization

**Mục tiêu:** Nâng cấp MapLibre offline lên đẳng cấp chuyên nghiệp cho điều hành chiến thuật.

### Task 5.1 — Offline Heatmap Overlay
- Implement data-driven heatmap layer trên MapLibre dựa vào mật độ node hoặc mức độ suy giảm tín hiệu (RSSI/SNR).

### Task 5.2 — Marker Clustering
- Gom nhóm (clustering) tự động khi zoom out để tránh rối bản đồ khi có >50 nodes.

### Task 5.3 — 3D Terrain & Pitch
- Kích hoạt MapLibre 3D Terrain extrusion (nếu có DEM tiles) và tối ưu hóa camera pitch cho góc nhìn 3D.

### Task 5.4 — Tactical Compass Widget
- Tích hợp la bàn trên màn hình map phản hồi theo cảm biến từ trường thiết bị (Device Rotation).

### Task 5.5 — GPS Accuracy Circles
- Vẽ vòng tròn bán kính sai số (Accuracy radius) xung quanh vị trí mỗi soldier. Mờ dần về viền.

---

## Phase 6 — Advanced Communication & OTA

**Mục tiêu:** Quản lý cấu hình node từ xa và chuẩn bị cho firmware OTA qua Mesh.

### Task 6.1 — Two-way Command Protocol
- Hoàn thiện luồng xác nhận (ACK) từ soldier node lên gateway khi nhận command.
- Hiển thị trạng thái "Delivered" hoặc "Failed" trên Event Log.

### Task 6.2 — Remote Node Configuration
- Màn hình Settings cho mỗi Node: Đổi tên, thay đổi chu kỳ gửi GPS (Tx Rate), bật/tắt cảm biến từ xa qua sóng LoRa.

### Task 6.3 — OTA Firmware Update qua Gateway
- Giao diện chọn file `.bin` (Firmware).
- Chia nhỏ file `.bin` thành các chunks và gửi tuần tự qua USB OTG xuống ESP32 Gateway để Gateway phát broadcast update cho mạng Mesh.
- Progress bar hiển thị tiến độ OTA.

---

## Phase 7 — Quality Assurance & Testing

**Mục tiêu:** Đảm bảo độ ổn định cấp quân sự (Military-grade reliability).

### Task 7.1 — Unit Testing & Logic
- Viết unit tests (JUnit 5, MockK) cho: `PacketParser`, `GeofenceChecker`, `TacticalCalculator`.

### Task 7.2 — Mock Serial & UI Testing
- Hoàn thiện Mock Serial Generator để có thể test app mà không cần phần cứng thật.
- Jetpack Compose UI Tests cơ bản cho TacticalPanel và MapScreen.

### Task 7.3 — Integration Test Suite
- Test luồng dữ liệu E2E: Fake USB Data -> Room DB -> StateFlow -> Compose UI rendering.

---

## Phase 8 — Tactical Geofencing

**Mục tiêu:** Mở rộng khả năng cảnh báo tự động trên bản đồ dựa trên khu vực địa lý.

### Task 8.1 — Interactive Geofence Drawing
- Hỗ trợ vẽ các vùng polygon (Safe Zone, Danger Zone) trực tiếp trên bản đồ MapLibre bằng cử chỉ vuốt/chạm.
- Lưu trữ tọa độ vùng Geofence vào Room Database.

### Task 8.2 — Real-time Geofence Checking
- Thêm logic kiểm tra tọa độ Node mới nhận xem có nằm trong vùng nguy hiểm hoặc rời khỏi vùng an toàn.
- Cập nhật trạng thái Node thành CRITICAL và hiển thị cảnh báo đỏ trên màn hình.

### Task 8.3 — Gateway Alert Sync
- Đồng bộ cấu hình Geofence xuống Gateway (qua USB/BLE) hoặc gửi lệnh Broadcast Alert tới các Node vi phạm thông qua mạng LoRa Mesh.

---

## Phase 9 — Management Demo Simulation

**Mục tiêu:** Tạo môi trường chạy giả lập hoàn chỉnh trên điện thoại với dữ liệu ảo để trình diễn (demo) cho cấp quản lý mà không cần Gateway thật.

### Task 9.1 — Management Demo Simulation Environment
- Thiết lập Product Flavors (`live` và `demo`) trong Gradle để tạo 2 app độc lập (App thật và App demo).
- Tạo `MockSerialManager` để sinh dữ liệu ảo (GPS nodes di chuyển, sự kiện SOS, ra vào vùng cấm) thay vì đọc từ USB.
- Thay thế Dependency Injection để bản `demo` sử dụng Mock, bản `live` sử dụng USB thật.

