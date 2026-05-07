# Brainstorm Session — Solar-Powered Mesh Node Wiring Guide
**Date:** 2026-05-06 (Updated)  
**Topic:** Hướng dẫn đấu nối & hàn mạch Solar + LilyGo LoRa32 Mesh Node  
**Status:** In Progress  
**Board xác nhận:** LilyGo LoRa32 433/470MHz (KHÔNG phải T-Beam)

---

## ⚠️ SỬA LỖI QUAN TRỌNG

Board thực tế là **LilyGo LoRa32** (LoRa32 V2.1), KHÔNG phải T-Beam.  
Khác biệt chính:
- **Không có GPS tích hợp** → BẮT BUỘC phải dùng GPS NEO-6M ngoài
- **Không có khe pin 18650** → phải cấp nguồn hoàn toàn từ bên ngoài
- **Có cổng JST 1.25mm** cho pin LiPo → có thể dùng để cấp nguồn pin
- **Có OLED SSD1306 tích hợp** trên I2C (GPIO21/22)

---

## Danh sách linh kiện

| # | Linh kiện | SL | Vai trò |
|---|-----------|-----|---------|
| 1 | **LilyGo LoRa32** 433/470MHz | 1 | MCU — LoRa mesh, OLED |
| 2 | **BMP280** (cảm biến môi trường) | 1 | Nhiệt độ, áp suất |
| 3 | **GPS GY-GPS6MV2** (u-blox NEO-6M) | 1 | Định vị GPS |
| 4 | **LM2596** (hạ áp có LED hiển thị) | 1 | Hạ 6V solar → 5V |
| 5 | **TP4056** (sạc Li-Ion + bảo vệ) | 3 | Sạc 3 cell 18650 |
| 6 | **Pin 18650** 3.7V | 3 | Lưu trữ năng lượng |
| 7 | **Pin mặt trời** 25W 6V | 1 | Nguồn năng lượng |
| 8 | **Diode 1N5819** Schottky | 1 | Chống dòng ngược |

---

## SƠ ĐỒ CHÂN CHI TIẾT TỪNG MODULE (ĐỂ HÀN)

### 📌 MODULE 1: LilyGo LoRa32 433/470MHz

Nhìn board **mặt trước** (có OLED + chip LoRa), **USB ở dưới**, **anten SMA ở trên**:

```
            [SMA Antenna]
         ┌────────────────────┐
         │    LoRa SX1276     │
         │   433/470MHz       │
         │                    │
    Trái │                    │ Phải
         │   ┌──────────┐    │
         │   │  OLED     │    │
         │   │  SSD1306  │    │
         │   └──────────┘    │
         │                    │
         │   [ESP32 chip]     │
         │                    │
         └──────┤USB├─────────┘

  HÀNG CHÂN TRÁI          HÀNG CHÂN PHẢI
  (nhìn từ mặt trước)     (nhìn từ mặt trước)
  ──────────────────       ──────────────────
  3V3  ← nguồn 3.3V out   IO25
  GND  ← mass chung        IO26 (LoRa DIO0)
  IO15                     IO27 (LoRa MOSI)
  IO2                      IO14
  IO4   ← GPS TX pin       IO12 ← ★ GPS RX pin
  IO16  (RX0)              IO13
  IO17  (TX0)              IO21 ← ★ SDA (I2C)
  IO5   (LoRa SCK)         IO22 ← ★ SCL (I2C)
  IO18  (LoRa CS)          IO23 (LoRa RST)
  IO19  (LoRa MISO)        IO1  (TX0/USB)
  GND                      IO3  (RX0/USB)
  3V3                      GND
  5V   ← ★ nguồn 5V IN    5V
```

**Các chân CẦN HÀN DÂY:**

