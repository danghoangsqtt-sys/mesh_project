#include <Arduino.h>
#include <Wire.h>

#if defined(BOARD_TBEAM)
#include <XPowersLib.h>
#endif

#include "config.h"
#include "sensor.h"
#include "sensor_hub.h"
#include "mpu6050_sensor.h"
#include "max30102_sensor.h"
#include "bme280_sensor.h"
#include "gps_manager.h"
#include "navigation.h"
#include "lora_manager.h"
#include "display_manager.h"
#include "packet.h"
#include <Preferences.h>

namespace cfg {
    uint8_t RADIO_SF = 9;
}

// ===== Concrete sensors (swap these lines to change hardware) =====
static MPU6050Sensor  imuSensor;
static MAX30102Sensor healthSensor;
static BME280Sensor   envSensor;

// ===== Managers =====
static SensorHub      sensors;
static GPSManager     gps;
static Navigation     nav;
static LoRaManager    lora;
static DisplayManager oled;

#if defined(BOARD_TBEAM)
XPowersAXP2101 pmu;
#endif

// ===== Shared state =====
static QueueHandle_t  sensorQ;
static QueueHandle_t  navQ;
static uint16_t       statusFlags = 0;
static bool           sensorsOk = false;

static void scanI2CBus() {
    Serial.printf("I2C scan on SDA=%u SCL=%u @ %lu Hz\n",
                  cfg::SDA_PIN, cfg::SCL_PIN, cfg::I2C_FREQ);
    uint8_t found = 0;
    for (uint8_t addr = 1; addr < 127; addr++) {
        Wire.beginTransmission(addr);
        if (Wire.endTransmission() == 0) {
            Serial.printf("  - device at 0x%02X", addr);
            if (addr == cfg::OLED_ADDR) Serial.print(F(" OLED"));
            if (addr == cfg::MAX3010X_ADDR) Serial.print(F(" MAX3010x"));
            if (addr == cfg::BME280_ADDR_PRIMARY || addr == cfg::BME280_ADDR_ALT) Serial.print(F(" BME280"));
            if (addr == cfg::MPU6050_ADDR) Serial.print(F(" MPU6050"));
            Serial.println();
            found++;
        }
        delay(2);
    }
    if (found == 0) Serial.println(F("  - no I2C devices found"));
}

// ===== Status flags from sensor data =====
static uint16_t buildFlags(const SensorSnapshot& s) {
    uint16_t f = 0;
    if (s.imu.ax != 0 || s.imu.ay != 0 || s.imu.az != 0) f |= IMU_VALID;
    if (s.health.hrValid)    f |= HR_VALID;
    if (s.health.spo2Valid)  f |= SPO2_VALID;
    if (!isnan(s.env.temperature)) f |= TEMP_VALID;
    if (!isnan(s.env.humidity))    f |= HUMIDITY_VALID;
    if (!isnan(s.env.pressure))    f |= PRESSURE_VALID;
    if (gps.fixed())               f |= GPS_FIX;
    if (!sensorsOk)                f |= SENSOR_ERROR;
    return f;
}

// ===== FreeRTOS Tasks =====

static void sensorTask(void*) {
    TickType_t wake = xTaskGetTickCount();
    uint32_t tick = 0;
    SensorSnapshot snap;

    while (true) {
        snap.timestamp = millis();

        if (sensors.imu() && sensors.imu()->ready())
            snap.imu = sensors.imu()->read();

        if (tick % cfg::IMU_PER_HEALTH == 0 && sensors.health() && sensors.health()->ready())
            snap.health = sensors.health()->read();

        if (tick % cfg::IMU_PER_ENV == 0 && sensors.env() && sensors.env()->ready())
            snap.env = sensors.env()->read();

        xQueueOverwrite(sensorQ, &snap);
        tick++;
        vTaskDelayUntil(&wake, pdMS_TO_TICKS(cfg::IMU_PERIOD_MS));
    }
}

static void gpsTask(void*) {
    TickType_t wake = xTaskGetTickCount();
    while (true) {
        gps.update();
        vTaskDelayUntil(&wake, pdMS_TO_TICKS(cfg::GPS_PERIOD_MS));
    }
}

static void fusionTask(void*) {
    TickType_t wake = xTaskGetTickCount();
    SensorSnapshot snap;

    while (true) {
        if (xQueueReceive(sensorQ, &snap, 0) == pdTRUE) {
            nav.update(snap.imu, gps);
            NavData nd = nav.position();
            xQueueOverwrite(navQ, &nd);
        }
        vTaskDelayUntil(&wake, pdMS_TO_TICKS(cfg::FUSION_PERIOD_MS));
    }
}

static void loraTask(void*) {
    TickType_t wake = xTaskGetTickCount();
    SensorSnapshot snap;
    NavData nd;

    while (true) {
        lora.pollReceive();

        xQueuePeek(sensorQ, &snap, 0);
        xQueuePeek(navQ, &nd, 0);
        statusFlags = buildFlags(snap);

        lora.transmit(snap, nd, statusFlags);

        vTaskDelayUntil(&wake, pdMS_TO_TICKS(cfg::LORA_PERIOD_MS));
    }
}

static void displayTask(void*) {
    TickType_t wake = xTaskGetTickCount();
    SensorSnapshot snap;
    NavData nd;

    while (true) {
        xQueuePeek(sensorQ, &snap, 0);
        xQueuePeek(navQ, &nd, 0);
        oled.update(snap, nd, gps, lora, statusFlags);
        vTaskDelayUntil(&wake, pdMS_TO_TICKS(cfg::DISPLAY_PERIOD_MS));
    }
}

