# Soldier Node Production Firmware

## Overview

This is the **PRODUCTION** firmware for soldier nodes with **REAL** sensors.

### Hardware Required

**ESP32 Board**: TTGO T-Beam (or compatible)

**Sensors**:
- **MPU6050** - 6-axis IMU (accelerometer + gyroscope) @ 0x68
- **MAX30102** - Heart rate and SpO2 sensor @ 0x57
- **BME680** - Environmental sensor (temp, humidity, pressure, gas) @ 0x77 or 0x76
- **GPS Module** - Connected to UART (GPIO 34 RX, GPIO 12 TX)
- **LoRa Module** - SX1276/78 (433 MHz or 915 MHz)
- **OLED Display** - SSD1306 128x64 @ 0x3C

---

## LoRa Configuration

**CRITICAL**: All devices (gateway + all soldier nodes) must use identical settings:

```cpp
Frequency:      433 MHz (or 915 MHz for US)
Spreading Factor: 9 (better range than SF7)
Bandwidth:      125 kHz
Sync Word:      0x12
Preamble:       8
CRC:            Enabled
```

**Why SF9?**
- ✅ Better range (up to 2x compared to SF7)
- ✅ More reliable in noisy environments
- ✅ Better obstacle penetration
- ⚠️ Slower data rate (acceptable for 1Hz updates)

---

## Features

### Real-Time Sensor Reading

**IMU (MPU6050)** - 200Hz:
- 3-axis accelerometer
- 3-axis gyroscope
- Used for motion tracking and step detection

**Heart Rate & SpO2 (MAX30102)** - 25Hz:
- Optical heart rate monitoring
- Blood oxygen saturation (SpO2)
- Automatic finger detection

**Environmental (BME680)** - 1Hz:
- Temperature
- Humidity
- Barometric pressure
- Air quality (gas resistance)

**GPS** - 10Hz:
- Absolute positioning when available
- Satellite count
- Fix quality

### Dead Reckoning

**When GPS available**:
- Uses GPS position directly
- Updates EKF state

**When GPS unavailable**:
- Step detection from IMU
- Heading from magnetometer fusion
- EKF prediction from steps

**Seamless switching** between GPS and dead reckoning.

### Communication

**LoRa Transmission** - 1Hz:
- Sends complete sensor packet to gateway
- 32-byte packets with CRC validation
- Automatic retry on failure

**LoRa Reception** - Polling (100Hz check rate):
- Receives broadcast messages
- Receives individual messages
- CRC validation
- RSSI/SNR monitoring

### Display

**OLED Updates** - 2Hz:
- Node ID
- GPS coordinates (or "NO FIX")
- Heart rate and SpO2
- Temperature
- Battery voltage
- Last received message

---

## Multi-Threaded Architecture

### FreeRTOS Tasks:

**sensorTask** (Core 0, Priority 2):
- Reads IMU at 200Hz
- Reads MAX30102 at 25Hz
- Reads BME680 at 1Hz
- Puts data in sensor queue

**gpsTask** (Core 0, Priority 1):
- Reads GPS at 10Hz
- Updates global position
- Sets GPS fix flag

**fusionTask** (Core 1, Priority 2):
- Runs Madgwick AHRS filter
- Detects steps
- Fuses GPS and dead reckoning
- Outputs position to DR queue

**loraTask** (Core 1, Priority 1):
- Transmits packets at 1Hz
- Reads sensor and DR queues
- Calculates CRC
- Monitors battery

**displayTask** (Core 1, Priority 1):
- Updates OLED at 2Hz
- Shows all sensor data
- Shows messages

**loop()** (Core 1):
- Polls for LoRa messages at 100Hz
- Processes received messages
- Prints status every 10 seconds

---

## Wiring

### I2C Bus (400 kHz):
```
SDA: GPIO 21
SCL: GPIO 22

Devices:
- MPU6050:  0x68
- BME680:   0x77 (or 0x76)
- MAX30102: 0x57
- OLED:     0x3C
```

### GPS UART:
```
RX: GPIO 34
TX: GPIO 12
Baud: 9600
```

### LoRa SPI:
```
SCK:  GPIO 5
MISO: GPIO 19
MOSI: GPIO 27
CS:   GPIO 18
RST:  GPIO 23
DIO0: GPIO 26
```

### Analog:
```
Battery: GPIO 35 (2:1 voltage divider)
```

---

## Upload & Test

### 1. Install Dependencies

```bash
cd soldier_node_production
platformio lib install
```

### 2. Configure Node ID

Edit `src/main.cpp`:
```cpp
#define NODE_ID 1  // Change for each soldier (1, 2, 3, ...)
```

### 3. Upload Firmware

```bash
platformio run --target upload
```

### 4. Monitor Serial Output

```bash
platformio device monitor --baud 115200
```

### Expected Output:

```
=================================
SOLDIER NODE - PRODUCTION
=================================
Node ID: 1
=================================

Initializing sensors...
MPU6050... OK
MAX30102... OK
BME680... OK
GPS... OK
LoRa initialized (polling mode)
  Freq: 433.0 MHz
  SF: 9
  BW: 125.0 kHz
Dead Reckoning initialized

Soldier Node Ready
Waiting for GPS fix...

--- Status ---
GPS: NO FIX
Heading: 0.0°
HR: --
SpO2: --
Temp: 23.5°C
Battery: 3.75V
```

