# Tính năng: Tactical Geofencing (Hàng rào Chiến thuật)

**Geofencing** là tính năng cao cấp cho phép người chỉ huy quy định các ranh giới không gian an toàn (Safe Zone) hoặc vùng cấm (Restricted Zone). Khi một Node di chuyển qua lại các ranh giới này, hệ thống sẽ tự động bắt cảnh báo.

## 1. Vẽ Vùng Chiến Thuật
1. Ở màn hình bản đồ chính, hãy nhấn vào nút **Draw Geofence** (Cạnh dưới màn hình).
2. Khi bật chế độ này, mọi cú click (hoặc chạm ngón tay) trên bản đồ sẽ sinh ra một điểm tọa độ (Waypoint) mới, và nối lại với nhau bằng đường chỉ vàng.
3. Chạm ít nhất 3 điểm để tạo thành một Đa giác (Polygon).
4. Bạn sẽ thấy 2 nút tùy chọn mới hiện ra:
   - **Save Safe:** Lưu vùng này thành Vùng An Toàn (Màu xanh lá mờ).
   - **Save Danger:** Lưu vùng này thành Vùng Cấm (Màu đỏ mờ).

## 2. Quy tắc Báo động (Ray-Casting Algorithm)
Hệ thống ngầm chạy thuật toán kiểm tra toạ độ với các quy tắc sau:
- **Nguyên tắc Danger:** Nếu bất kỳ một Soldier Node nào đi LỌT VÀO bên trong Vùng Cấm (Restricted), nó sẽ bị chuyển ngay sang trạng thái BÁO ĐỘNG ĐỎ (`alertLevel = 2`).
- **Nguyên tắc Safe:** Nếu có tồn tại ít nhất 1 Vùng An Toàn, và Soldier Node đi RA KHỎI tất cả các Vùng An Toàn đó, nó cũng sẽ bị coi là đi lạc và kích hoạt BÁO ĐỘNG ĐỎ.

## 3. Hệ thống Cảnh báo Đồng bộ (Gateway Sync)
1. Trên màn hình của Gateway (App bạn đang xem), màn hình sẽ hiện popup và lưu Event: `[GEOFENCE_ALERT] Node XX Entered DANGER zone`.
2. Đồng thời, App sẽ truyền ngược tín hiệu `[BROADCAST] CMD:SOS_TRIGGER` xuống cáp USB.
3. Mạch LilyGo Gateway lập tức gửi cảnh báo SOS này ra toàn bộ Mạng Lưới LoRa. Các Node con khi nhận được sẽ kêu còi bíp, đèn LED chớp đỏ để cảnh báo quân nhân ngay ngoài thực địa.
