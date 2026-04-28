# Hướng dẫn Bắt đầu Nhanh (Quick Start)

Ứng dụng **Mesh Command** được thiết kế để chạy hoàn toàn Offline. Tuy nhiên, ở lần khởi tạo đầu tiên, hãy làm theo hướng dẫn sau.

## 1. Chuẩn bị Phần cứng
1. Máy tính bảng hoặc Điện thoại Android hỗ trợ **USB OTG**.
2. Một mạch **LilyGo T-Beam / T3** đã được nạp Firmware Gateway (Gateway Node).
3. Cáp USB truyền dữ liệu (Data Cable).

## 2. Cài đặt Bản đồ Offline
Để có thể xem bản đồ không cần 4G/Wifi:
1. Copy tệp `map_style_military.json` và thư mục mbtiles (dữ liệu cao độ, bản đồ) vào thư mục `files/maps` của ứng dụng. (Chỉ áp dụng với phiên bản tải qua APK thủ công).
2. Khi mở ứng dụng, bản đồ sẽ tự động kích hoạt chế độ **Dark Tactical Mode**.

## 3. Khởi động Ứng dụng & Cắm Cáp
1. Mở ứng dụng **Mesh Command**. Bạn sẽ thấy màn hình Map trống với chữ `Disconnected`.
2. Dùng cáp OTG cắm mạch Gateway vào điện thoại.
3. Android sẽ hiển thị hộp thoại xin cấp quyền **Allow Mesh Command to access {USB Device}?**. Nhấn **OK**.
4. Biểu tượng trạng thái sẽ chuyển sang **Connected**, và bạn sẽ thấy tọa độ các Node dần xuất hiện trên bản đồ (nhờ vào cơ chế đồng bộ Broadcast).

## 4. Chức năng Cơ bản
- **Chạm vào Node:** Xem thông tin Tim, SpO2, Pin và chọn Node làm mục tiêu chỉ huy.
- **Menu Bảng Điều Khiển (Phải):** Lựa chọn loại Lệnh (Command) muốn gửi xuống. Nhấn nút "Send Command" để truyền lệnh.
- **Vẽ Geofence:** (Tham khảo thêm ở trang Geofencing).
