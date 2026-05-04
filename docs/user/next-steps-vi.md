# Bước Tiếp Theo: Cài Đặt Từ Terminal (Dành Cho Người Mới)

Theo màn hình bạn cung cấp, bạn đã vào đến thư mục `mesh_pi5_server` thành công. Dưới đây là chính xác những gì bạn cần nhập tiếp theo vào màn hình đen đó.

## 1. Các Câu Lệnh Cần Nhập (Copy và Dán)

**Lệnh thứ nhất:**
Bạn cần cấp quyền "chạy" cho các file cài đặt. Hãy copy dòng dưới đây, dán vào Terminal và ấn Enter:
```bash
sudo chmod +x deploy/install.sh deploy/setup-wifi-ap.sh
```
*(Nếu nó không hiện gì mà nhảy xuống dòng mới `pi@k2pi:...` thì tức là đã thành công).*

**Lệnh thứ hai:**
Bây giờ chúng ta bắt đầu cài đặt. Copy dòng này, dán vào và ấn Enter:
```bash
sudo ./deploy/install.sh
```

## 2. Quá Trình Chờ Đợi

Ngay sau khi bạn ấn Enter lệnh thứ hai, màn hình sẽ chạy rất nhiều chữ. Đừng hoảng sợ!
- Pi 5 đang tự động tải và cài đặt Python, Node.js, cấu hình WiFi và khởi chạy Website.
- **Thời gian chờ:** Khoảng 10 đến 15 phút.
- Bạn tuyệt đối KHÔNG đóng cửa sổ Terminal lúc này.

## 3. Chuyện Gì Xảy Ra Tiếp Theo?

Khi quá trình cài đặt chạy xong tới 100%, hệ thống sẽ tự động biến chiếc Raspberry Pi của bạn thành một cục phát WiFi (Access Point).
Vì vậy, **mạng Internet của bạn tới con Pi sẽ bị ngắt**, và cửa sổ Terminal (SSH) của bạn có thể sẽ bị đơ (frozen) hoặc báo lỗi "Connection lost". **Điều này là hoàn toàn bình thường và là dấu hiệu của sự thành công!**

## 4. Hướng Dẫn Sử Dụng Sau Khi Cài Đặt

Khi Terminal của bạn bị văng ra, hãy làm theo các bước sau để sử dụng hệ thống:

1. **Lấy Điện thoại hoặc Laptop của bạn**, mở phần cài đặt WiFi lên.
2. Tìm một mạng WiFi mới có tên là: **`MESH_TACTICAL_WIFI`**
3. Bấm kết nối vào mạng đó. 
   - **Mật khẩu là:** `meshpassword`
4. Cắm thiết bị LoRa T-Beam vào cổng USB của con Pi 5.
5. Mở trình duyệt web (Google Chrome hoặc Safari) trên Điện thoại/Laptop vừa kết nối WiFi.
6. Gõ địa chỉ sau vào thanh tìm kiếm: 
   👉 **`http://10.42.0.1`**
7. Xong! Bạn sẽ thấy màn hình Điều khiển Chiến thuật hiện ra. Hệ thống đã hoàn toàn sẵn sàng. Mọi lần cắm điện sau này, bạn chỉ cần làm từ bước 1 đến bước 6 (không cần cài đặt lại nữa).