| Chân LoRa32 | Hàn dây tới | Mục đích | Vị trí trên board |
|-------------|-------------|----------|-------------------|
| **5V** | TP4056 OUT+ (chung) | Nguồn vào | Hàng trái, chân cuối cùng |
| **GND** | TP4056 OUT- (chung) | Mass | Hàng trái, chân áp cuối |
| **3V3** | BMP280 VCC + GPS VCC | Cấp nguồn sensor | Hàng trái, chân đầu tiên |
| **GND** | BMP280 GND + GPS GND | Mass sensor | Hàng trái, chân thứ 2 |
| **IO21** | BMP280 SDA | I2C Data | Hàng phải |
| **IO22** | BMP280 SCL | I2C Clock | Hàng phải |
| **IO34** *(input-only)* | GPS TX | GPS nhận data | Hàng phải (nếu có) |
| **IO4** | GPS TX (phương án thay thế) | GPS data | Hàng trái |
| **IO12** | GPS RX | GPS gửi lệnh | Hàng phải |

> ⚠️ **GPIO34 là input-only** — dùng làm RX (nhận từ GPS) rất tốt. Nếu board không có chân 34 trên header, dùng **IO4** thay thế.

---

### 📌 MODULE 2: GPS GY-GPS6MV2 (NEO-6M)

Board GPS nhìn **mặt trước** (có chip u-blox), **header 4 chân ở trên**:

```
       VCC  RX  TX  GND     ← 4 chân header (hàn thẳng)
        │    │   │    │
  ┌─────┴────┴───┴────┴──┐
  │  ○               ○   │  ← 2 lỗ bắt vít
  │                       │
  │   ┌───────────────┐   │
  │   │  u-blox       │   │
  │   │  NEO-6M-0-001 │   │
  │   │               │   │
  │   └───────────────┘   │
  │              GY-GPS6MV2│
  │  ○               ○   │
  │            [SMA]      │  ← cổng anten GPS
  └───────────────────────┘
```

**Bảng hàn dây GPS → LoRa32:**

| Chân GPS | Hàn tới chân LoRa32 | Dây màu | Ghi chú |
|----------|---------------------|---------|---------|
| **VCC** | **3V3** | 🔴 Đỏ | NEO-6M hoạt động 2.7V–3.6V |
| **RX** | **IO12** | 🟡 Vàng | LoRa32 gửi → GPS nhận |
| **TX** | **IO4** (hoặc IO34) | 🟢 Xanh | GPS gửi → LoRa32 nhận |
| **GND** | **GND** | ⚫ Đen | Mass chung |

> **Firmware code tương ứng:**
> ```cpp
> #define GPS_RX_PIN  4    // LoRa32 nhận ← GPS TX
> #define GPS_TX_PIN  12   // LoRa32 gửi → GPS RX
> HardwareSerial GPSSerial(2);
> GPSSerial.begin(9600, SERIAL_8N1, GPS_RX_PIN, GPS_TX_PIN);
> ```

---

### 📌 MODULE 3: BMP280 (Cảm biến môi trường)

Module BMP280 thường có **4 hoặc 6 chân**. Phiên bản 4 chân phổ biến:

```
  ┌───────────────┐
  │    BMP280     │
  │               │
  │  ┌─────────┐  │
  │  │ Bosch   │  │
  │  │ BMP280  │  │
  │  └─────────┘  │
  │               │
  └─┬──┬──┬──┬───┘
    │  │  │  │
   VCC GND SCL SDA    ← 4 chân (có thể khác thứ tự tùy module)
```

**Bảng hàn dây BMP280 → LoRa32:**

| Chân BMP280 | Hàn tới LoRa32 | Dây màu | Ghi chú |
|-------------|----------------|---------|---------|
| **VCC** | **3V3** | 🔴 Đỏ | Nguồn 3.3V |
| **GND** | **GND** | ⚫ Đen | Mass |
| **SCL** | **IO22** | 🔵 Xanh dương | I2C Clock — chia sẻ với OLED |
| **SDA** | **IO21** | 🟣 Tím | I2C Data — chia sẻ với OLED |

> **Địa chỉ I2C:** `0x76` (mặc định). OLED dùng `0x3C` → không xung đột.
> BMP280 dùng chung bus I2C với OLED tích hợp — chỉ cần nối song song.

---

### 📌 MODULE 4: LM2596 (Mạch hạ áp có LED)

Board LM2596 của bạn có **LED hiển thị điện áp** + **4 cổng domino** (screw terminal):

