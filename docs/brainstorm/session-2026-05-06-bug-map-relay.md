# Brainstorm Session — 2026-05-06 Bug: Bản đồ & Chuyển tin nhắn

**Date:** 2026-05-06  
**Status:** Completed (partial — tin nhắn còn mở)  
**Language:** vi  
**Topic:** Debug & Fix — Lỗi tính năng bản đồ offline + tính năng chuyển tin nhắn

---

## Context

User báo cáo 2 lỗi nghiêm trọng trong hệ thống Pi mesh quân sự (`meshpi`, aarch64):
- **Lỗi 1:** "Network error starting download" khi tải bản đồ offline
- **Lỗi 2:** Tính năng CHỈ HUY (gửi tin nhắn từ web UI) không hoạt động

**Hệ thống đang chạy:**
- Pi 5 hostname: `meshpi`, IP WiFi AP: `10.42.0.1`
- FastAPI backend: `uvicorn` trên port 8000 (systemd `mesh-server`)
- nginx: port 80, proxy `/api/` → `:8000`, serve static frontend từ `/opt/mesh_pi5_server/frontend/dist`
- pmtiles: `/usr/local/bin/pmtiles` đã cài ✅
- Gateway serial: `/dev/ttyACM0` chưa cắm (expected)

---

## Bug 1: "Network error starting download" ✅ ĐÃ SỬA

### Root Causes (3 tầng)

**Tầng 1 — AttributeError (critical):**
```
File "/opt/mesh_pi5_server/backend/app/routers/map.py", line 21
    if map_service.is_downloading:
AttributeError: 'MapService' object has no attribute 'is_downloading'
```
Pi's `map.py` (newer version) dùng `is_downloading` nhưng `map_service.py` chỉ có `downloading`.

**Tầng 2 — Wrong broadcast method:**
```
AttributeError: 'ConnectionManager' object has no attribute 'broadcast'
```
Pi's `ConnectionManager` dùng `broadcast_event(event_type, data)` không phải `broadcast(dict)`.

**Tầng 3 — Maps bị xóa khi build (persistent issue):**
Maps lưu tại `frontend/dist/maps/` — bị Vite xóa mỗi lần `npm run build`.

### Fixes Applied

| Fix | File | Status |
|-----|------|--------|
| Thêm `@property is_downloading` | `map_service.py` | ✅ Deploy lên Pi |
| Đổi `broadcast()` → `broadcast_event()` | `map_service.py` | ✅ Deploy lên Pi |
| Fix process loop (asyncio.wait_for) | `map_service.py` | ✅ Deploy lên Pi |
| Đổi storage → `/opt/mesh_pi5_server/maps/` | `map_service.py` | ✅ Code sửa, chưa tạo dir |
| Thêm `location /maps/` trong nginx | `deploy/nginx.conf` | ⚠️ Chưa deploy nginx |

### Kết quả sau fix

```
POST /api/maps/download → 200 OK ✅
GET  /api/maps/status   → {"is_downloading": false} ✅
Download chạy background → không còn 500 error ✅
```

### Việc còn lại (nginx + dir)

```bash
# Chạy từ máy Windows:
pscp -pw "mesh" -hostkey "ssh-ed25519 255 SHA256:/eg1xhDt+tfvCefIJPIM//zpdY4yByba+tfcroqve3s" \
  "e:/data/2.MyProject/2026/mesh_pi5_server/deploy/nginx.conf" \
  pi@10.42.0.1:/tmp/nginx-new.conf

plink -ssh -pw "mesh" -hostkey "ssh-ed25519 255 SHA256:/eg1xhDt+tfvCefIJPIM//zpdY4yByba+tfcroqve3s" \
  -batch pi@10.42.0.1 \
  "sudo cp /tmp/nginx-new.conf /etc/nginx/sites-available/mesh-server && \
   sudo ln -sf /etc/nginx/sites-available/mesh-server /etc/nginx/sites-enabled/mesh-server && \
   sudo nginx -t && sudo systemctl reload nginx && \
   sudo mkdir -p /opt/mesh_pi5_server/maps && sudo chown pi:pi /opt/mesh_pi5_server/maps"
```

