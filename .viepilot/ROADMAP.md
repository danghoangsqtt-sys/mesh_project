# ROADMAP — Mesh Pi5 Server (v1.0.0)

> Lộ trình phát triển hệ thống Web-based Command & Control trên Raspberry Pi 5.

---

## Phase 1 — Pi 5 Foundation & Serial Bridge

**Mục tiêu:** Xây dựng Backend FastAPI cốt lõi có thể đọc Serial từ LoRa Gateway, lưu dữ liệu, và phát WebSocket.

### Task 1.1 — Project Setup & Backend Structure
- Khởi tạo project Python (`pyproject.toml` hoặc `requirements.txt`).
- Cấu trúc thư mục: `app/`, `app/routers/`, `app/models/`, `app/services/`, `app/ws/`.
- FastAPI app entry (`main.py`) với Uvicorn.
- Config management (`config.py` với Pydantic Settings).
- **Verification:** `uvicorn app.main:app --reload` starts without error.

### Task 1.2 — Binary Packet Parser
- Port PacketParser từ Kotlin sang Python.
- Parse 40-byte SoldierPacket: nodeId, GPS, vitals, statusFlags.
- CRC16-CCITT validation.
- Unit tests cho parser.
- **Verification:** `pytest tests/test_parser.py` passes.

### Task 1.3 — Serial Bridge (pyserial + Thread)
- Serial reader thread đọc `/dev/ttyUSB0` (115200 baud).
- Frame detection: `0xAA` ... `0x55`.
- Bridge qua `asyncio.Queue` sang async context.
- Auto-reconnect khi mất kết nối serial.
- **Verification:** Connect serial device, data flows to queue.

### Task 1.4 — SQLite Database & Models
- SQLAlchemy models: `SoldierEntity`, `EventEntity`, `CommandEntity`.
- Alembic migration setup.
- Repository pattern: `SoldierRepository` với CRUD operations.
- WAL mode enabled.
- **Verification:** `alembic upgrade head` + insert/query test.

### Task 1.5 — REST API Endpoints
- `GET /api/nodes` — Danh sách tất cả nodes.
- `GET /api/nodes/{id}` — Chi tiết node + vitals.
- `GET /api/nodes/{id}/history` — Lịch sử vị trí node.
- `GET /api/events` — Event log (SOS, alerts).
- `GET /api/status` — System status (serial connected, node count, uptime).
- **Verification:** `curl http://localhost:8000/api/nodes` returns JSON.

### Task 1.6 — WebSocket Real-time Stream
- Endpoint `/ws` broadcast SoldierPacket JSON cho tất cả clients.
- Connection manager hỗ trợ multi-client.
- Heartbeat/ping-pong để detect disconnected clients.
- **Verification:** WebSocket client connects and receives live data.

---

## Phase 2 — React Web Dashboard

**Mục tiêu:** Xây dựng giao diện Web chiến thuật chuyên nghiệp với MapLibre GL JS offline.

### Task 2.1 — Frontend Project Setup
- Khởi tạo React + Vite + TypeScript.
- Cấu trúc: `src/components/`, `src/hooks/`, `src/pages/`, `src/services/`.
- Military dark theme (CSS variables).
- ESLint + Prettier config.
- **Verification:** `npm run dev` starts, `npm run build` succeeds.

### Task 2.2 — MapLibre GL JS + PMTiles Offline Map
- Tích hợp MapLibre GL JS.
- Load bản đồ từ PMTiles file (offline, không cần Internet).
- Military color scheme cho map style.
- Custom markers cho soldier nodes (icon theo status).
- **Verification:** Map renders offline with PMTiles data.

### Task 2.3 — WebSocket Client & State Management
- Custom hook `useWebSocket` kết nối `/ws`.
- State store (Zustand hoặc React Context) cho node data.
- Auto-reconnect logic.
- **Verification:** UI receives and displays live node data.

### Task 2.4 — Tactical Panel UI
- Node list table: ID, name, status, battery, last seen.
- Node detail view: vitals (heart rate, SpO2, temp, humidity, pressure).
- Status color coding (OK=green, WARNING=yellow, CRITICAL=red).
- Responsive layout (tablet + phone + desktop).
- **Verification:** Panel shows real-time node data from WebSocket.

### Task 2.5 — Event Log & Alerts
- Real-time event feed (SOS, geofence breach, low battery).
- Audio alert cho CRITICAL events.
- Filter by severity, node, time range.
- **Verification:** Events display and alert sounds trigger correctly.

---

## Phase 3 — Two-way Command & Node Management

**Mục tiêu:** Gửi lệnh xuống Gateway, cấu hình node từ xa.

