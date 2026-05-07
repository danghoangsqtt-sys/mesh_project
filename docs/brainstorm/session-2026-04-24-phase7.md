# Brainstorm: Phase 7 - Hardware Control & Log Integration

## Meta
- **Date**: 2026-04-24
- **Topic**: Tích hợp các tính năng quản lý phần cứng (COM port) và theo dõi Log từ dự án cũ.
- **Status**: active

## Background
Dự án cũ `Mesh_Manager` có một giao diện rất mạnh về cấu hình phần cứng (chọn cổng COM, đọc Serial, lưu Log sự kiện). Giao diện hiện tại `Tactical Command Center` (từ Phase 6) lại rất mạnh về theo dõi bản đồ và thống kê. Mục tiêu của Phase 7 là hợp nhất hai sức mạnh này mà không làm hỏng tính "Chiến thuật" của giao diện mới.

## Proposed Features (Phase 7)
1. **Thanh Toolbar Kết nối (Top Bar)**
   - Mục đích: Cho phép người dùng chọn cổng COM thực tế thay vì hardcode bằng lệnh `--port`.
   - Vị trí: Ở sát trên cùng màn hình, bên trên chữ "TACTICAL TRACKING SYSTEM".
   - Thành phần: Dropdown chọn cổng COM, Nút Connect/Disconnect, Trạng thái (Xanh/Đỏ).

2. **Cửa sổ Nhật ký Sự kiện (Event Log) & Serial Monitor**
   - Mục đích: Lưu lại lịch sử gửi nhận tin nhắn và đọc Serial thô từ Gateway.
   - Các phương án Vị trí:
     - *Phương án A:* Thêm một thanh chia (QSplitter) theo chiều dọc ở góc dưới bên TRÁI. Tức là Bảng Node Table sẽ bị thu ngắn lại một chút để nhường chỗ cho khung Log ở dưới cùng bên trái.
     - *Phương án B:* Làm dạng Cửa sổ nổi (Dock Widget) có thể kéo ra kéo vào.
     - *Phương án C:* Làm hệ thống Tab ở bên Trái. Tab 1: Control & Table. Tab 2: Event Logs & Serial.

3. **Nút Tính Năng Mở Rộng**
   - Nút "Tải Bản đồ Offline": Cần thiết nếu triển khai ở vùng không có mạng internet (mở thư mục chứa tile hoặc hộp thoại tải).

## Phases Assignment
- **Phase 7**: Hardware Control & Logging Integration

## Open Questions for User
1. Về tính năng Event Log / Serial Monitor, bạn thích Phương án A (chia đôi bên trái), Phương án B (Cửa sổ nổi), hay Phương án C (Tab)?
2. Bạn có muốn mang tính năng "Tải bản đồ Offline" vào bản đồ chiến thuật không (sẽ cho phép load thư mục map giống dự án cũ)?
