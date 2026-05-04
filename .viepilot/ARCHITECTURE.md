# ARCHITECTURE — Mesh Pi5 Server

## Tổng quan hệ thống

Hệ thống Web-based Command & Control chạy trên Raspberry Pi 5, kết nối trực tiếp với LoRa Gateway qua USB Serial, phát WiFi AP để các thiết bị đầu cuối (tablet, phone, laptop) truy cập dashboard chiến thuật qua trình duyệt web. Hoạt động **100% offline** tại thực địa.

### Sơ đồ tổng thể

```mermaid
graph TB
    subgraph Field["Chiến trường"]
        S1["Soldier Node 1<br/>T-Beam + Sensors"]
        S2["Soldier Node 2<br/>T-Beam + Sensors"]
        SN["Soldier Node N<br/>T-Beam + Sensors"]
    end

    subgraph Gateway["Gateway Node"]
        GW["T-Beam Gateway<br/>LoRa 433MHz<br/>GPS + OLED"]
    end

    subgraph Pi5["Raspberry Pi 5 (4GB)"]
        SERIAL["Serial Bridge<br/>pyserial Thread"]
        FASTAPI["FastAPI Backend<br/>REST + WebSocket"]
        DB[("SQLite DB<br/>WAL mode")]
        NGINX["Nginx<br/>Static + Proxy"]
        WIFI["WiFi AP<br/>NetworkManager"]
        STATIC["React Build<br/>(dist/)"]
    end

    subgraph Clients["Client Devices"]
        TAB["Tablet<br/>Browser"]
        PHONE["Phone<br/>Browser"]
        LAPTOP["Laptop<br/>Browser"]
    end

    S1 -->|"LoRa 433MHz"| GW
    S2 -->|"LoRa 433MHz"| GW
    SN -->|"LoRa 433MHz"| GW
    GW -->|"USB Serial<br/>115200 baud"| SERIAL
    SERIAL --> FASTAPI
    FASTAPI --> DB
    FASTAPI -->|"WebSocket /ws"| NGINX
    FASTAPI -->|"REST /api/*"| NGINX
    STATIC --> NGINX
    NGINX -->|"WiFi AP"| WIFI

    WIFI -.->|"http://10.42.0.1"| TAB
    WIFI -.->|"http://10.42.0.1"| PHONE
    WIFI -.->|"http://10.42.0.1"| LAPTOP
```

## Kiến trúc Backend

### FastAPI Application Structure

```mermaid
graph LR
    subgraph SerialLayer["Serial Layer"]
        THREAD["Serial Reader<br/>Thread (blocking)"]
        QUEUE["asyncio.Queue<br/>(bridge)"]
        PARSER["PacketParser<br/>40-byte protocol"]
    end

    subgraph APILayer["API Layer"]
        REST["REST Endpoints<br/>/api/nodes<br/>/api/events<br/>/api/commands"]
        WS["WebSocket<br/>/ws"]
    end

    subgraph DataLayer["Data Layer"]
        REPO["Repository<br/>SoldierRepository"]
        MODELS["SQLAlchemy Models<br/>SoldierEntity<br/>EventEntity"]
        SQLITE[("SQLite<br/>WAL mode")]
    end

    subgraph FutureAI["AI Layer (Phase 5-6)"]
        PATHFIND["A* Pathfinding"]
        VISION["OpenCV/YOLO"]
    end

    THREAD -->|"raw bytes"| PARSER
    PARSER -->|"SoldierPacket"| QUEUE
    QUEUE --> WS
    QUEUE --> REPO
    REPO --> MODELS
    MODELS --> SQLITE

    REST --> REPO
    PATHFIND --> REST
    VISION --> REST
```

### Data Flow (Nhận dữ liệu từ Gateway)

```mermaid
sequenceDiagram
    participant GW as Gateway Node
    participant SER as Serial Thread
    participant PARSER as PacketParser
    participant Q as asyncio.Queue
    participant REPO as Repository
    participant DB as SQLite
    participant WS as WebSocket
    participant UI as Browser UI

    GW->>SER: Serial bytes (0xAA + 40 bytes + 0x55)
    SER->>PARSER: Raw byte buffer
    PARSER->>PARSER: Validate CRC16-CCITT
    PARSER->>Q: SoldierPacket (parsed)
    Q->>REPO: Insert/Update
    REPO->>DB: SQLAlchemy write
    Q->>WS: Broadcast to clients
    WS->>UI: JSON message
    UI->>UI: Update map markers + panel
```

### Data Flow (Gửi lệnh xuống Gateway)

