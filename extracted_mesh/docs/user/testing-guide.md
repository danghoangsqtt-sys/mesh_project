# Kịch bản Kiểm thử Thiết bị (Testing Guide)

Tài liệu này hướng dẫn cách thực hiện kiểm thử thực địa với phần cứng T-Beam ESP32 và Raspberry Pi 5.

## 1. Chuẩn bị Kết nối Phần cứng
1. Bật nguồn (lắp pin) cho tất cả các thiết bị Node Lính (Soldier Nodes). Đảm bảo Antenna LoRa đã được vặn chặt.
2. Cắm **LoRa Gateway (T-Beam ESP32)** vào cổng USB của Raspberry Pi 5.
   - *Lưu ý:* Hệ thống được lập trình để tự động dò cổng `/dev/ttyUSB0` hoặc `/dev/ttyACM0`. Gateway sẽ nháy đèn hiệu khi được cấp nguồn từ cổng USB.

## 2. Kết nối vào Bảng điều khiển (Dashboard)
1. Dùng Tablet (Máy tính bảng) kết nối vào WiFi do Pi 5 phát ra (`MESH_TACTICAL_WIFI`).
2. Mở trình duyệt (Chrome/Safari), truy cập địa chỉ `http://10.42.0.1`.
3. Nhìn vào góc trên bên phải màn hình, đảm bảo hệ thống báo trạng thái **🟢 LIVE** (Màu xanh), tức là Backend đã kết nối thành công với Gateway qua cổng Serial.

## 3. Các Bài Test Tiêu Chuẩn (Test Cases)

### Test 1: Kiểm tra Nhận diện & GPS
- **Thực hiện:** Mang 1 Node Lính di chuyển ra khu vực trống trải để bắt sóng vệ tinh.
- **Kỳ vọng:** 
  - Node xuất hiện trên **Bảng điều khiển (Tactical Panel)**.
  - Trạng thái `GPS Fix: Yes`.
  - Icon lính xuất hiện và di chuyển tương ứng trên Bản đồ (Tactical Map).

### Test 2: Kiểm tra Vitals (Sinh tồn)
- **Thực hiện:** Truyền dữ liệu cảm biến mô phỏng hoặc gắn cảm biến nhịp tim/SpO2 thực vào Node lính. (Có thể test bằng cách chạm vào cảm biến MAX30102 nếu có).
- **Kỳ vọng:** Nhịp tim và % Oxy hiển thị theo thời gian thực (update mỗi 10-15s tùy cấu hình Tx Rate) trong Bảng điều khiển.

### Test 3: Kích hoạt Cảnh báo Tự động (Smart Alerts)
- **Thực hiện Test Nhịp tim/SpO2:** Mô phỏng dữ liệu nhịp tim tụt xuống dưới 60 bpm.
  - *Kỳ vọng:* Màn hình hiện màu đỏ (CRITICAL), bảng Event Log hiển thị thông báo "Nhịp tim thấp nguy hiểm" kèm hướng dẫn "Điều động quân y...".
- **Thực hiện Test Mất tín hiệu:** Tắt nguồn đột ngột một Node đang trực tuyến, sau đó đợi quá 30 giây.
  - *Kỳ vọng:* Event Log hiển thị cảnh báo "Mất kết nối", hệ thống ghim "Last Known Location" trên bảng điều khiển để khoanh vùng tìm kiếm.

### Test 4: Truyền lệnh Xuống (Downlink Command) & Cấu hình
- **Thực hiện:** Bấm vào nút bánh răng (Cấu hình) của một Node lính trên UI. Chỉnh thông số **Tx Rate** từ 15 giây xuống 5 giây, bấm "Lưu".
- **Kỳ vọng:** 
  - Hệ thống báo gửi lệnh thành công (phản hồi ACK).
  - Tần suất chớp đèn TX trên thiết bị lính nhanh lên thành 5 giây/lần.
  - Dữ liệu hiển thị trên bảng điều khiển cập nhật nhanh hơn (5s 1 lần).

### Test 5: Cập nhật Firmware Từ xa (OTA qua LoRa)
- **Thực hiện:** 
  1. Vào bảng Cấu hình của một Node đích.
  2. Tại mục "Cập nhật Firmware (OTA)", chọn file `.bin` đã được biên dịch mới.
  3. Bấm "Bắt đầu cập nhật OTA".
- **Kỳ vọng:** Thanh tiến trình phần trăm (%) hiện lên và chạy dần cho đến 100%. Node lính ở xa sẽ tự động khởi động lại và nhận Firmware mới mà không cần cắm cáp.
