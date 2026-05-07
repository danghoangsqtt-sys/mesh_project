#include "gps_manager.h"
#include <TinyGPSPlus.h>

// UART2 for GPS on both T-Beam (GPIO34/12) and LoRa32 (GPIO4/12).
// Pins are defined per-board in config.h.
static HardwareSerial GPSSerial(2);
static TinyGPSPlus    tinyGps;

bool GPSManager::beginUART(Stream& log) {
    log.println(F("Starting UART GPS..."));
    log.printf("  RX pin: %d (← GPS TX)\n", cfg::GPS_RX_PIN);
    log.printf("  TX pin: %d (→ GPS RX)\n", cfg::GPS_TX_PIN);
    log.printf("  Baud: %lu\n", (unsigned long)cfg::GPS_BAUD);

    GPSSerial.begin(cfg::GPS_BAUD, SERIAL_8N1, cfg::GPS_RX_PIN, cfg::GPS_TX_PIN);
    delay(500);

    log.println(F("UART GPS initialized — waiting for fix..."));
    return true;
}

void GPSManager::update() {
    while (GPSSerial.available() > 0) {
        tinyGps.encode(GPSSerial.read());
    }

    if (tinyGps.location.isUpdated() && tinyGps.location.isValid()) {
        _lat     = tinyGps.location.lat();
        _lon     = tinyGps.location.lng();
        _fixed   = true;
        _lastFix = millis();
    }

    // 10-second tolerance: clears fix only after 10 consecutive seconds without data
    if (_fixed && (millis() - _lastFix > 10000)) {
        _fixed = false;
    }
}

uint8_t GPSManager::satellites() const {
    return tinyGps.satellites.isValid() ? (uint8_t)tinyGps.satellites.value() : 0;
}
