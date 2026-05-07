# 📊 Báo Cáo Phân Tích Dự Án: Mesh Soldier Tracker

---

## 1. TỔNG QUAN DỰ ÁN

**Tên:** Mesh Soldier Tracker  
**Mục tiêu:** Hệ thống giám sát bộ binh thời gian thực qua mạng LoRa — không cần cellular/internet.

### Ba thành phần chính:

| Thành phần | Board | Vai trò |
|---|---|---|
| `soldier_node_production/` | TTGO T-Beam ESP32 | Node đeo người lính — đọc sensor, gửi LoRa |
| `gateway_node/` | TTGO T-Beam ESP32 | Cổng thu — nhận LoRa, chuyển tiếp USB Serial |
| `server/` | PC/Laptop (Python Flask) | Dashboard — phân tích dữ liệu, cảnh báo, REST API |

---

## 2. CHỨC NĂNG TỪNG THIẾT BỊ

### 2.1 Soldier Node (node đeo người lính)

**MCU:** ESP32 trên TTGO T-Beam  
**OS:** FreeRTOS với 5 tasks song song

| Task | Core | Chu kỳ | Chức năng |
|---|---|---|---|
| `sensorTask` | Core 0 | 5ms | Đọc MPU6050, MAX30102 (×8), BME280 (×200) |
| `gpsTask` | Core 0 | 100ms | Cập nhật GPS Bluetooth |
| `fusionTask` | Core 1 | 5ms | Fuse IMU + GPS → tọa độ + heading |
| `loraTask` | Core 1 | 1000ms | Đóng gói + truyền LoRa 40 bytes |
| `displayTask` | Core 1 | 500ms | Cập nhật OLED hiển thị trạng thái |

**Thuật toán nội bộ:**
- **Madgwick AHRS:** Tính roll/pitch/yaw từ 6-axis IMU
- **EKF (Extended Kalman Filter):** Kết hợp GPS + dead-reckoning khi mất GPS
- **Step detection:** Phát hiện bước chân bằng ngưỡng gia tốc (threshold 11.5 m/s², hysteresis 1.5)
- **Bluetooth GPS:** Kết nối module GPS ngoài qua BT Classic (SPP/NMEA)

---

### 2.2 Gateway Node

**MCU:** ESP32 trên TTGO T-Beam  
**Chạy vòng lặp đơn (Arduino loop)**

| Chức năng | Chi tiết |
|---|---|
| Thu LoRa (polling) | `LoRa.parsePacket()` mỗi vòng loop |
| Kiểm tra CRC | Validate CRC16-CCITT trước khi forward |
| Forward Serial | `[0xAA][40 bytes][0x55]` lên PC |
| GPS riêng | UART GPS → cung cấp vị trí gateway |
| Nhận lệnh ngược | Serial ← PC → LoRa TX (gửi lệnh xuống node) |
| OLED status | Hiển thị: số node, tổng gói, GPS gateway |
| Tracking node | Danh sách 100 node, timeout 30s |

---

### 2.3 Server (Python Flask)

| Module | Chức năng |
|---|---|
| `serial_comm.py` | Đọc Serial từ Gateway, parse binary frame |
| `packet_handler.py` | Giải mã struct 40 bytes, verify CRC16 |
| `soldier_manager.py` | Quản lý danh sách lính, lịch sử vị trí, KML export |
| `team_manager.py` | Phân nhóm lính theo đội |
| `alert_manager.py` | Đánh giá ngưỡng cảnh báo theo `alerts.conf` |
| `app.py` | REST API Flask + Web dashboard |

**REST API:**
- `GET /api/soldiers` — danh sách lính + alert
- `GET /api/teams` — lính theo đội
- `POST /api/send_message` — gửi lệnh xuống node (broadcast/team/single)
- `GET /api/soldier/<id>/history` — lịch sử vị trí
- `GET /api/soldier/<id>/kml` — export KML

---

## 3. LƯU ĐỒ THUẬT TOÁN

### 3.1 Soldier Node — FreeRTOS Flow

