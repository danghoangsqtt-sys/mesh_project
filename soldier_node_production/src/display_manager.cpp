#include "display_manager.h"
#include "gps_manager.h"
#include "lora_manager.h"
#include <Wire.h>
#include <Adafruit_SSD1306.h>

static void drawHeader(Adafruit_SSD1306& d) {
    d.fillRect(0, 0, 128, 10, SSD1306_WHITE);
    d.setTextColor(SSD1306_BLACK);
    d.setTextSize(1);
    d.setCursor(2, 1);
    d.print(F("LINH "));
    d.print(cfg::NODE_ID);
    d.setCursor(88, 1);
    d.print(F("MESH"));
    d.setTextColor(SSD1306_WHITE);
}

static void printValueOrDash(Adafruit_SSD1306& d, bool valid, float value, uint8_t digits) {
    if (valid) d.print(value, digits);
    else d.print(F("--"));
}

bool DisplayManager::begin() {
    _disp = new Adafruit_SSD1306(cfg::SCREEN_W, cfg::SCREEN_H, &Wire, cfg::OLED_RESET_PIN);
    return _disp->begin(SSD1306_SWITCHCAPVCC, cfg::OLED_ADDR);
}

Adafruit_SSD1306& DisplayManager::raw() {
    return *_disp;
}

void DisplayManager::showSplash() {
    _disp->clearDisplay();
    drawHeader(*_disp);
    _disp->setTextColor(SSD1306_WHITE);
    _disp->setTextSize(2);
    _disp->setCursor(12, 18);
    _disp->println(F("NODE"));
    _disp->setTextSize(1);
    _disp->setCursor(12, 42);
    _disp->print(F("Khoi dong cam bien..."));
    _disp->display();
}

void DisplayManager::update(const SensorSnapshot& snap, const NavData& nav,
                            const GPSManager& gps, const LoRaManager& lora,
                            uint16_t flags) {
    auto& d = *_disp;
    d.clearDisplay();
    drawHeader(d);

    const String& msg = lora.lastMessage();
    uint32_t msgAge = millis() - lora.lastMessageTime();

    // If message received within last 20 seconds, show prominent notification
    if (msg.length() > 0 && msgAge < 20000) {
        d.setTextSize(1);
        d.setTextColor(SSD1306_WHITE);

        // "TIN NHẮN" header
        d.setCursor(2, 16);
        d.println(F("--- TIN NHAN ---"));

        // Display full message text, wrapped
        d.setCursor(2, 28);
        uint16_t lineWidth = 20;  // ~20 chars per line with textSize(1)
        String displayMsg = msg;
        if (displayMsg.length() > 60) displayMsg = msg.substring(0, 57) + "...";

        // Manual word wrap
        uint8_t y = 28;
        uint16_t idx = 0;
        while (idx < displayMsg.length() && y < 52) {
            uint16_t endIdx = idx + lineWidth;
            if (endIdx > displayMsg.length()) {
                endIdx = displayMsg.length();
            } else {
                // Find space before lineWidth to wrap nicely
                uint16_t spaceIdx = displayMsg.lastIndexOf(' ', endIdx);
                if (spaceIdx > idx) endIdx = spaceIdx;
            }

            String line = displayMsg.substring(idx, endIdx);
            d.setCursor(2, y);
            d.println(line);
            y += 9;
            idx = endIdx + 1;  // skip space
        }

        // Show timeout countdown at bottom
        uint16_t remaining = (20000 - msgAge) / 1000;
        d.setTextSize(1);
        d.setCursor(2, 56);
        d.print(F("Het trong "));
        d.print(remaining);
        d.println(F("s"));

        d.display();
        return;  // Skip normal display
    }

    // ===== NORMAL DISPLAY (when no recent message) =====

    d.drawRoundRect(0, 13, 62, 25, 3, SSD1306_WHITE);
    d.setTextSize(1);
    d.setCursor(4, 16);
    d.print(F("Nhip tim"));
    if (flags & HR_VALID) {
        d.setTextSize(2);
        d.setCursor(8, 26);
        d.print(snap.health.heartRate);
        d.setTextSize(1);
        d.setCursor(44, 30);
        d.print(F("bpm"));
    } else if (snap.health.fingerPresent) {
        d.setCursor(8, 28);
        d.print(F("IR "));
        d.print(snap.health.ir / 1000);
        d.print(F("k"));
    } else {
        d.setCursor(8, 28);
        d.print(F("Cho tay"));
    }

    d.drawRoundRect(66, 13, 62, 25, 3, SSD1306_WHITE);
    d.setTextSize(1);
    d.setCursor(70, 16);
    d.print(F("Oxy mau"));
    if (flags & SPO2_VALID) {
        d.setTextSize(2);
        d.setCursor(74, 26);
        d.print(snap.health.spo2);
        d.setTextSize(1);
        d.setCursor(108, 30);
        d.print(F("%"));
    } else {
        d.setCursor(78, 28);
        d.print(F("--"));
    }

    d.drawLine(0, 41, 127, 41, SSD1306_WHITE);

    d.setTextSize(1);
    d.setCursor(0, 44);
    d.print(F("MT "));
    printValueOrDash(d, flags & TEMP_VALID, snap.env.temperature, 1);
    d.print(F("C "));
    printValueOrDash(d, flags & HUMIDITY_VALID, snap.env.humidity, 0);
    d.print(F("% "));
    printValueOrDash(d, flags & PRESSURE_VALID, snap.env.pressure, 0);
    d.print(F("hPa"));

    d.setCursor(0, 55);
    d.print(F("GPS:"));
    d.print(gps.fixed() ? F("OK") : F("--"));

    d.setCursor(44, 55);
    d.print(F("Pin:"));
    if (lora.batteryPresent()) {
        d.print(lora.packet().batteryVoltage, 2);
        d.print(F("V"));
    } else if (lora.usbPowered()) {
        d.print(F("USB"));
    } else {
        d.print(F("--"));
    }

    if (flags & SENSOR_ERROR) {
        d.setCursor(88, 55);
        d.print(F("CB loi"));
    }

    d.display();
}