// ===== Setup =====

void setup() {
    Serial.begin(cfg::SERIAL_BAUD);
    delay(1000);

    Serial.println(F("================================="));
    Serial.printf("SOLDIER NODE  ID: %d\n", cfg::NODE_ID);
#if defined(BOARD_TBEAM)
    Serial.println(F("Board: TTGO T-Beam"));
#elif defined(BOARD_LORA32)
    Serial.println(F("Board: TTGO LoRa32 V2.1"));
#endif
    Serial.println(F("=================================\n"));

    Preferences prefs;
    prefs.begin("mesh", true); // read-only
    cfg::RADIO_SF = prefs.getUChar("sf", 9);
    prefs.end();
    Serial.printf("Loaded Config -> SF: %d\n", cfg::RADIO_SF);

    Wire.begin(cfg::SDA_PIN, cfg::SCL_PIN);
    Wire.setClock(cfg::I2C_FREQ);

    // ---- PMU init (T-Beam only) ----
#if defined(BOARD_TBEAM)
    Serial.print(F("PMU Init... "));
    if (pmu.begin(Wire, AXP2101_SLAVE_ADDRESS, cfg::SDA_PIN, cfg::SCL_PIN)) {
        
        pmu.setALDO4Voltage(3300);
        pmu.enableALDO4();
        pmu.setALDO2Voltage(3300);
        pmu.enableALDO2();
        pmu.setALDO3Voltage(3300);
        pmu.enableALDO3();
        
        pmu.enableBattDetection();
        pmu.enableBattVoltageMeasure();
        pmu.enableVbusVoltageMeasure();
        Serial.println(F("OK"));
        Serial.printf("Power: battery=%s usb=%s\n",
                      pmu.isBatteryConnect() ? "YES" : "NO",
                      pmu.isVbusIn() ? "YES" : "NO");
    } else {
        Serial.println(F("FAIL"));
    }
#elif defined(BOARD_LORA32)
    Serial.println(F("LoRa32: No PMU — using ADC for battery"));
#endif

    delay(500); // Give sensors time to power up
    scanI2CBus();

    if (!oled.begin()) {
        Serial.println(F("OLED init failed"));
        while (1);
    }
    oled.showSplash();
    delay(2000);

    sensors.setImu(&imuSensor);
    sensors.setHealth(&healthSensor);
    sensors.setEnv(&envSensor);
    sensorsOk = sensors.beginAll(Serial);

    // Configure T-Beam button as touch input (active low)
#if defined(BOARD_TBEAM)
    pinMode(cfg::BUTTON_PIN, INPUT_PULLUP);
#endif

    gps.beginUART(Serial);

    if (!lora.begin(Serial)) while (1);

    nav.begin();

    sensorQ = xQueueCreate(1, sizeof(SensorSnapshot));
    navQ    = xQueueCreate(1, sizeof(NavData));

    xTaskCreatePinnedToCore(sensorTask,  "Sensor",  4096, nullptr, 2, nullptr, 0);
    xTaskCreatePinnedToCore(gpsTask,     "GPS",     3072, nullptr, 1, nullptr, 0);
    xTaskCreatePinnedToCore(fusionTask,  "Fusion",  4096, nullptr, 2, nullptr, 1);
    xTaskCreatePinnedToCore(loraTask,    "LoRa",    8192, nullptr, 1, nullptr, 1);
    xTaskCreatePinnedToCore(displayTask, "Display", 4096, nullptr, 1, nullptr, 1);

    Serial.println(F("\nSoldier Node Ready\n"));
}

// ===== Loop =====

void loop() {
    static uint32_t lastPrint = 0;
    if (millis() - lastPrint < cfg::STATUS_PRINT_MS) { delay(10); return; }
    lastPrint = millis();

    Serial.println(F("--- Status ---"));
    Serial.print(F("GPS: "));
    if (gps.fixed()) {
        Serial.printf("%.6f, %.6f (%d sats)\n", gps.latitude(), gps.longitude(), gps.satellites());
    } else {
        Serial.println(F("NO FIX"));
    }

    const auto& p = lora.packet();
    Serial.printf("Heading: %.1f deg\n", p.heading);

    if (statusFlags & HR_VALID) Serial.printf("HR: %d BPM\n", p.heartRate);
    else Serial.println(F("HR: --"));

    if (statusFlags & SPO2_VALID) Serial.printf("SpO2: %d%%\n", p.spo2);
    else Serial.println(F("SpO2: --"));

    if (statusFlags & TEMP_VALID) Serial.printf("Temp: %.1f C\n", p.temperature);
    else Serial.println(F("Temp: --"));

    if (statusFlags & PRESSURE_VALID) Serial.printf("Pres: %.1f hPa\n", p.pressure);
    else Serial.println(F("Pres: --"));

    if (statusFlags & HUMIDITY_VALID) Serial.printf("Hum: %.1f%%\n", p.humidity);
    else Serial.println(F("Hum: --"));

    if (statusFlags & BATTERY_PRESENT) Serial.printf("Batt: %.2fV\n\n", p.batteryVoltage);
    else Serial.printf("Batt: %s\n\n", (statusFlags & USB_POWER) ? "USB" : "--");
}