```
BOOT
 ├─ Wire.begin() → I2C bus
 ├─ AXP2101 PMU init → bật ALDO2/3/4
 ├─ OLED splash
 ├─ Sensor init (MPU6050, MAX30102, BME280)
 ├─ BT GPS scan → user chọn thiết bị → connect
 ├─ LoRa init (433MHz, SF9, BW125k, sync 0x12)
 ├─ Tạo Queue: sensorQ(1), navQ(1)
 └─ Spawn 5 FreeRTOS tasks

sensorTask [Core0, 5ms]:
  Đọc IMU luôn
  Đọc Health (mỗi 40ms = 8×5ms)
  Đọc Env (mỗi 1000ms = 200×5ms)
  → xQueueOverwrite(sensorQ)

gpsTask [Core0, 100ms]:
  btgps::update() → parse NMEA → cập nhật _lat/_lon

fusionTask [Core1, 5ms]:
  lấy snap từ sensorQ
  Madgwick.update(gyro, accel) → yaw
  if GPS fixed: lấy tọa độ GPS, reset EKF
  else: step detection → EKF.predict(dx, dy, heading)
  → xQueueOverwrite(navQ)

loraTask [Core1, 1000ms]:
  lấy snap + navData
  buildFlags (kiểm tra validity từng sensor)
  đọc voltage pin 35 → battery flags
  đóng gói SoldierPacket (40 bytes)
  CRC16-CCITT → transmit LoRa (retry 3 lần)
  pollReceive 800ms (nhận lệnh từ gateway)

displayTask [Core1, 500ms]:
  oled.update(snap, nd, gps, lora, flags)
```

### 3.2 Gateway Node — Polling Loop

```
BOOT
 ├─ Wire.begin → AXP2101 (ALDO3=GPS, ALDO2=LoRa, DC1=OLED)
 ├─ OLED splash
 ├─ LoRa.begin(433MHz) → configure → polling mode
 └─ GPS_Serial(UART1, GPIO34 RX, GPIO12 TX, 9600 baud)

loop():
  Đọc GPS UART → TinyGPS++ decode → currentLat/Lon
  packetSize = LoRa.parsePacket()
  if packetSize == 40:
    readBytes → rxPkt
    validateCRC → if OK:
      updateNodeList(nodeId)
      totalMessages++
      if gpsValid: Serial.print("GW_GPS:lat,lon")
      sendPacketToSerial([0xAA][40 bytes][0x55])
    else: reject (CRC fail)
  processSerialCommand() ← nhận lệnh từ PC, TX LoRa
  if 1s: updateDisplay()
```

### 3.3 Server — Data Pipeline

```
Serial port
  → SerialCommunicator._read_loop() [thread]
  → _process_buffer():
      if "GW_GPS:..." → gps_callback → soldier_manager.update_gateway_position()
      if 0xAA + 40 bytes + 0x55:
          parse_packet() → CRC verify
          packet_callback → soldier_manager.update_soldier()

Flask API:
  GET /api/soldiers
    → soldier_manager.get_all_soldiers()
    → + team_manager.get_team_name()
    → + alert_manager.evaluate_soldier() [threshold check]
    → JSON response

Alert Engine (evaluate_soldier):
  HR: <30 CRITICAL, <40 WARNING, >150 CRITICAL, >120 WARNING
  SpO2: <80 CRITICAL, <90 WARNING
  Temp: <34 or >40 → CRITICAL; <35 or >38.5 → WARNING
  Battery: <3.3V CRITICAL, <3.5V WARNING
  ManDown flag → EMERGENCY (level 3)
  HeatStress flag → CRITICAL
  Offline → CRITICAL
```

---

## 4. CÁC LỖI PHÁT HIỆN TRONG DỰ ÁN

### 🔴 BUG-001 — Serial Framing Mismatch (CRITICAL)

**Vị trí:** `gateway_node/src/main.cpp` L158-162 vs `serial_comm.py` L101-106

**Vấn đề:** Gateway gửi frame `[0xAA][40 bytes][0x55]` (42 bytes). Server tìm `PKT_END` tại vị trí `buffer[PACKET_SIZE + 1]` = `buffer[41]` — **đúng**. Nhưng gateway **không gửi CRC16 riêng** trong frame serial (chỉ gửi raw 40 bytes), trong khi README mô tả `[0xAA][40 bytes][CRC16 lo][CRC16 hi][0x55]` = 44 bytes.

**Hậu quả:** Nếu ai đó update server theo README sẽ bị lệch frame.

---

### 🔴 BUG-002 — Battery Voltage Đọc Sai (HIGH)

**Vị trí:** `soldier_node_production/src/lora_manager.cpp` L27

```cpp
return analogRead(cfg::VBAT_PIN) * (3.3f / 4095.0f) * 2.0f;
```

**Vấn đề:** TTGO T-Beam dùng AXP2101 PMU để quản lý pin. Pin KHÔNG được đo trực tiếp qua GPIO 35 trên T-Beam v1.2 — cần đọc qua AXP2101 ADC. Đọc GPIO 35 cho kết quả không chính xác hoặc luôn = 0.

