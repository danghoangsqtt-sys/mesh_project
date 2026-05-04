# Quy trình Triển khai (Deployment Guide)

Tài liệu này hướng dẫn cách cài đặt toàn bộ hệ thống **Mesh Pi5 Server** lên thiết bị Raspberry Pi 5.

## 1. Yêu cầu Phần cứng
- **Raspberry Pi 5** (Khuyến nghị bản 4GB RAM trở lên).
- **Thẻ nhớ MicroSD** (Tối thiểu 32GB, Class 10/U3).
- **Bộ nguồn Pi 5** (27W USB-C Power Supply).
- **LoRa Gateway (T-Beam ESP32)** đi kèm cáp USB Data để cắm vào cổng USB của Pi 5.

## 2. Chuẩn bị Hệ điều hành (Raspberry Pi OS)
1. Sử dụng [Raspberry Pi Imager](https://www.raspberrypi.com/software/) trên máy tính cá nhân.
2. Chọn HĐH: **Raspberry Pi OS (64-bit) Bookworm** (Bản có Desktop hoặc Lite đều được).
3. Trong phần Advanced Settings (phím hình răng cưa):
   - Kích hoạt **SSH**.
   - Thiết lập username/password mặc định (ví dụ: `pi` / `raspberry`).
   - Thiết lập kết nối WiFi trung gian để Pi có mạng lúc cài đặt.
4. Ghi image ra thẻ MicroSD.

## 3. Cài đặt Hệ thống Mesh Server (Tự động)
1. Cắm thẻ nhớ, cắm nguồn bật Raspberry Pi 5.
2. Kết nối SSH vào Pi 5 từ máy tính cá nhân:
   ```bash
   ssh pi@<IP_CUA_PI>
   ```
3. Copy mã nguồn (hoặc clone qua Git) thư mục `mesh_pi5_server` vào thư mục Home (`/home/pi/mesh_pi5_server`).
4. Truy cập thư mục và chạy script cài đặt tự động:
   ```bash
   cd ~/mesh_pi5_server
   sudo chmod +x deploy/install.sh deploy/setup-wifi-ap.sh
   sudo ./deploy/install.sh
   ```
5. **Tiến trình cài đặt tự động (mất khoảng 10-15 phút) sẽ thực hiện:**
   - Cài đặt Python 3, Node.js, Nginx, SQLite3.
   - Biên dịch ứng dụng React Frontend tĩnh.
   - Thiết lập môi trường Python ảo (venv) và cài đặt dependencies (FastAPI, uvicorn, pyserial...).
   - Tạo điểm phát sóng WiFi (Access Point) có tên `MESH_TACTICAL_WIFI`.
   - Thiết lập Systemd Service để Server tự động chạy ngầm mỗi khi Pi khởi động lại.

## 4. Kiểm tra Kết quả Cài đặt
Sau khi script cài đặt hoàn tất, Pi 5 của bạn sẽ bị ngắt mạng Internet (vì nó đã tự chuyển thành một trạm phát WiFi độc lập).

1. Dùng điện thoại hoặc Tablet tìm và kết nối vào mạng WiFi:
   - **SSID:** `MESH_TACTICAL_WIFI`
   - **Mật khẩu:** (Xem trong file `deploy/setup-wifi-ap.sh`, mặc định là `meshpassword`)
2. Mở trình duyệt web trên điện thoại/Tablet và truy cập:
   - **http://10.42.0.1**
3. Bạn sẽ nhìn thấy giao diện **Trung tâm Chỉ huy Mesh** đã được tải lên thành công (hoàn toàn Offline).
