#include "ota_handler.h"
#include <Update.h>

void OtaHandler::begin(uint32_t firmwareSize) {
    if (!Update.begin(firmwareSize)) {
        Serial.printf("OTA Begin Failed: %s\n", Update.errorString());
    } else {
        Serial.println("OTA Started...");
    }
}

void OtaHandler::writeChunk(uint16_t chunkIndex, const uint8_t* data, uint8_t length) {
    if (!Update.isRunning()) return;

    // We assume sequential chunks for this simple implementation
    if (Update.write(const_cast<uint8_t*>(data), length) != length) {
        Serial.printf("OTA Write Failed: %s\n", Update.errorString());
    }
}

void OtaHandler::end() {
    if (Update.end(true)) {
        Serial.println("OTA Success! Rebooting...");
        delay(1000);
        ESP.restart();
    } else {
        Serial.printf("OTA End Failed: %s\n", Update.errorString());
    }
}