```
  Nhìn mặt trước (có LED 7-đoạn):

  ┌─────────────────────────────────┐
  │  [OUT-] [OUT+]   [888]   [IN-] [IN+]  │
  │   ▓▓▓    ▓▓▓    LED      ▓▓▓   ▓▓▓   │
  │                display                  │
  │   ┌─┐                                  │
  │   │░│ ← potentiometer (vặn chỉnh Vout) │
  │   └─┘                                  │
  │  [LM2596S]  [220/35V] [220/35V]       │
  └─────────────────────────────────┘

  Bên TRÁI: OUT- , OUT+  (đầu ra - hạ áp)
  Bên PHẢI: IN+  , IN-   (đầu vào - solar)
```

**Bảng hàn dây LM2596:**

| Cổng domino | Hàn tới | Dây màu | Ghi chú |
|-------------|---------|---------|---------|
| **IN+** (phải) | Solar (+) qua diode 1N5819 | 🔴 Đỏ | Đầu vào dương |
| **IN-** (phải) | Solar (-) | ⚫ Đen | Đầu vào âm |
| **OUT+** (trái) | 3× TP4056 IN+ (song song) | 🔴 Đỏ | Đầu ra 5V |
| **OUT-** (trái) | 3× TP4056 IN- (song song) | ⚫ Đen | Đầu ra GND |

> **Chỉnh điện áp:** Vặn potentiometer (ốc xanh nhỏ) → LED hiển thị **5.00V** → OK

---

### 📌 MODULE 5: TP4056 (Mạch sạc + bảo vệ)

Board TP4056 có **Micro-USB ở dưới**, **6 pad hàn**:

```
  Nhìn mặt sau (có IC 8205A + TP4056):

  ┌─────────────────┐
  │ [OUT+]   [OUT-] │  ← Pad trên: đầu ra (tải/load)
  │                 │
  │  [B+]    [B-]   │  ← Pad giữa: pin 18650
  │                 │
  │  [8205A]        │  ← IC bảo vệ quá sạc/quá xả
  │                 │
  │  [TP4056]       │  ← IC sạc
  │                 │
  │  [+]     [-]    │  ← Pad dưới: đầu vào (= IN+/IN-)
  │  ┌─────────┐   │
  │  │microUSB │   │  ← Hoặc cấp nguồn qua USB
  │  └─────────┘   │
  └─────────────────┘
```

**Bảng hàn dây TP4056 (×3 mạch giống nhau):**

| Pad TP4056 | Hàn tới | Dây màu | Ghi chú |
|------------|---------|---------|---------|
| **+ (IN+)** pad dưới | LM2596 OUT+ (chung 3 mạch) | 🔴 Đỏ | Nguồn 5V vào |
| **- (IN-)** pad dưới | LM2596 OUT- (chung 3 mạch) | ⚫ Đen | GND vào |
| **B+** pad giữa | Pin 18650 đầu **(+) nhô** | 🔴 Đỏ | Sạc pin dương |
| **B-** pad giữa | Pin 18650 đầu **(-) phẳng** | ⚫ Đen | Sạc pin âm |
| **OUT+** pad trên | Điểm nối chung (+) → LoRa32 5V | 🔴 Đỏ | Cấp nguồn ra |
| **OUT-** pad trên | Điểm nối chung (-) → LoRa32 GND | ⚫ Đen | GND ra |

> **LED:** 🔴 Đỏ = đang sạc | 🟢 Xanh/xanh lá = pin đầy

---

### 📌 Diode 1N5819 Schottky

```
  Chiều nối diode:

  Solar(+) ───►|──── LM2596 IN+
              Anode  Cathode
               │      │
               ○──────○
               │ vạch │
               │ trắng│
               │  →   │
               └──────┘
  
  Vạch trắng (cathode) hướng VỀ PHÍA LM2596
```

---

## SƠ ĐỒ TỔNG THỂ HỆ THỐNG (ĐÃ CẬP NHẬT)