### Task 3.1 — Command API & Serial Write
- `POST /api/commands` — Gửi lệnh xuống gateway.
- Serial write logic (build packet + CRC + frame).
- Command queue với retry logic.
- **Verification:** Command sent, ACK received from gateway.

### Task 3.2 — Remote Node Configuration UI
- Settings panel per node: rename, Tx rate, sensor toggle.
- REST API: `PUT /api/nodes/{id}/config`.
- Confirmation dialog + status feedback.
- **Verification:** Config change applied and confirmed by node ACK.

### Task 3.3 — OTA Firmware Upload
- Web UI: file picker cho `.bin` firmware file.
- Chunked upload: split file → send chunks via serial.
- Progress bar + status tracking.
- `POST /api/ota/upload` + WebSocket progress updates.
- **Verification:** Firmware uploaded and node reboots with new version.

---

## Phase 4 — WiFi AP Auto-Setup & Deployment

**Mục tiêu:** Đóng gói hoàn chỉnh để deploy Pi 5 tại thực địa.

### Task 4.1 — WiFi AP Setup Script
- Script `deploy/setup-wifi-ap.sh` dùng `nmcli`.
- Cấu hình: SSID, password, channel, security.
- Auto-start on boot.
- **Verification:** Pi 5 phát WiFi, client kết nối được.

### Task 4.2 — Systemd Service
- File `deploy/mesh-server.service` cho FastAPI (Uvicorn).
- Auto-start on boot, restart on failure.
- Log output to journald.
- **Verification:** `sudo systemctl status mesh-server` shows active.

### Task 4.3 — Nginx Reverse Proxy
- Config `deploy/nginx.conf`: serve static + proxy API/WS.
- Gzip compression cho JS/CSS.
- WebSocket upgrade headers.
- **Verification:** `http://10.42.0.1` loads dashboard from Nginx.

### Task 4.4 — One-liner Install Script
- `deploy/install.sh`: cài Python, Node (build only), Nginx, clone repo, build frontend, setup services.
- Idempotent (chạy lại không lỗi).
- **Verification:** Fresh Pi 5 → run script → system operational.

---

## Phase 5 — AI Pathfinding & Advanced Features

**Mục tiêu:** Tìm đường ngắn nhất offline trên bản đồ.

### Task 5.1 — Road Network Extraction
- Extract road graph từ OSM data (PBF/GeoJSON).
- Build adjacency graph (Python networkx hoặc custom).
- Serialize graph cho fast loading.
- **Verification:** Graph loaded with correct node/edge count.

### Task 5.2 — A* Pathfinding Algorithm
- Implement A* trên road graph.
- Heuristic: Haversine distance.
- Support weighted edges (road type, terrain).
- REST API: `POST /api/pathfinding` with start/end coords.
- **Verification:** Returns valid shortest path GeoJSON.

### Task 5.3 — Route Display on Map
- Frontend: render route as MapLibre Polyline layer.
- UI: click start point + end point → show route.
- Distance + estimated time display.
- **Verification:** Route displayed correctly on offline map.

### Task 5.4 — Tactical Geofencing
- Draw polygon zones on map (Safe Zone, Danger Zone).
- Store geofence polygons in SQLite.
- Real-time point-in-polygon checking.
- Alert when node enters/exits zone.
- **Verification:** Geofence alerts trigger correctly.

---

## Phase 6 — AI Vision Integration (Future)

**Mục tiêu:** Camera module + AI nhận diện mục tiêu.

### Task 6.1 — Camera Module Integration
- Pi Camera Module v3 hoặc USB camera.
- Capture frames via `picamera2` library.
- **Verification:** Camera captures frames on Pi 5.

### Task 6.2 — Object Detection (YOLO/OpenCV)
- YOLO model (TFLite hoặc ONNX) cho Pi 5.
- Detect + classify objects in camera feed.
- **Verification:** Detection runs at ≥5 FPS on Pi 5 4GB.

### Task 6.3 — Video Streaming & Map Integration
- Stream via WebSocket (MJPEG) hoặc WebRTC.
- Auto-mark detected targets on map.
- **Verification:** Live video + markers visible in dashboard.

---

## Phase 7 — Vitals Alerts & Internationalization (i18n)

**Mục tiêu:** Mở rộng hệ thống cảnh báo và hỗ trợ đa ngôn ngữ cho hệ thống.

### Task 7.1 — Internationalization (i18n)
- Cấu hình `react-i18next` ở Frontend.
- Dịch toàn bộ Dashboard sang 2 ngôn ngữ: Tiếng Việt và Tiếng Anh.
- Thêm nút switch ngôn ngữ trên UI.
- **Verification:** Chuyển đổi ngôn ngữ thành công mà không cần tải lại trang.