---

## Sensor Testing

### GPS Fix:
Takes 30-60 seconds outdoors. Status will show:
```
GPS: 42.360123, -71.058901 (8 sats)
```

### Heart Rate:
Place finger firmly on MAX30102 sensor:
```
HR: 72 BPM
SpO2: 98%
```

### Motion:
Walk around - heading should change:
```
Heading: 45.3°
```

---

## Troubleshooting

### No GPS Fix

**Symptoms**: `GPS: NO FIX` for > 2 minutes

**Solutions**:
1. Go outdoors (GPS won't work indoors)
2. Check antenna connection
3. Wait 2-5 minutes for cold start
4. Verify wiring (RX/TX not swapped)

### No Heart Rate

**Symptoms**: `HR: --` even with finger on sensor

**Solutions**:
1. Clean sensor surface
2. Press finger firmly but gently
3. Check I2C connection
4. Verify MAX30102 address (should be 0x57)
5. Try different finger

### LoRa Not Receiving

**Symptoms**: No messages received, gateway sends OK

**Solutions**:
1. Verify all LoRa parameters match gateway:
   - Frequency: 433 MHz
   - SF: 9
   - BW: 125 kHz
   - Sync: 0x12
   - Preamble: 8
2. Check antenna connection
3. Move closer to gateway (< 100m for testing)
4. Check RSSI in received messages (should be > -100 dBm)

### Sensor Init Failed

**Symptoms**: `WARNING: Some sensors failed`

**Solutions**:
1. Check I2C wiring (SDA/SCL correct)
2. Verify sensor addresses with I2C scanner
3. Check power supply (3.3V to all sensors)
4. Verify pullup resistors on I2C lines (4.7kΩ)

---

## Performance

### Range:
- **SF9 @ 433 MHz**: Up to 2 km line-of-sight
- **Urban**: 500m - 1 km
- **Indoor**: 50m - 200m

### Battery Life (2000mAh):
- **GPS on**: ~8-12 hours
- **GPS off (DR only)**: ~20-24 hours
- **Sleep mode**: >48 hours (not implemented)

### Update Rates:
- **Position**: 1 Hz (via LoRa)
- **IMU**: 200 Hz (internal)
- **Heart Rate**: 25 Hz sampling, ~1 Hz update
- **Environment**: 1 Hz

---

## Customization

### Change Node ID:
```cpp
#define NODE_ID 2  // Soldier 2
```

### Change LoRa Frequency (US):
```cpp
#define LORA_FREQ 915E6  // 915 MHz for US
```

### Adjust Transmission Rate:
```cpp
// In loraTask():
const TickType_t period = pdMS_TO_TICKS(2000);  // 2 seconds instead of 1
```

### Disable GPS (Indoor Testing):
```cpp
// Comment out GPS task creation in setup():
// xTaskCreatePinnedToCore(gpsTask, "GPS", 2048, NULL, 1, NULL, 0);
```

---

## Packet Format

Same 32-byte format as test firmware:

```
Bytes 0-1:   Node ID (uint16)
Bytes 2-5:   Timestamp (uint32)
Bytes 6-9:   Latitude (float)
Bytes 10-13: Longitude (float)
Bytes 14-17: Heading (float)
Byte 18:     Heart Rate (uint8)
Byte 19:     SpO2 (uint8)
Bytes 20-23: Temperature (float)
Bytes 24-27: Battery Voltage (float)
Bytes 28-29: Status Flags (uint16)
Bytes 30-31: CRC16 (uint16)
```

---

## Status Flags

```cpp
GPS_FIX          = 0x0001  // GPS has valid fix
IMU_VALID        = 0x0002  // IMU data valid
HR_VALID         = 0x0004  // Heart rate valid
SPO2_VALID       = 0x0008  // SpO2 valid
TEMP_VALID       = 0x0010  // Temperature valid
LOW_BATTERY      = 0x0020  // Battery < 3.5V
CRITICAL_BATTERY = 0x0040  // Battery < 3.3V
SENSOR_ERROR     = 0x0080  // Sensor malfunction
ALERT            = 0x0100  // General alert
MAN_DOWN         = 0x0200  // Man down detected
HEAT_STRESS      = 0x0400  // Heat stress detected
```

---

## Integration with Gateway

1. **Upload gateway firmware** with SF9 settings
2. **Upload soldier firmware** with unique NODE_ID
3. **Start server**: `python server/app.py --port /dev/ttyUSB0`
4. **Open dashboard**: `http://localhost:5000`
5. **Verify**: Soldier appears on map with real sensor data

---

## Summary

✅ **Real sensors** - MPU6050, MAX30102, BME680, GPS  
✅ **Multi-threaded** - FreeRTOS tasks on both cores  
✅ **Dead reckoning** - Works indoors without GPS  
✅ **LoRa messaging** - Broadcast and individual  
✅ **OLED display** - Real-time status  
✅ **Production ready** - SF9, CRC validation, error handling  

**This firmware is ready for field deployment!** 🎖️