```
┌─────────────────┐
│  PIN MẶT TRỜI   │
│   25W / 6V      │
│   (+)     (-)   │
└───┬────────┬────┘
    │        │
    ▼        │
 [1N5819]    │     Vạch trắng → LM2596
    │        │
    ▼        ▼
┌───────────────────────────┐
│   LM2596 (Buck + LED)    │
│  IN+           IN-       │  ← Bên PHẢI board
│  OUT+          OUT-      │  ← Bên TRÁI board
│  Chỉnh potentiometer=5V │
└──┬──────────────┬────────┘
   │(+)           │(-)
   │              │
   ├──────┬───────┼──────┬───────┐
   │      │       │      │       │
   ▼      │       ▼      │       ▼
┌──────┐  │    ┌──────┐  │    ┌──────┐
│TP4056│  │    │TP4056│  │    │TP4056│
│  #1  │  │    │  #2  │  │    │  #3  │
│IN+IN-│  │    │IN+IN-│  │    │IN+IN-│
│      │  │    │      │  │    │      │
│B+ B- │  │    │B+ B- │  │    │B+ B- │
│  ││  │  │    │  ││  │  │    │  ││  │
└──┼┼──┘  │    └──┼┼──┘  │    └──┼┼──┘
   ││     │       ││     │       ││
  ┌▼▼──┐  │     ┌▼▼──┐  │     ┌▼▼──┐
  │18650│  │     │18650│  │     │18650│
  │ #1  │  │     │ #2  │  │     │ #3  │
  └─────┘  │     └─────┘  │     └─────┘
           │              │
 OUT+ chung (3 TP4056 //) │ OUT- chung
           │              │
           ▼              ▼
┌──────────────────────────────────────┐
│       LilyGo LoRa32 433MHz         │
│  5V  ← OUT+ chung                   │
│  GND ← OUT- chung                   │
│                                      │
│  3V3 ──┬──► BMP280 VCC              │
│        └──► GPS VCC                  │
│  GND ──┬──► BMP280 GND              │
│        └──► GPS GND                  │
│                                      │
│  IO21 (SDA) ◄──► BMP280 SDA         │
│  IO22 (SCL) ◄──► BMP280 SCL         │
│                                      │
│  IO4  (RX2) ◄── GPS TX              │
│  IO12 (TX2) ──► GPS RX              │
└──────────────────────────────────────┘
```

---

## Bảng tổng hợp tất cả mối hàn

| # | Từ module | Chân | → Tới module | Chân | Dây |
|---|-----------|------|-------------|------|-----|
| 1 | Solar | (+) | Diode 1N5819 | Anode | 🔴 |
| 2 | Diode 1N5819 | Cathode | LM2596 | IN+ | 🔴 |
| 3 | Solar | (-) | LM2596 | IN- | ⚫ |
| 4 | LM2596 | OUT+ | TP4056 #1 | + (IN) | 🔴 |
| 5 | LM2596 | OUT+ | TP4056 #2 | + (IN) | 🔴 |
| 6 | LM2596 | OUT+ | TP4056 #3 | + (IN) | 🔴 |
| 7 | LM2596 | OUT- | TP4056 #1 | - (IN) | ⚫ |
| 8 | LM2596 | OUT- | TP4056 #2 | - (IN) | ⚫ |
| 9 | LM2596 | OUT- | TP4056 #3 | - (IN) | ⚫ |
| 10 | TP4056 #1 | B+ | 18650 #1 | (+) nhô | 🔴 |
| 11 | TP4056 #1 | B- | 18650 #1 | (-) phẳng | ⚫ |
| 12 | TP4056 #2 | B+ | 18650 #2 | (+) nhô | 🔴 |
| 13 | TP4056 #2 | B- | 18650 #2 | (-) phẳng | ⚫ |
| 14 | TP4056 #3 | B+ | 18650 #3 | (+) nhô | 🔴 |
| 15 | TP4056 #3 | B- | 18650 #3 | (-) phẳng | ⚫ |
| 16 | TP4056 #1 | OUT+ | Nối chung (+) | — | 🔴 |
| 17 | TP4056 #2 | OUT+ | Nối chung (+) | — | 🔴 |
| 18 | TP4056 #3 | OUT+ | Nối chung (+) | — | 🔴 |
| 19 | TP4056 #1 | OUT- | Nối chung (-) | — | ⚫ |
| 20 | TP4056 #2 | OUT- | Nối chung (-) | — | ⚫ |
| 21 | TP4056 #3 | OUT- | Nối chung (-) | — | ⚫ |
| 22 | Nối chung | (+) | LoRa32 | 5V | 🔴 |
| 23 | Nối chung | (-) | LoRa32 | GND | ⚫ |
| 24 | LoRa32 | 3V3 | BMP280 | VCC | 🔴 |
| 25 | LoRa32 | GND | BMP280 | GND | ⚫ |
| 26 | LoRa32 | IO21 | BMP280 | SDA | 🟣 |
| 27 | LoRa32 | IO22 | BMP280 | SCL | 🔵 |
| 28 | LoRa32 | 3V3 | GPS | VCC | 🔴 |
| 29 | LoRa32 | GND | GPS | GND | ⚫ |
| 30 | LoRa32 | IO4 | GPS | TX | 🟢 |
| 31 | LoRa32 | IO12 | GPS | RX | 🟡 |