### Task 7.2 — Vitals & Connectivity Alerts
- Backend logic: theo dõi Nhịp tim (<60) và SpO2 (<90%).
- Cảnh báo mất kết nối: Timeout 30s không nhận được ping. Hiển thị "Last Known Location" trên bản đồ.
- Cảnh báo mất nguồn (Power Loss) qua flag hoặc % pin rớt đột ngột.
- **Verification:** Trigger mock data và kiểm tra alert hiện đúng trên Event Log.

### Task 7.3 — Smart Debug Logs & Instructions
- Nâng cấp UI của Event Log: hiển thị rõ lý do sự cố và **hướng dẫn xử lý**.
- Phân loại severity rõ ràng (INFO, WARNING, CRITICAL).
- **Verification:** Event log hiển thị thông báo thân thiện với người dùng.

---

## Phase 8: PMTiles Downloader & Tactical UI Redesign (✅ Completed)
**Goal:** Tích hợp bộ tải bản đồ Offline trực tiếp trên Pi và nâng cấp UI Web theo phong cách Android App.
**Tasks:**
- [x] Backend API tải PMTiles (`pmtiles extract` / `httpx`).
- [x] Giao diện quản lý bản đồ (Bounding Box & WebSocket Progress).
- [x] Thiết kế UI Tactical (Glassmorphism, Dark Military, Glow Effects).

### Task 8.1 — PMTiles Downloader Backend API
- Backend: Cấu hình API endpoint `/api/maps/download` trên FastAPI.
- Tích hợp công cụ tải file `.pmtiles` ngầm (background task).
- Tracking % tiến độ tải (phát qua WebSocket).
- **Verification:** Gọi API và kiểm tra file `offline.pmtiles` được tải thành công vào thư mục `maps/`.

### Task 8.2 — Map Manager Frontend UI
- Thêm màn hình (Modal/Panel) "Map Manager" vào React Frontend.
- UI: Cho phép nhập tọa độ Bounding Box hoặc chọn khu vực mẫu (VD: Nha Trang, Hà Nội...).
- Hiển thị thanh tiến trình (Progress Bar) lắng nghe WebSocket.
- **Verification:** Nhấn tải và thấy thanh chạy, khi xong Map tự động làm mới.

### Task 8.3 — Tactical UI Redesign (Sync with Android App)
- Chỉnh sửa bố cục Frontend (Panel, Header, Node List) cho chuyên nghiệp, khớp thiết kế Android.
- Tinh chỉnh Color Theme (Dark Military, Neo-brutalism hoặc Glassmorphism tùy theo concept).
- **Verification:** UI trông hiện đại, mượt mà và trực quan hơn.

---

## Phase 9 — Advanced 3D Terrain & Multi-color Vector Map

**Mục tiêu:** Nâng cấp bản đồ offline chiến thuật lên mức cao nhất: hiển thị màu sắc địa hình phong phú, nhà cửa 3D và địa hình đồi núi thực tế (Terrain 3D) dựa trên công nghệ MapLibre.

### Task 9.1 — Multi-color Vector Style (OSM Liberty Concept)
- Cấu hình lại `style.json` của MapLibre (bỏ style LIGHT đơn sắc mặc định).
- Áp dụng bảng màu phong phú: xanh lá cho thảm thực vật/công viên, xanh dương cho nước, phân loại màu cho từng cấp độ đường xá (như `AliFlux/VectorTileRenderer`).
- **Verification:** Bản đồ vector hiển thị nhiều màu sắc sống động, dễ nhận diện các khu vực địa hình khác nhau.

### Task 9.2 — 3D Buildings (Fill-extrusion)
- Kích hoạt layer `fill-extrusion` trong MapLibre cho các vùng `building` từ `offline.pmtiles`.
- Cấu hình độ cao tự động theo thuộc tính (hoặc default) và thêm đổ bóng sáng tối.
- **Verification:** Khi xoay/nghiêng bản đồ (Right-click + Drag), nhà cửa hiện lên thành các khối 3D.

### Task 9.3 — 3D Terrain (Digital Elevation Model)
- Tải dữ liệu độ cao dạng `RGB DEM` (.pmtiles) cho khu vực hoạt động (VD: Nha Trang).
- Lưu trữ vào `/opt/mesh_pi5_server/maps/terrain.pmtiles`.
- Khai báo source `terrain` và kích hoạt `map.setTerrain()` với hệ số phóng đại (exaggeration) ~1.2 đến 1.5.
- **Verification:** Bản đồ vệ tinh và vector uống cong theo dạng đồi núi thực tế. Dễ dàng quan sát các cao điểm chiến thuật.
