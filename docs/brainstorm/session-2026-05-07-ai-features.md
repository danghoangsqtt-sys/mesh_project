# Brainstorm Session: Advanced AI Features for Mesh Pi5 Server

**Date:** 2026-05-07
**Status:** 🧠 Ideation

## Khởi động: Đề xuất các tính năng AI cho Raspberry Pi 5
Với sức mạnh của Raspberry Pi 5 (có thể chạy các mô hình AI nhỏ lẻ hoặc xử lý ảnh tốt hơn Pi 4), chúng ta có thể tích hợp AI vào hệ thống Mesh theo 3 hướng chính:

### 1. Trí tuệ Nhân tạo Thị giác (AI Vision / YOLO)
- **Tích hợp Camera:** Gắn module Camera (USB hoặc Pi Camera) vào gateway Pi 5.
- **Nhận diện mục tiêu:** Sử dụng YOLO (TFLite/ONNX) để nhận diện người, phương tiện, hoặc mục tiêu quân sự xung quanh trạm chỉ huy.
- **Cảnh báo bản đồ:** Khi phát hiện mục tiêu, AI tự động đánh dấu tọa độ (ước lượng) lên Tactical Map và gửi cảnh báo WebSocket.

### 2. Trợ lý Chỉ huy (Local LLM / NLP)
- **Chạy LLM nhẹ (Ollama / Llama.cpp):** Cài đặt một mô hình nhỏ (vd: Llama-3-8B-Q4 hoặc Phi-3) chạy trực tiếp (offline) trên Pi 5.
- **Phân tích nhật ký (Event Analysis):** AI tự động đọc log (Nhịp tim tăng cao, rớt mạng, SOS) và đưa ra tóm tắt chiến thuật hoặc dự đoán rủi ro.
- **Ra lệnh bằng giọng nói/văn bản tự nhiên:** Người chỉ huy gõ "Yêu cầu tất cả các đội quay về cứ điểm an toàn", AI tự động dịch thành các mã lệnh Hex để gửi qua LoRa.

### 3. AI Dẫn đường (Smart Pathfinding & Geofencing)
- Nâng cấp thuật toán A* hiện tại thành thuật toán dò đường thông minh có tính đến địa hình (độ cao từ `terrain.pmtiles`) để tránh vách núi.
- Phân tích rủi ro khu vực: Hệ thống AI tự khoanh vùng "Vùng nguy hiểm" (Danger Zone) nếu khu vực đó thường xuyên mất tín hiệu LoRa hoặc có cảnh báo nhịp tim bất thường.

## Quyết định & Kế hoạch (Draft)

## Phases

### Phase 10: AI Vision & Target Detection
- Cài đặt YOLOv8 (TFLite) trên backend.
- Tạo endpoint stream camera và API cảnh báo mục tiêu.
- Render mục tiêu phát hiện được lên bản đồ.

### Phase 11: Tactical AI Assistant (Local LLM)
- Cài đặt Ollama / Phi-3 trên Raspberry Pi 5.
- Tích hợp giao diện Chat (Trợ lý AI) vào Tactical Panel.
- Cho phép AI gửi lệnh xuống các NodeMesh thông qua backend API.

## Project meta intake (FEAT-009)
- **status**: skipped
- **reason**: Project đã có binding META từ trước.
