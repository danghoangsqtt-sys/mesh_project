# Changelog

Tất cả các thay đổi đáng chú ý của dự án Mesh Command Android App sẽ được ghi chép tại đây.

## [2.0.0] - 2026-04-28 (Milestone 2 - Advanced Tactical)

### Thêm Mới (Added)
- **Offline Heatmap & Clusters:** Tích hợp tính năng hiển thị bản đồ nhiệt dữ liệu mật độ và thuật toán gom nhóm Marker thông minh qua SDK MapLibre.
- **3D Terrain & Tactical Compass:** Bản đồ hỗ trợ xem nghiêng 3D địa hình và tích hợp la bàn chỉ hướng tác chiến.
- **OTA Firmware via USB:** Tính năng nạp Firmware từ xa cho các Node con, phát Broadcast chia chunk từ Cổng USB Gateway qua sóng Mesh.
- **Interactive Geofencing:** Vẽ và định nghĩa vùng An toàn (Safe) / Nguy hiểm (Danger) trực tiếp lên bản đồ chiến thuật bằng thao tác click.
- **Real-time Geofence Alert:** Thuật toán Ray-Casting phát hiện vi phạm hàng rào thời gian thực.
- **Gateway Alert Sync:** Khi xảy ra vi phạm Geofence, tự động gửi ngược luồng Broadcast báo động (SOS) về USB để Gateway báo động toàn hệ thống Mesh.
- **Bộ Kiểm thử:** Thêm Unit Test toàn vẹn dữ liệu cho GeofenceChecker và hệ thống Test E2E Dataflow (Jetpack Compose, Room In-Memory).

### Sửa đổi (Changed)
- **Two-way Command:** Nâng cấp hệ thống hiển thị ACK trả về, báo tin "Delivered" khi gửi lệnh xuống Firmware.
- **Remote Config:** Mở rộng giao diện điều chỉnh Tx Rate và cấu hình LoRa Preferences cho thiết bị từ xa.

## [1.1.0] - 2026-04-28

### Sửa đổi (Changed)
- Cập nhật giao diện UI để tương đồng với phiên bản Desktop (Parity).

## [1.0.0] - 2026-04-28

### Thêm Mới (Added)
- Khởi tạo dự án Mesh LilyGo App Android với kết nối USB Serial (OTG).
- Room Database theo dõi lịch sử vị trí (Trails) và trạng thái quân nhân (Status).
- MapLibre cơ bản hiển thị tọa độ GPS offline.