---

## Bug 2: Tính năng CHỈ HUY (Gửi tin nhắn) — CHƯA DEBUG

### Hiện trạng

Từ screenshot UI, tab **CHỈ HUY** có:
- Khu vực hiển thị tin nhắn đã gửi
- Tin nhắn nhanh: DI CHUYỂN ĐẾN ĐIỂM TẬP, CĂN HỖ TRỢ Y TẾ, PHÁT HIỆN KẺ ĐỊCH, KHU VỰC AN TOÀN
- BÁO ĐỘNG (SOS)
- SOẠN TIN NHẮN với toggle Gửi: Toàn đội / Gửi: Cá nhân

### Backend đã sẵn sàng

`POST /api/commands` hỗ trợ:
```json
{ "target_node_id": null, "command_type": "BROADCAST", "payload_hex": "hex-encoded-text" }
```

### Vấn đề chưa rõ

- Frontend mới (trên Pi) khác với source code local — cần xem code mới của Pi
- Cần debug flow: UI → POST /api/commands → serial_bridge.write() → Gateway → LoRa → Node
- Gateway `/dev/ttyACM0` chưa cắm nên không test được end-to-end

---

## Decisions

- [x] **D1:** SSH connection via plink + pscp (password: mesh, hostkey cached)
- [x] **D2:** Map storage persistent tại `/opt/mesh_pi5_server/maps/` (ngoài dist/)
- [x] **D3:** `broadcast_event("MAP_DOWNLOAD_PROGRESS", data)` là API đúng của Pi
- [x] **D4:** Pi's source code ≠ local source code — cần sync

---

## Open Questions

- [ ] Tính năng CHỈ HUY: flow nào đang broken? (serial chưa cắm hay backend lỗi?)
- [ ] Có muốn sync toàn bộ source code từ Pi về local không?
- [ ] Maps cũ (trong `dist/maps/`) có cần migrate sang `/opt/mesh_pi5_server/maps/` không?

---

## Phases

### Phase 1 — Đã làm ✅
- [x] Diagnose root cause lỗi bản đồ (3 tầng AttributeError)
- [x] Fix và deploy `map_service.py` lên Pi
- [x] Fix progress bar (case mismatch WebSocket event)
- [x] Fix NoneType bug (deploy `map.py` router đúng)
- [x] Fix URL nguồn PMTiles (`v3.pmtiles` — URL cũ `build.pmtiles` bị 404)
- [x] Tải bản đồ Nha Trang 3.6 MB thành công → `/opt/mesh_pi5_server/maps/offline.pmtiles`
- [x] Verify API: `exists: true`, `size_mb: 3.6`, `downloading: true`