**Sửa:** Dùng `pmu.getBattVoltage()` từ XPowersLib.

---

### 🟠 BUG-003 — GPS Bluetooth Block Setup() (MEDIUM)

**Vị trí:** `gps_manager.cpp` L9-13

```cpp
while (!btgps::isConnected()) {
    btgps::update();
    delay(50);
}
```

**Vấn đề:** Vòng lặp blocking này chạy trong `setup()` — **toàn bộ hệ thống bị treo** cho đến khi user kết nối BT GPS. Nếu không có GPS Bluetooth, node không thể chạy dù EKF dead-reckoning không cần GPS.

**Sửa:** Thêm timeout 30s, sau đó tiếp tục với dead-reckoning.

---

### 🟠 BUG-004 — EKF updateGPS() Không Bao Giờ Được Gọi (MEDIUM)

**Vị trí:** `navigation.cpp` L34-41, `ekf.h` L48

**Vấn đề:** Khi GPS fixed, code chỉ gọi `ekf.setState(lonM, latM)` — reset cứng trạng thái EKF. Hàm `ekf.updateGPS()` (bước Kalman update đúng nghĩa) **không bao giờ được gọi**. EKF chỉ hoạt động ở chế độ predict (dead-reckoning), không tích hợp GPS measurement đúng cách.

---

### 🟠 BUG-005 — pollReceive() Blocking 800ms Trong loraTask (MEDIUM)

**Vị trí:** `lora_manager.cpp` L70-83

```cpp
void LoRaManager::pollReceive() {
    LoRa.receive();
    uint32_t start = millis();
    while (millis() - start < 800) { ... delay(10); }
}
```

**Vấn đề:** `loraTask` có chu kỳ 1000ms, nhưng `pollReceive()` chiếm 800ms → task thực tế chạy mỗi ~1.8s thay vì 1s, **sai lệch timing** đáng kể.

---

### 🟡 BUG-006 — Soldier Node Nhận Gói 32 Bytes Nhưng Check Sai (LOW)

**Vị trí:** `lora_manager.cpp` L76

```cpp
if (sz == 32) { handleRxPacket(); }
```

Downlink message = 32 bytes. `handleRxPacket()` dùng `buf[0] == 0xFF` để nhận dạng. Đây là magic byte dễ xung đột nếu có LoRa noise ngẫu nhiên bằng đúng 32 bytes.

---

### 🟡 BUG-007 — activeNodes Array Không Compact Khi Node Timeout (LOW)

**Vị trí:** `gateway_node/src/main.cpp` L98-110

`getActiveNodeCount()` đếm node active nhưng `activeNodes[]` không bao giờ được dọn dẹp. Sau 100 node khác nhau đã từng connect, array đầy và node mới không được thêm dù array chứa nhiều node đã timeout lâu rồi.

---

### 🟡 BUG-008 — packet.h Include Arduino.h Nhưng Gateway Dùng PACKET_SIZE Trước Init (LOW)

**Vị trí:** `gateway_node/include/packet.h` — file này dùng `Arduino.h`. Gateway bao gồm header này ổn, nhưng nếu dùng trong unit test PC-side sẽ không compile được.

---

## 5. SƠ ĐỒ ĐẤU NỐI PHẦN CỨNG

### 5.1 SOLDIER NODE — TTGO T-Beam v1.2 (ESP32)

> Board TTGO T-Beam v1.2 có sẵn: LoRa SX1276, GPS UART, AXP2101 PMU, nút IO38. Các module dưới đây là external sensor cần đấu nối.

#### I2C Bus (SDA=GPIO21, SCL=GPIO22, tốc độ 400kHz)

Tất cả sensor I2C đấu chung 1 bus:

```
ESP32 GPIO21 (SDA) ──────────────────────────────────
                   │           │           │
                MPU6050      MAX30102    BME280    SSD1306 OLED
                0x68         0x57        0x76      0x3C
                [IMU]      [Tim mạch]  [Môi trường] [Màn hình]

ESP32 GPIO22 (SCL) ──────────────────────────────────
                   │           │           │
                MPU6050      MAX30102    BME280    SSD1306 OLED

Nguồn 3.3V (từ AXP2101 ALDO4) → VCC tất cả sensor I2C
GND → GND tất cả sensor I2C
```

Pull-up resistors: 4.7kΩ từ SDA và SCL lên 3.3V (thường đã tích hợp trên module breakout).