**Tổng: 31 mối hàn**

---

## GIẢI THÍCH CHI TIẾT CÁCH ĐẤU KHỐI NGUỒN (TP4056 + PIN + JST)

Nhiều người thắc mắc tại sao dùng 3 mạch TP4056 cho 3 cục pin và đấu nối thế nào cho an toàn. Dưới đây là giải thích luồng đi của điện:

### Bước 1: Ghép 3 cục pin 18650 (ĐẤU SONG SONG)
- **Tuyệt đối đấu SONG SONG**: Nối tất cả 3 cực Dương (+) của 3 viên pin lại với nhau thành 1 cụm Dương chung. Nối 3 cực Âm (-) lại với nhau thành 1 cụm Âm chung.
- **Lý do**: Mạch LoRa32 dùng điện áp 3.7V. Đấu song song sẽ giữ nguyên điện áp 3.7V nhưng cộng dồn dung lượng (ví dụ 3 viên 3000mAh = 9000mAh). Nếu bạn đấu nối tiếp, điện áp sẽ lên 11.1V và **cháy mạch LoRa ngay lập tức**.

### Bước 2: Ghép 3 mạch sạc TP4056 (ĐẤU SONG SONG)
- **Tại sao dùng 3 mạch?**: Một mạch TP4056 cung cấp dòng sạc tối đa 1A. Với khối pin khổng lồ 9000mAh, nếu dùng 1 mạch sẽ sạc mất 9-10 tiếng. Dùng 3 mạch đấu song song sẽ gộp dòng sạc lên 3A, sạc đầy pin chỉ trong vài tiếng nắng to.
- **Cách đấu**: 
  1. Cấp nguồn: Nối gộp cả 3 ngõ `IN+` (hoặc dấu `+` cạnh cổng USB) của 3 mạch lại. Nối gộp 3 ngõ `IN-` lại.
  2. Nối vào Pin: Nối gộp 3 ngõ `B+` lại rồi hàn vào cụm Dương của Pin. Nối gộp 3 ngõ `B-` lại rồi hàn vào cụm Âm của Pin.
  3. Xuất điện cho mạch: Nối gộp 3 ngõ `OUT+` lại. Nối gộp 3 ngõ `OUT-` lại.

### Bước 3: Đấu Solar và LM2596 cấp nguồn cho TP4056
- Nối dây Dương (+) của tấm pin Solar vào 1 đầu Diode. Đầu kia của Diode nối vào domino `IN+` của LM2596.
- Nối dây Âm (-) của tấm pin Solar vào domino `IN-` của LM2596.
- **Bắt buộc**: Phải để LM2596 ngoài nắng để có điện, sau đó lấy tua-vít vặn con ốc xanh dương trên LM2596 cho màn hình LED hiện chính xác **5.00V**.
- Sau khi có 5V, nối domino `OUT+` của LM2596 vào ngõ `IN+` (đã gộp) của cụm TP4056. Nối `OUT-` vào `IN-`.

### Bước 4: Nối giắc JST cấp điện cho LoRa32
- Kiếm một sợi dây có giắc cắm nhựa **JST PH2.0 màu trắng** (loại 2 chân). Sợi cáp này thường có sẵn 1 dây Đỏ và 1 dây Đen.
- Hàn **dây Đỏ** của giắc JST vào ngõ `OUT+` (đã gộp) của cụm TP4056.
- Hàn **dây Đen** của giắc JST vào ngõ `OUT-` (đã gộp).
- Cuối cùng, cầm giắc JST màu trắng cắm thẳng vào cổng cắm pin bằng nhựa dưới đuôi mạch LoRa32.

