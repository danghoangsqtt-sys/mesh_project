# Mesh Command Android App

![Version](https://img.shields.io/badge/version-2.0.0-blue)
![Status](https://img.shields.io/badge/status-active-success)

Ứng dụng bảng điều khiển chiến thuật Android dành riêng cho hệ thống Mesh LilyGo. Hệ thống cho phép kết nối offline 100% qua cáp USB OTG với mạch điều khiển LoRa, cung cấp khả năng theo dõi thời gian thực, lập bản đồ tương tác và đồng bộ các thay đổi môi trường.

## Mục lục Tài liệu

Tài liệu dự án đã được phân chia rõ ràng theo từng đối tượng:

| Phần | Tệp | Mô tả |
|------|-----|-------|
| **Chỉ dẫn Bắt đầu** | [Quick Start](docs/user/quick-start.md) | Cách tải và kết nối App với Gateway lần đầu |
| **Dành cho Người dùng** | [Geofencing](docs/user/features/tactical-geofencing.md) | Hướng dẫn tính năng Cảnh báo khu vực (Geofencing) |
| **Dành cho Dev** | [Architecture](docs/dev/architecture.md) | Cấu trúc luồng dữ liệu MVVM và MapLibre |
| **Lịch sử cập nhật** | [CHANGELOG](../CHANGELOG.md) | Ghi nhận chi tiết từng phiên bản |
| **ViePilot Skills** | [Skills Reference](docs/skills-reference.md) | Danh sách skills tự động hóa đã dùng |

## Project Structure

```text
.
├── app/
│   ├── src/main/java/com/meshcommand/app/
│   │   ├── comm/         # Giao tiếp USB Serial & OTA
│   │   ├── data/         # Room Database, DAOs, Entity, Repository
│   │   ├── service/      # MeshForegroundService (Chạy nền bắt USB)
│   │   ├── tactical/     # Các thuật toán xử lý chiến thuật (Geofence, Logic)
│   │   └── ui/           # Jetpack Compose UI (MapScreen, TacticalPanel)
├── docs/                 # Tài liệu hướng dẫn sử dụng và Dev (như bảng trên)
└── .viepilot/            # Tệp quản lý tiến độ Milestone của dự án
```

## Các Tính năng Chính (v2.0.0)
- **MapLibre Offline:** 3D Terrain, Heatmap, GPS Accuracy Circles và Cluster thông minh.
- **Geofencing:** Vẽ khu vực An Toàn (Safe) & Nguy Hiểm (Restricted) trực tiếp trên bản đồ. Đồng bộ báo động qua Mesh.
- **Two-way Command & OTA:** Gửi/nhận xác nhận qua sóng LoRa, cập nhật Firmware nốt con qua sóng vô tuyến (OTA).
- **USB Serial OTG:** Kết nối không yêu cầu Wi-Fi / Internet (100% Offline Tactical Command).

## Giấy phép
Sử dụng nội bộ.
