#pragma once

#include <Arduino.h>
#include <Adafruit_SSD1306.h>
#include "config.h"
#include "sensor.h"
#include "packet.h"

class GPSManager;
class LoRaManager;

class DisplayManager {
public:
    bool begin();
    void showSplash();
    void update(const SensorSnapshot& snap, const NavData& nav,
                const GPSManager& gps, const LoRaManager& lora,
                uint16_t flags);

    // Expose display for GPS manager BT selection UI
    Adafruit_SSD1306& raw();

private:
    // Allocated dynamically to keep the heavy header out of this header
    Adafruit_SSD1306* _disp = nullptr;
};