> **💡 Mẹo:** Làm theo cách này, mạch LoRa32 sẽ nhận diện đây là một "cục pin" duy nhất. Mạch sẽ tự động chạy bằng pin, và tự động dùng điện mặt trời để sạc!

---

## Mẹo hàn mạch

1. **Nhiệt độ mỏ hàn:** 320°C–360°C cho dây đồng, 280°C–320°C cho pad PCB nhỏ
2. **Thứ tự hàn:** Hàn nguồn (LM2596→TP4056) trước → test → rồi mới hàn sensor
3. **Dùng co nhiệt** bọc mỗi mối hàn để tránh chập
4. **Dây tín hiệu** (SDA/SCL/TX/RX) nên ngắn nhất có thể (<15cm)
5. **Dây nguồn** (5V/GND) dùng dây 22AWG, dây tín hiệu dùng 26-28AWG
6. **Test từng giai đoạn:** Solar→LM2596 OK? → TP4056 OK? → 18650 sạc OK? → rồi mới nối LoRa32

---

## GPIO LoRa32 — Chân đã dùng vs chân trống

| GPIO | Dùng cho | Trạng thái |
|------|----------|-----------|
| 1, 3 | USB Serial | ❌ Không dùng |
| 5 | LoRa SCK | ❌ LoRa |
| 18 | LoRa CS | ❌ LoRa |
| 19 | LoRa MISO | ❌ LoRa |
| 23 | LoRa RST | ❌ LoRa |
| 26 | LoRa DIO0 | ❌ LoRa |
| 27 | LoRa MOSI | ❌ LoRa |
| 21 | I2C SDA (OLED + BMP280) | ✅ Đã dùng |
| 22 | I2C SCL (OLED + BMP280) | ✅ Đã dùng |
| 4 | GPS RX (nhận từ GPS) | ✅ Đã dùng |
| 12 | GPS TX (gửi tới GPS) | ✅ Đã dùng |
| 35 | VBAT ADC (đo pin) | ✅ Tích hợp |
| 2 | — | 🟢 Trống |
| 13 | — | 🟢 Trống |
| 14 | — | 🟢 Trống |
| 15 | — | 🟢 Trống (strapping) |
| 16 | — | 🟢 Trống |
| 17 | — | 🟢 Trống |
| 25 | — | 🟢 Trống |

---

## Decisions (cập nhật)

| # | Quyết định | Lý do |
|---|-----------|-------|
| D1 | Board = LoRa32, KHÔNG phải T-Beam | Xác nhận từ ảnh thực tế |
| D2 | GPS NEO-6M là BẮT BUỘC | LoRa32 không có GPS tích hợp |
| D3 | GPS dùng IO4 (RX) + IO12 (TX) | IO4 an toàn, IO34 có thể không có trên header |
| D4 | LM2596 hạ 6V→5V trước TP4056 | Tối ưu hiệu suất sạc |
| D5 | 3 TP4056 song song output → 5V LoRa32 | Tăng dung lượng ~9000mAh |
| D6 | Diode 1N5819 chống ngược | Bảo vệ ban đêm |
| D7 | BMP280 chia sẻ I2C bus với OLED | Cùng bus, khác địa chỉ |

## Open Questions

- [ ] Cần thêm sensor nào nữa? (còn GPIO trống: 2, 13, 14, 15, 16, 17, 25)
- [ ] Cần đo humidity? → Nên dùng BME280 thay BMP280
- [ ] Cần hộp chống nước không?

## Phases

### Phase 1 — Hàn hệ thống nguồn Solar (mối hàn #1–#23)
- Hàn diode + LM2596 + 3× TP4056 + pin
- Test sạc: LED đỏ = OK

### Phase 2 — Hàn sensor vào LoRa32 (mối hàn #24–#31)
- Hàn BMP280 (I2C) + GPS (UART)
- Flash firmware + test

### Phase 3 — Tối ưu
- Cập nhật firmware config cho LoRa32
- Test thời lượng pin outdoor
- Đóng hộp 3D

---

## Next Steps

> Sử dụng `/vp-crystallize` để chuyển brainstorm này thành task thực thi.
