#pragma once

#include <Arduino.h>

namespace cfg {

#ifdef NODE_ID_OVERRIDE
constexpr uint8_t NODE_ID = NODE_ID_OVERRIDE;
#else
constexpr uint8_t NODE_ID = 3;
#endif

// I2C - same on both boards. 100 kHz is more reliable with external modules.
constexpr uint8_t  SDA_PIN      = 21;
constexpr uint8_t  SCL_PIN      = 22;
constexpr uint32_t I2C_FREQ     = 100000;

// OLED - same on both boards
constexpr int8_t   OLED_RESET_PIN = -1;
constexpr uint8_t  OLED_ADDR    = 0x3C;
constexpr uint16_t SCREEN_W     = 128;
constexpr uint16_t SCREEN_H     = 64;

// LoRa radio parameters (pin mapping from board variant pins_arduino.h)
constexpr long     RADIO_FREQ       = 433E6;
extern uint8_t     RADIO_SF;
constexpr long     RADIO_BW         = 125E3;
constexpr uint8_t  RADIO_SYNC_WORD  = 0x12;
constexpr uint8_t  RADIO_PREAMBLE   = 8;

// I2C sensor addresses - same on both boards
constexpr uint8_t  MPU6050_ADDR     = 0x68;
constexpr uint8_t  BME280_ADDR_PRIMARY = 0x76;
constexpr uint8_t  BME280_ADDR_ALT     = 0x77;
constexpr uint8_t  MAX3010X_ADDR       = 0x57;
constexpr uint32_t MAX3010X_FINGER_THRESHOLD = 2500;

// Battery
constexpr uint8_t  VBAT_PIN         = 35;
constexpr float    BATT_LOW         = 3.5f;
constexpr float    BATT_CRITICAL    = 3.3f;

// Serial
constexpr uint32_t SERIAL_BAUD      = 115200;

// ================================================================
// Board-specific configuration
// ================================================================

#if defined(BOARD_LORA32)
    // --- LoRa32 V2.1 ---
    // GPS via UART (HardwareSerial2)
    constexpr uint8_t  GPS_RX_PIN    = 4;     // LoRa32 receives ← GPS TX
    constexpr uint8_t  GPS_TX_PIN    = 12;    // LoRa32 sends    → GPS RX
    constexpr uint32_t GPS_BAUD      = 9600;
    // Button — LoRa32 has no dedicated button; use GPIO0 (BOOT) if needed
    constexpr uint8_t  BUTTON_PIN    = 0;
    // Battery reading via ADC (no PMU)
    // ADC on GPIO35 with voltage divider: Vbat → 100K → GPIO35 → 100K → GND
    constexpr float    VBAT_DIVIDER  = 2.0f;  // voltage divider ratio
    constexpr float    ADC_REF       = 3.3f;
    constexpr float    ADC_RESOLUTION = 4095.0f;

#elif defined(BOARD_TBEAM)
    // --- T-Beam ---
    // Built-in GPS module (NEO-6M) via UART2
    constexpr uint8_t  GPS_RX_PIN    = 34;    // T-Beam receives ← GPS TX (GPIO34, input-only)
    constexpr uint8_t  GPS_TX_PIN    = 12;    // T-Beam sends    → GPS RX
    constexpr uint32_t GPS_BAUD      = 9600;
    // Button on IO38 (T-Beam built-in)
    constexpr uint8_t  BUTTON_PIN    = 38;

#else
    #error "Define BOARD_TBEAM or BOARD_LORA32 in platformio.ini build_flags"
#endif

// Task rates
constexpr uint32_t IMU_PERIOD_MS    = 5;
constexpr uint32_t IMU_PER_HEALTH   = 8;
constexpr uint32_t IMU_PER_ENV      = 100;
constexpr uint32_t GPS_PERIOD_MS    = 100;
constexpr uint32_t FUSION_PERIOD_MS = 5;
constexpr uint32_t LORA_PERIOD_MS   = 1000;
constexpr uint32_t DISPLAY_PERIOD_MS = 250;
constexpr uint32_t STATUS_PRINT_MS  = 10000;

// Navigation
constexpr float STEP_THRESHOLD      = 11.5f;
constexpr float STEP_HYSTERESIS     = 1.5f;
constexpr float STEP_MIN_INTERVAL   = 0.3f;

enum class MoveMode : uint8_t { WALK, JOG, RUN };

inline float stepLength(MoveMode m) {
    switch (m) {
        case MoveMode::JOG: return 1.1f;
        case MoveMode::RUN: return 1.4f;
        default:            return 0.75f;
    }
}

} // namespace cfg
