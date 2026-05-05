# Hướng Dẫn Sử Dụng Raspberry Pi 5 & Cài Đặt Mesh Server (Dành cho Người Mới)

Tài liệu này được viết theo cách "cầm tay chỉ việc", giúp bạn thiết lập hệ thống dù bạn chưa từng chạm vào Raspberry Pi (gọi tắt là Pi 5) bao giờ.

---

## PHẦN 1: Chuẩn bị Thẻ nhớ (Làm trên Máy tính Windows/Mac của bạn)

Raspberry Pi không có ổ cứng gắn trong như máy tính thông thường. Nó lưu hệ điều hành và dữ liệu trên một chiếc **Thẻ nhớ MicroSD**. Việc đầu tiên bạn cần làm là "cài win" (hệ điều hành) lên chiếc thẻ nhớ này.

**Các bước thực hiện:**
1. Cắm thẻ nhớ MicroSD vào máy tính của bạn (dùng đầu đọc thẻ nếu cần).
2. Tải và cài đặt phần mềm **Raspberry Pi Imager** từ trang chủ: [https://www.raspberrypi.com/software/](https://www.raspberrypi.com/software/)
3. Mở phần mềm lên, bạn sẽ thấy 3 nút chính:
   - **CHOOSE DEVICE (Chọn thiết bị):** Bấm vào và chọn **Raspberry Pi 5**.
   - **CHOOSE OS (Chọn HĐH):** Bấm vào, chọn **Raspberry Pi OS (64-bit)** (Mặc định).
   - **CHOOSE STORAGE (Chọn ổ đĩa):** Bấm vào và chọn đúng chiếc thẻ nhớ bạn vừa cắm vào (Cẩn thận chọn nhầm ổ cứng máy tính nhé!).
4. Bấm **NEXT**. Lúc này phần mềm sẽ hỏi bạn có muốn cài đặt tùy chỉnh (Edit Settings) không. Hãy chọn **EDIT SETTINGS**.
5. Trong bảng Settings hiện ra:
   - Thẻ **General**: Đặt Username là `pi` và Password là `123456` (hoặc mật khẩu bạn dễ nhớ). Đánh dấu vào ô "Configure wireless LAN" và nhập tên WiFi + Mật khẩu WiFi nhà bạn vào đây (để Lát nữa con Pi tự bắt WiFi nhà bạn).
   - Thẻ **Services**: Đánh dấu tích vào ô **Enable SSH** (chọn Use password authentication). Việc này giúp bạn điều khiển Pi từ máy tính mà không cần cắm màn hình.
6. Bấm **SAVE**, sau đó bấm **YES** để bắt đầu ghi. Chờ khoảng 5-10 phút cho đến khi phần mềm báo hoàn tất.
7. Rút thẻ nhớ ra khỏi máy tính.

---

## PHẦN 2: Bật nguồn và Cài đặt Mesh Server

**Bước 1: Khởi động Raspberry Pi 5**
1. Lật mặt dưới của con Pi 5 lên, bạn sẽ thấy một khe cắm thẻ nhớ (nhỏ xíu). Hãy nhét chiếc thẻ nhớ vừa ghi vào đó cho đến khi nghe tiếng "tách" hoặc thẻ nằm khít.
2. Cắm cáp nguồn USB-C vào Pi 5 và cắm vào ổ điện (nên dùng củ sạc chính hãng 27W để tránh sập nguồn).
3. Đèn đỏ/xanh trên Pi 5 sẽ sáng lên. Hãy chờ khoảng 2-3 phút để máy khởi động lần đầu tiên và tự động kết nối vào WiFi nhà bạn.

**Bước 2: Điều khiển Pi 5 từ máy tính của bạn**
Vì chúng ta không cắm màn hình cho Pi 5, chúng ta sẽ điều khiển nó qua mạng.
1. Trên máy tính Windows của bạn, bấm nút `Start`, gõ `cmd` và mở **Command Prompt**.
2. Gõ lệnh sau và ấn Enter: `ssh pi@raspberrypi.local`
3. Máy tính sẽ hỏi mật khẩu. Nhập mật khẩu bạn đã tạo ở Phần 1 (ví dụ `123456`) và ấn Enter (lưu ý lúc gõ mật khẩu màn hình sẽ không hiện chữ gì cả, cứ gõ rồi ấn Enter).
4. Nếu màn hình hiện ra dòng chữ xanh lá cây kiểu `pi@raspberrypi:~ $` nghĩa là bạn đã chui vào trong con Pi 5 thành công!

**Bước 3: Tải mã nguồn và Cài đặt Tự động**
Bạn đang ở trong con Pi 5. Giờ chúng ta sẽ tải phần mềm Mesh Server về và cài đặt:
1. Gõ lệnh tải mã nguồn (Copy dòng này dán vào và ấn Enter):
   ```bash
   git clone https://github.com/your-repo/mesh_pi5_server.git
   ```
   *(Lưu ý: Bạn thay đường dẫn thực tế của folder hoặc chép thư mục `mesh_pi5_server` của chúng ta vào con Pi)*
2. Chui vào thư mục vừa tải:
   ```bash
   cd mesh_pi5_server
   ```
3. Chạy lệnh cài đặt tự động (Cứ copy và dán):
   ```bash
   sudo chmod +x deploy/install.sh
   sudo ./deploy/install.sh
   ```
4. Bây giờ bạn có thể đi uống cafe. Pi 5 sẽ tự động tải các phần mềm cần thiết, cài đặt tự động toàn bộ. Quá trình này mất khoảng 10-15 phút.

---

## PHẦN 3: Sử Dụng Thực Tế (Ra chiến trường)

Sau khi cài xong, con Pi 5 đã được "biến hóa" thành một cái **Cục Phát WiFi (Router)** độc lập. Bạn có thể mang nó ra rừng, lên núi, không cần cắm mạng Internet nhà bạn nữa.

1. Rút cáp nguồn Pi 5 ra, mang ra vị trí cần làm trạm chỉ huy.
2. Cấp nguồn lại cho Pi 5 (Dùng sạc dự phòng loại xịn cắm cổng USB-C).
3. Lấy con chip **LoRa Gateway (T-Beam)** cắm vào một trong các cổng USB của con Pi 5.
4. Lấy Điện thoại hoặc Máy tính bảng (Tablet) của bạn, mở danh sách WiFi lên.
5. Bạn sẽ thấy một mạng WiFi mới tên là: **Mesh_Tactical_AP**.
6. Bấm kết nối, mật khẩu mặc định là: **meshadmin123**.
7. Mở trình duyệt web (Safari/Chrome) trên Điện thoại/Tablet, gõ vào thanh địa chỉ:
   👉 **http://10.42.0.1**
8. Tuyệt vời! Bạn sẽ nhìn thấy "Trung tâm Chỉ huy Mesh" hiện ra. Mọi thiết bị lính đi trong rừng sẽ hiện lên bản đồ này.

**Chúc bạn thành công! Nếu có bước nào bị kẹt, hãy liên hệ ngay với kỹ thuật viên để được hỗ trợ.**