```mermaid
sequenceDiagram
    participant UI as Browser UI
    participant API as REST API
    participant REPO as Repository
    participant PARSER as PacketParser
    participant SER as Serial Writer
    participant GW as Gateway Node

    UI->>API: POST /api/commands
    API->>REPO: Validate + store command
    REPO->>PARSER: Build packet + CRC16
    PARSER->>SER: Frame: 0xAA + packet + 0x55
    SER->>GW: Serial write
    GW->>GW: LoRa broadcast
    GW-->>SER: "TX: Message sent"
    SER-->>API: ACK response
    API-->>UI: Command status update
```

## Giao thức truyền thông

### Binary Packet Protocol (kế thừa từ firmware hiện tại)

```
┌──────┬─────────────────────────────────────┬──────┐
│ 0xAA │     SoldierPacket (40 bytes)        │ 0x55 │
│ START│                                     │ END  │
└──────┴─────────────────────────────────────┴──────┘

SoldierPacket Layout (packed, little-endian):
Offset  Size  Type     Field
──────  ────  ──────   ─────
0       2     uint16   nodeId
2       4     uint32   timestamp
6       4     float32  latitude
10      4     float32  longitude
14      4     float32  heading
18      1     uint8    heartRate
19      1     uint8    spo2
20      4     float32  temperature
24      4     float32  humidity
28      4     float32  pressure
32      4     float32  batteryVoltage
36      2     uint16   statusFlags
38      2     uint16   crc16
──────────────────────────────
Total: 40 bytes
```

### CRC16-CCITT
```
Polynomial: 0x1021
Initial value: 0xFFFF
Input: First 38 bytes (excluding crc16 field)
```

## Kết nối phần cứng

### USB Serial (Pi 5 ↔ Gateway)

| Thông số | Giá trị |
|----------|---------|
| **Port** | `/dev/ttyUSB0` (CP2102) hoặc `/dev/ttyACM0` |
| **Baudrate** | 115200 |
| **Data bits** | 8 |
| **Parity** | None |
| **Stop bits** | 1 |
| **USB chip** | CP2102 / CH340 (ESP32 T-Beam) |

### WiFi Access Point

| Thông số | Giá trị |
|----------|---------|
| **Method** | NetworkManager (nmcli) |
| **Interface** | wlan0 (built-in) |
| **SSID** | MESH_COMMAND_WIFI |
| **Security** | WPA2 |
| **IP** | 10.42.0.1 |
| **DHCP** | Auto (NetworkManager) |

### Nginx Reverse Proxy

| Route | Target |
|-------|--------|
| `/` | `frontend/dist/` (static) |
| `/api/*` | `http://127.0.0.1:8000` (FastAPI) |
| `/ws` | `ws://127.0.0.1:8000/ws` (WebSocket) |
| `/maps/*.pmtiles` | `maps/` directory (static) |

## Technology Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Platform | Raspberry Pi 5 (4GB) | Đủ mạnh cho server + AI, tiết kiệm năng lượng, nhỏ gọn triển khai thực địa |
| Backend | Python FastAPI | Async native, WebSocket tích hợp, hệ sinh thái AI/ML (OpenCV, PyTorch) |
| Frontend | React + Vite + TypeScript | Build nhanh, component architecture, type-safe |
| Map | MapLibre GL JS + PMTiles | Vector tiles offline, đọc trực tiếp trong browser, không cần tile server |
| Database | SQLite (WAL) | Nhẹ, không cần DB server, phù hợp nhúng |
| Serial | pyserial + Thread | Đọc blocking I/O trong thread riêng, bridge qua asyncio.Queue |
| Proxy | Nginx | Serve static + proxy API/WS, production-grade, ~5MB RAM |
| WiFi AP | NetworkManager | Chuẩn Pi OS Bookworm, tự động DHCP/NAT |
| AI Pathfinding | A* (Phase 5) | Tìm đường offline trên road network OSM |
| AI Vision | OpenCV/YOLO (Phase 6) | Nhận diện mục tiêu từ camera Pi |

## Diagram Applicability Matrix

| Diagram Type | Status | Notes |
|-------------|--------|-------|
| System Overview | **required** | ✅ Above |
| Data Flow (RX) | **required** | ✅ Above (sequence) |
| Data Flow (TX) | **required** | ✅ Above (sequence) |
| Module Dependencies | **required** | ✅ Backend structure above |
| Deployment | **required** | Pi 5 single-device deployment |
| Event Flows | **optional** | Covered by data flow sequences |
| User Use Case | **optional** | Single-user (commander) + multi-viewer |
| ERD | **required** | See schemas/database-schema.sql |