---

#### SPI Bus — LoRa SX1276 (BUILT-IN trên T-Beam)

> **Không cần đấu nối thêm** — LoRa đã tích hợp trên board T-Beam.

| Signal | GPIO T-Beam |
|---|---|
| SCK | GPIO5 |
| MISO | GPIO19 |
| MOSI | GPIO27 |
| NSS/CS | GPIO18 |
| RST | GPIO23 |
| DIO0/IRQ | GPIO26 |

---

#### UART1 — GPS (BUILT-IN trên T-Beam)

> T-Beam có GPS module (u-blox Neo-6M) kết nối sẵn nội bộ. Với **Soldier Node**, GPS được đọc qua **Bluetooth** thay vì UART vật lý.

| Signal | GPIO |
|---|---|
| GPS TX → ESP32 RX | GPIO34 (input only) |
| GPS RX ← ESP32 TX | GPIO12 |

---

#### Bluetooth GPS — Kết Nối Không Dây

```
Smartphone / GPS BT Receiver
        ↕ Bluetooth Classic (SPP)
ESP32 Bluetooth (tên: "TBeam-DR")
        → Nhận NMEA ($GPRMC, $GPGGA)
```

User nhấn **nút IO38** (built-in T-Beam):
- **Short press**: chọn thiết bị tiếp theo
- **Long press (>800ms)**: kết nối

---

#### Battery & PMU

```
LiPo Battery ──→ AXP2101 PMU (I2C 0x34, GPIO21/22)
                 │
                 ├─ ALDO2 (3.3V) → LoRa module power
                 ├─ ALDO3 (3.3V) → GPS module power  
                 ├─ ALDO4 (3.3V) → Sensor power (IMU/Health/Env)
                 └─ DC1           → Main ESP32 power

GPIO35 (ADC) → Voltage divider → Battery monitoring
(⚠️ BUG-002: nên dùng pmu.getBattVoltage() thay thế)
```

---

#### Sơ Đồ Tổng Quát Soldier Node

```
┌─────────────────────────────────────────────────────┐
│                 TTGO T-Beam ESP32                    │
│                                                     │
│  GPIO21 (SDA) ─┬─────────────────────────────────  │
│  GPIO22 (SCL) ─┤                                    │
│                │   ┌──────────┐  ┌──────────┐       │
│                ├───│ MPU6050  │  │ SSD1306  │       │
│                │   │ 0x68 IMU │  │ 0x3C OLED│       │
│                │   └──────────┘  └──────────┘       │
│                │   ┌──────────┐  ┌──────────┐       │
│                └───│ MAX30102 │  │ BME280   │       │
│                    │ 0x57 HR  │  │ 0x76 Env │       │
│                    └──────────┘  └──────────┘       │
│                                                     │
│  GPIO5/19/27/18/23/26 ── LoRa SX1276 (built-in)    │
│                                                     │
│  GPIO34 (RX1) ── GPS UART (built-in, not used*)    │
│  GPIO38 (INPUT) ── Button (built-in)               │
│  GPIO35 (ADC) ── Battery voltage divider           │
│                                                     │
│  Bluetooth ↔ External GPS receiver (BT Classic)   │
└─────────────────────────────────────────────────────┘
         ↕ LoRa 433MHz
┌─────────────────────────────┐
│      GATEWAY NODE           │
└─────────────────────────────┘
```

---

### 5.2 GATEWAY NODE — TTGO T-Beam v1.2 (ESP32)

Gateway đơn giản hơn — **không cần external sensor**, chỉ dùng hardware built-in trên T-Beam.

#### I2C Bus (SDA=GPIO21, SCL=GPIO22)

```
GPIO21/22 ──→ AXP2101 PMU (0x34) — quản lý nguồn
           └→ SSD1306 OLED (0x3C) — hiển thị trạng thái
```

#### SPI — LoRa SX1276 (Built-in)

| Signal | GPIO |
|---|---|
| SCK | GPIO5 |
| MISO | GPIO19 |
| MOSI | GPIO27 |
| NSS/CS | GPIO18 |
| RST | GPIO23 |
| DIO0 | GPIO26 |

#### UART1 — GPS (Built-in T-Beam)

| Signal | GPIO |
|---|---|
| GPS TX → ESP32 RX | GPIO34 |
| GPS RX ← ESP32 TX | GPIO12 |
| Baud | 9600 |

#### USB Serial — Kết Nối Laptop