### Phase 2 — Fix hiển thị bản đồ ✅
- [x] Deploy nginx.conf mới lên Pi (thêm `/maps/` location)
- [x] Tạo `/opt/mesh_pi5_server/maps/` trên Pi
- [x] **Deploy `map.py` router lên Pi** (fix NoneType bug — Bug #3 ✅)
- [x] **Fix URL nguồn PMTiles** (`build.pmtiles` → `v3.pmtiles` — URL cũ 404 ✅)
- [x] **Tải lại bản đồ Nha Trang vùng rộng** bbox=(108.9,11.9,109.5,12.6) — **6.5 MB** ✅
- [x] **Fix map style** — DARK theme contrast cực thấp (highway #474747 trên earth #1f1f1f)
- [x] **Triển khai TACTICAL theme** — highway màu vàng (#f0c040), major road xanh trắng (#a0c0e0), minor road xanh xám (#5a7090)
- [x] **Dùng `layers()` không có `lang` option** — không cần online fonts, offline hoàn toàn
- [x] **Build và deploy frontend mới** `index-BXQ4DPCr.js` lên Pi
- [ ] **Xác nhận UI hiển thị đường xá rõ ràng** — người dùng cần hard refresh (Ctrl+Shift+R) tại http://10.42.0.1
- [ ] Debug tính năng CHỈ HUY (cắm gateway vào Pi, test gửi tin)

### Phase 3 — Cải thiện UX Bản Đồ (Yêu cầu mới)
- [ ] **Auto-zoom sau download**: sau khi tải xong, map tự zoom đến vùng bbox đã tải (UX quan trọng)
- [ ] **Hiển thị thông tin coverage**: trong modal hiện bbox đã tải + diện tích (km²)
- [ ] **Label địa danh offline**: download font glyphs về Pi để hiển thị tên đường/địa danh không cần internet
- [ ] **Tone bản đồ**: cho phép chọn giữa TACTICAL (tối + cao contrast) vs SATELLITE-STYLE vs TERRAIN
- [ ] **Upload .pmtiles từ PC** (API POST /api/maps/upload — phục vụ triển khai thực địa không internet)
- [ ] Sync source code giữa Pi và repo local
- [ ] **Tính năng Upload .pmtiles từ PC** (API upload + UI drag-drop)
- [ ] Thiết lập auto-backup bản đồ trước khi build
- [ ] Script download pmtiles trên Windows (pmtiles CLI + bbox)

### Phase 4 — Tối ưu
- [ ] Hỗ trợ multi-region (nhiều file .pmtiles theo khu vực)
- [ ] Metadata bản đồ (ngày tải, kích thước, bbox) hiển thị trên UI

---

---

## Session 2026-05-07 — Bug Fix #2: Progress bar tắt ngay sau 0.5s

**Ngày:** 2026-05-07  
**Trạng thái:** ✅ Fixed & Deployed to Pi

### Vấn đề được báo cáo

Nhấn "Tải Bản Đồ" → progress bar hiện **"Đang kết nối / Kiểm tra mạng..."** trong **~0.5s** rồi tắt — không có tiến trình tải xuất hiện tiếp theo.

### Root Cause — Case mismatch WebSocket event type

| Layer | Giá trị | Trạng thái |
|-------|---------|-----------|
| `map_service.py` backend | `"MAP_DOWNLOAD_PROGRESS"` (UPPERCASE) | ❌ Lỗi |
| `useWebSocket.ts:38` frontend | `'map_download_progress'` (lowercase) | ✅ Đúng |

JavaScript string comparison phân biệt hoa/thường → `MAP_DOWNLOAD_PROGRESS !== map_download_progress` → store không bao giờ được cập nhật → progress bar ẩn sau `isRequesting = false`.

**Lưu ý:** Session trước (D3) ghi nhận `broadcast_event("MAP_DOWNLOAD_PROGRESS", data)` là đúng nhưng KHÔNG kiểm tra frontend handler — đây là điểm bỏ sót.

### Fixes Applied

| Fix | File | Mô tả |
|-----|------|-------|
| 7 lần đổi case | `map_service.py` | `"MAP_DOWNLOAD_PROGRESS"` → `"map_download_progress"` |
| Refactor errorMsg | `MapManagerModal.tsx` | `useState` + `useEffect` → computed value từ render |
| Build frontend | `extracted_mesh/frontend/` | `npm run build` → `index-CcTCT-JD.js` |

### Deploy lên Pi 5

```
1. pscp map_service.py → pi@10.42.0.1:/opt/mesh_pi5_server/backend/app/services/
2. pscp index-CcTCT-JD.js → /tmp/ → sudo cp → /opt/.../frontend/dist/assets/
3. pscp index.html → /tmp/ → sudo cp → /opt/.../frontend/dist/
4. sudo systemctl restart mesh-server.service
```

### Trạng thái sau deploy

```
Pi 5 (10.42.0.1):
├── backend/app/services/map_service.py  → lowercase "map_download_progress" x7 ✅
├── frontend/dist/assets/index-CcTCT-JD.js  → build mới với MapManagerModal fix ✅
├── frontend/dist/index.html  → reference /assets/index-CcTCT-JD.js ✅
└── mesh-server.service  → active (running) ✅

pmtiles CLI: /usr/local/bin/pmtiles (đã cài từ session trước) ✅
```

### Kế hoạch tiếp theo

1. **Test thực tế** — `http://10.42.0.1` → Quản lý Bản đồ → Tải Bản Đồ → confirm progress 5%→90%→100%
2. **Deploy favicon/icons** — `favicon.svg`, `icons.svg` chưa được re-deploy (không ảnh hưởng chức năng)
3. **Thiết lập deploy script** — thay quy trình thủ công bằng `deploy/deploy.sh`
4. **Sync source code** — Pi không có source `MapManagerModal.tsx`, `hooks/useWebSocket.ts` mới → cần push full source lên Pi
5. **Bug 2 (Tính năng CHỈ HUY)** — vẫn chưa debug (gateway chưa cắm)

### Quyết định cập nhật

- **D3 (sửa):** `broadcast_event("map_download_progress", data)` — lowercase — là API đúng
- **D5 (mới):** Frontend build tại local (`extracted_mesh/frontend/`) rồi deploy `dist/` lên Pi qua pscp + sudo, không build trên Pi

---

---

## Session 2026-05-07 — Bug Fix #3: NoneType error khi nhấn "Tải Bản Đồ"

**Ngày:** 2026-05-07  
**Trạng thái:** ✅ Fixed & Deployed to Pi  

### Vấn đề được báo cáo

Nhấn "TẢI BẢN ĐỒ" → ngay lập tức hiện lỗi đỏ:
```
Lỗi hệ thống: expected str, bytes or os.PathLike object, not NoneType
```

### Root Cause — 2 tầng

**Tầng 1 — Deployment gap:**
Chỉ `map_service.py` được deploy từ session trước, **`map.py` router chưa được deploy**. Pi router cũ → `source_url: Optional[str] = None` → Pydantic gán `None` → `asyncio.create_subprocess_exec(*[..., None, ...])` raise `TypeError`.

**Tầng 2 — URL nguồn 404:**
`DEFAULT_SOURCE` cũ trỏ đến `build.pmtiles` đã bị xóa. URL đúng hiện tại là `v3.pmtiles`:
```
Cũ (404): https://data.source.coop/protomaps/openstreetmap/pmtiles/build.pmtiles  
Mới (✅): https://data.source.coop/protomaps/openstreetmap/tiles/v3.pmtiles
```

### Fixes Applied

| Fix | File | Trạng thái |
|-----|------|------------|
| Deploy `map.py` router (fix NoneType) | `routers/map.py` | ✅ Deployed |
| Cập nhật `DEFAULT_SOURCE` → `v3.pmtiles` | `routers/map.py` | ✅ Deployed |
| Deploy nginx.conf (`/maps/` location) | `nginx.conf` | ✅ Deployed |
| Tạo thư mục `/opt/mesh_pi5_server/maps/` | Pi filesystem | ✅ Done |
| Tải bản đồ Nha Trang (test extract) | `/maps/offline.pmtiles` | ✅ 3.6 MB |

### Kết quả xác thực

```json
GET /api/maps/status → {
  "exists": true,
  "path": "/opt/mesh_pi5_server/maps/offline.pmtiles",
  "size_mb": 3.6,
  "downloading": false
}
```

### Quyết định cập nhật

- **D5 (cập nhật):** URL nguồn PMTiles chuẩn: `https://data.source.coop/protomaps/openstreetmap/tiles/v3.pmtiles`
- **D6 (mới):** Chiến lược bản đồ: Download 1 lần khi có internet → lưu persistent vào `/opt/mesh_pi5_server/maps/offline.pmtiles` → dùng offline lâu dài
- **D7 (mới):** Phạm vi bản đồ Nha Trang: `bbox=109.1,12.1,109.4,12.5` → 3.6 MB (đủ dùng cho thực địa)

---

## Session 2026-05-07 — Triển khai Hybrid Satellite Map

**Ngày:** 2026-05-07
**Trạng thái:** ✅ Đã triển khai hoàn tất
**Công cụ:** MapTilesDownloader, pmtiles CLI

### Quá trình thực thi:
1. **Tool Download**: Viết script Python `tools/download_satellite.py` tự động tải ảnh vệ tinh từ **ESRI World Imagery** với format `.mbtiles`. Hỗ trợ resume, multi-thread, tự động sửa lỗi Unicode trên Windows.
2. **Download Tile**: Tải thành công zoom 0-16 cho khu vực Nha Trang, tổng cộng 2,442 tiles (~37.4 MB).
3. **Convert PMTiles**: Sử dụng `pmtiles` CLI chuyển đổi `satellite.mbtiles` → `satellite.pmtiles` (35.5 MB).
4. **Deploy Pi5**: 
    - Đẩy `satellite.pmtiles` sang thư mục `/opt/mesh_pi5_server/maps/` của Pi qua SSH (`pscp`).
    - Phân quyền `sudo chmod -R 777 /opt/mesh_pi5_server/frontend/dist/`.
5. **Frontend Update**:
    - Chỉnh sửa `TacticalMap.tsx`: Thêm raster source `satellite` và layer `satellite-layer`. Layer vệ tinh nằm dưới layer vector.
    - Thêm **nút Toggle SAT ON/SAT OFF** góc trên phải.
    - Build Vite app (`npm run build`) và deploy code Frontend tĩnh sang Pi.
    - Restart systemd services trên Pi (`nginx` và `mesh-server`).

**Kết quả:** Pi Mesh server hiện hỗ trợ xem offline bản đồ quân sự với vệ tinh (Google Maps-like) với lớp overlay nhãn đường.

---

## Session 2026-05-07 — Brainstorm: Advanced 3D Terrain & Vector Map (MapLibre + VectorTileRenderer concepts)

**Ngày:** 2026-05-07
**Trạng thái:** 🧠 Đang phân tích yêu cầu

### Yêu cầu của người dùng
Kết hợp tư tưởng của `AliFlux/VectorTileRenderer` với `maplibre`, loại bỏ giao diện bản đồ cũ, tạo bản đồ mới:
1. Đẹp, chính xác, đa sắc màu.
2. Bản đồ vector hoàn toàn offline.
3. Hiển thị RÕ ràng địa hình (Terrain 3D) và nhà cửa (3D Buildings), đường xá.

### Phân tích kiến trúc đề xuất (Draft)

1. **Về `VectorTileRenderer`:**
   - Đây là công cụ render cho C# (.NET). Chúng ta đang sử dụng React/MapLibre cho Pi5 Dashboard.
   - *Giá trị cốt lõi cần mượn:* Các style đẹp (OSM Liberty, Hybrid) và khả năng xử lý Vector Tile linh hoạt. Chúng ta có thể port các file JSON Style (màu sắc, line width) của họ sang định dạng `style.json` của MapLibre.

2. **Về `MapLibre GL JS` (Trọng tâm Frontend Web):**
   - **Vector Tiles (Đường xá, Tòa nhà):** Đã có sẵn qua `offline.pmtiles` (Protomaps), nhưng cần cấu hình lại `paint` layers (Màu sắc địa hình đa dạng thay vì chỉ trắng đen, kích hoạt `fill-extrusion` để đẩy nhà cửa lên thành khối 3D).
   - **Terrain 3D (Địa hình đồi núi):** MapLibre hỗ trợ tính năng `terrain`. Cần tải thêm 1 file `terrain.pmtiles` (định dạng Terrarium RGB DEM - Digital Elevation Model) lưu tại Pi. Khi có DEM, MapLibre sẽ tự uốn cong lớp nền vệ tinh và đường xá theo dạng đồi núi thực tế.

### Các công việc dự kiến (Phân bổ vào Phase 9 - ĐÃ HOÀN THÀNH):
- [x] Tìm/tải file dữ liệu **DEM (Digital Elevation Model)** khu vực Nha Trang (file RGB raster pmtiles) để làm dữ liệu độ cao: Viết script `tools/download_terrain.py`, tải 70 tiles DEM từ AWS Open Data. Convert sang `terrain.pmtiles`.
- [x] Chỉnh sửa `TacticalMap.tsx`: Kích hoạt `terrain-source` và `map.setTerrain({ 'source': 'terrain-source', 'exaggeration': 1.5 })`.
- [x] Kích hoạt `fill-extrusion` layer trong MapLibre cho các object có thuộc tính `building` (nhà cửa 3D), extrusion height sử dụng `coalesce` với property `height`.
- [x] Viết lại hàm tạo `style` (`getCustomLayers`): Áp dụng bảng màu phong phú (xanh lá cho công viên, xanh dương cho nước) giống như `OSM Liberty` hay `VectorTileRenderer`.
- [x] Cập nhật UI: Thêm nút **3D VIEW** để tự động rotate camera pitch lên 60 độ (nhìn xiên) hoặc trả về 0 độ (nhìn thẳng).

**Kết quả:**
- Bản đồ Vector có nhiều màu sắc đẹp hơn so với mặc định.
- Tòa nhà nổi lên thành khối 3D.
- Khi nghiêng bản đồ (click nút 3D VIEW), địa hình đồi núi thực tế khu vực Nha Trang sẽ nổi lên rõ ràng.
- Upload thành công `terrain.pmtiles` và deploy Frontend lên Raspberry Pi 5.

---

## Session 2026-05-07 (Part 2) — Brainstorm: UI Scroll & TBEAM I2C/LoRa Bug Fixes

**Ngày:** 2026-05-07
**Trạng thái:** 🧠 Đang phân tích

### Yêu cầu của người dùng
1. **Frontend:** Tính năng gửi tin nhắn ở sidebar bên phải không sử dụng được vì sidebar không có thanh cuộn dọc (scrollbar) làm nút gửi tin nhắn bị khuất.
2. **Firmware TBEAM:** Không nhận được dữ liệu cảm biến qua I2C và cả gateway lẫn node TBEAM đều không nhận được tin nhắn qua LoRa.

### Phân tích vấn đề & Kế hoạch
1. **Vấn đề Sidebar (TacticalPanel):**
   - Panel hiện tại đang cấu hình CSS `flex: 1` hoặc `overflow` chưa đúng cách, khiến nội dung bị tràn (overflow-y) mà không hiển thị thanh cuộn, đặc biệt là khi mở phần Compose Message.
   - Giải pháp: Chỉnh lại flexbox trong `TacticalPanel.tsx` để đảm bảo khu vực chat / compose message có thể cuộn được (`overflow-y: auto`).
2. **Vấn đề Firmware TBEAM:**
   - **I2C:** Kiểm tra file `config.h` và `sensor_manager.cpp` của `soldier_node_production` xem cấu hình chân SDA/SCL cho TBEAM có đúng chưa (TBEAM v1.1 hay v1.2 thường dùng SDA=21, SCL=22 cho PMU và các thiết bị ngoại vi). Đặc biệt khi sử dụng module AXP2101/AXP192 cần khởi tạo đúng I2C.
   - **LoRa Nhận Tin Nhắn:** Việc TBEAM gateway/nodemesh không nhận được tin nhắn có thể do cấu hình chân DIO0, DIO1, DIO2, RST, CS không khớp với thiết kế phần cứng TBEAM, hoặc vòng lặp `pollReceive` bị block.
   - Giải pháp: Kiểm tra mã nguồn, đối chiếu `config.h` với sơ đồ chân thực tế của board LILYGO T-Beam.

## Project meta intake (FEAT-009)

- **status**: skipped
- **reason**: Binding `.viepilot/META.md` đã tồn tại từ session trước
