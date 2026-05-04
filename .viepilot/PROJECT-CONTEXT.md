# PROJECT-CONTEXT — Mesh Pi5 Server

## Domain Knowledge

Hệ thống quản lý và giám sát mạng lưới Mesh LoRa cho các đơn vị hoạt động tại thực địa. Mỗi soldier mang một node T-Beam (ESP32 + GPS + LoRa 433MHz) gửi dữ liệu vị trí và vitals qua sóng LoRa Mesh đến Gateway. Gateway kết nối USB Serial vào Raspberry Pi 5, nơi chạy toàn bộ phần mềm backend/frontend phục vụ dashboard chiến thuật qua WiFi cho nhiều thiết bị đầu cuối cùng lúc.

## Business Rules

1. **Offline-first:** Hệ thống phải hoạt động 100% không cần Internet. Tất cả dữ liệu, bản đồ, và AI đều chạy cục bộ trên Pi 5.
2. **Multi-client:** Nhiều thiết bị (tablet, phone, laptop) có thể kết nối WiFi AP cùng lúc để xem dashboard.
3. **Real-time:** Vị trí node phải cập nhật trên bản đồ trong vòng < 1 giây kể từ khi Gateway nhận được gói LoRa.
4. **Protocol compatibility:** Giữ nguyên Binary Packet Protocol 40 bytes để tương thích firmware mesh hiện tại (không cần flash lại node).
5. **Resource-aware:** Pi 5 4GB RAM — backend + frontend + SQLite + Nginx phải tổng cộng < 1GB RAM để dành cho AI tương lai.

## Conventions

- Backend Python: PEP 8, type hints bắt buộc, docstrings Google style.
- Frontend TypeScript: ESLint + Prettier, functional components, hooks only.
- API: RESTful naming (`/api/nodes`, `/api/events`), JSON response.
- WebSocket: JSON messages format `{ "type": "node_update", "data": {...} }`.
- Git: Conventional Commits (`feat:`, `fix:`, `chore:`).

## Constraints

- **Hardware:** Raspberry Pi 5 (4GB RAM), Raspberry Pi OS Bookworm 64-bit.
- **Serial:** USB Serial CP2102/CH340, 115200 baud, 8N1.
- **WiFi:** Built-in WiFi (wlan0), 2.4GHz/5GHz, WPA2.
- **Storage:** MicroSD hoặc USB SSD, cần ~500MB cho OS + app + map data.
- **Power:** 5V/5A USB-C, có thể chạy bằng powerbank cho triển khai cơ động.

<product_vision>

## Product Vision

### Project Scope
Xây dựng hệ thống Web-based Command & Control trên Raspberry Pi 5 để thay thế ứng dụng Android native hiện tại. Pi 5 đóng vai trò server trung tâm: kết nối Gateway, phát WiFi, chạy backend/frontend. Mọi thiết bị chỉ cần mở trình duyệt để truy cập dashboard chiến thuật.

### Phase Overview

| Phase | Tên | Mục tiêu chính |
|-------|-----|----------------|
| 1 | Pi 5 Foundation & Serial Bridge | Backend FastAPI đọc Serial, WebSocket, REST API, SQLite |
| 2 | React Web Dashboard | MapLibre GL JS offline, Tactical Panel, real-time data |
| 3 | Two-way Command & Node Mgmt | Gửi lệnh, cấu hình node từ xa, OTA firmware |
| 4 | WiFi AP Auto-Setup & Deploy | Script tự động AP, systemd, Nginx, install script |
| 5 | AI Pathfinding | A* tìm đường ngắn nhất offline |
| 6 | AI Vision Integration | OpenCV/YOLO nhận diện mục tiêu từ camera |

### Anti-goals
- ❌ Không xây dựng App native (Android/iOS) — dùng trình duyệt web.
- ❌ Không yêu cầu Internet — 100% offline.
- ❌ Không thay đổi firmware mesh hiện tại — giữ nguyên protocol.
- ❌ Không hỗ trợ Windows/macOS server — chỉ Raspberry Pi OS.

</product_vision>

## User Stories & Use Cases

### Actors
- **Commander (Chỉ huy):** Người sử dụng chính, truy cập dashboard qua tablet/laptop.
- **Viewer (Quan sát viên):** Xem bản đồ và trạng thái node (read-only).

### Use Cases
1. Commander mở trình duyệt → xem vị trí tất cả soldier trên bản đồ offline.
2. Commander chọn 1 node → xem chi tiết vitals (nhịp tim, SpO2, nhiệt độ, pin).
3. Commander gửi lệnh broadcast → tất cả nodes nhận qua LoRa.
4. Commander vẽ vùng geofence → hệ thống tự động cảnh báo node vi phạm.
5. Commander yêu cầu tìm đường → AI tính tuyến ngắn nhất đến mục tiêu.
6. Nhiều người cùng kết nối WiFi AP → xem dashboard đồng thời.