```
Gateway ESP32 UART0 (GPIO1/3)
        ↕ USB-to-Serial (CH340 on T-Beam)
        ↕ USB cable
Laptop / Field Computer
        ↕ COM port (COMx hoặc /dev/ttyUSB0)
Python Server (app.py --port COMx)
```

#### PMU Gateway

```
LiPo ──→ AXP2101
         ├─ ALDO2 (3.3V) → LoRa power
         ├─ ALDO3 (3.3V) → GPS power
         └─ DC1  (3.3V) → OLED power
```

#### Sơ Đồ Tổng Quát Gateway Node

```
┌─────────────────────────────────────────────────────┐
│                TTGO T-Beam ESP32 (Gateway)           │
│                                                     │
│  GPIO21 (SDA) ──┬─ AXP2101 PMU (0x34)              │
│  GPIO22 (SCL) ──┘─ SSD1306 OLED (0x3C)             │
│                                                     │
│  GPIO5/19/27/18/23/26 ── LoRa SX1276 (built-in)    │
│          ↑↓ 433MHz RF                               │
│                                                     │
│  GPIO34 (RX1) ── Built-in GPS u-blox (9600 baud)   │
│  GPIO12 (TX1) ── Built-in GPS                       │
│                                                     │
│  GPIO1/3 UART0 ── USB Serial ── Laptop              │
│                                                     │
│  LiPo ── AXP2101 (ALDO2/3, DC1)                   │
└─────────────────────────────────────────────────────┘
         ↑ USB Cable
┌─────────────────────────────┐
│  LAPTOP / FIELD COMPUTER    │
│  Python Flask Server        │
│  http://localhost:5000      │
└─────────────────────────────┘
```

---

### 5.3 Sơ Đồ Hệ Thống Tổng Thể

```
┌──────────────┐     LoRa 433MHz      ┌──────────────┐
│  Soldier #1  │ ──────────────────→  │              │
│  T-Beam ESP32│                      │   GATEWAY    │
│  + MPU6050   │                      │  T-Beam ESP32│
│  + MAX30102  │ ←─────────────────── │  (thu/phát)  │
│  + BME280    │  Command downlink     │              │
│  + OLED      │                      └──────┬───────┘
└──────────────┘                             │ USB Serial
                                             │ [0xAA][40B][0x55]
┌──────────────┐                             │
│  Soldier #2  │ ──────────────────→         │
│  T-Beam ESP32│                      ┌──────▼───────┐
│  ...         │ ←───────────────────  │   LAPTOP     │
└──────────────┘                      │ Flask Server  │
                                      │ Port 5000     │
┌──────────────┐                      │               │
│  Soldier #N  │ ──────────────────→  │ Dashboard     │
│  T-Beam ESP32│ ←───────────────────  │ REST API      │
└──────────────┘                      └───────────────┘
       ↕ BT Classic
  GPS Receiver/Phone
```

---

## 6. TÓM TẮT VẤN ĐỀ CẦN XỬ LÝ

| # | Mức độ | Vấn đề | File |
|---|---|---|---|
| BUG-001 | 🔴 CRITICAL | Serial frame format không khớp README | gateway/main.cpp |
| BUG-002 | 🔴 HIGH | Battery voltage đọc sai (GPIO thay vì AXP PMU) | lora_manager.cpp |
| BUG-003 | 🟠 MEDIUM | GPS BT blocking vô hạn trong setup() | gps_manager.cpp |
| BUG-004 | 🟠 MEDIUM | EKF.updateGPS() không được gọi | navigation.cpp |
| BUG-005 | 🟠 MEDIUM | pollReceive() blocking 800ms làm sai timing | lora_manager.cpp |
| BUG-006 | 🟡 LOW | Nhận dạng downlink packet dễ false positive | lora_manager.cpp |
| BUG-007 | 🟡 LOW | activeNodes array không dọn dẹp khi đầy | gateway/main.cpp |

---

## 7. KHUYẾN NGHỊ TIẾP THEO

1. **Fix BUG-002 ngay** — Battery reading sai làm cảnh báo pin không hoạt động
2. **Fix BUG-003** — Thêm timeout 30s để node có thể chạy không có BT GPS
3. **Fix BUG-004** — Gọi `ekf.updateGPS()` thay vì `ekf.setState()` khi có GPS fix
4. **Fix BUG-005** — Giảm `pollReceive` timeout hoặc dùng interrupt
5. **Clarify BUG-001** — Quyết định frame format cuối cùng và đồng bộ README

> Dùng `/vp-evolve` để lập kế hoạch fix và `/vp-auto` để thực thi.
