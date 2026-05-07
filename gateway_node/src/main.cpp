#include <Arduino.h>
#include <Wire.h>
#include <SPI.h>
#include <LoRa.h>
#include <TinyGPS++.h>
#include <Adafruit_SSD1306.h>
#define XPOWERS_CHIP_AXP2101
#include <XPowersLib.h>
#include "packet.h"

#define LORA_SCK 5
#define LORA_MISO 19
#define LORA_MOSI 27
#define LORA_CS 18
#define LORA_RST 23
#define LORA_DIO0 26
#define LORA_FREQ 433e6
#define LORA_SYNC_WORD 0x12
#define LORA_SPREADING_FACTOR 9

#define GPS_RX 34
#define GPS_TX 12
#define GPS_BAUD 9600

#define OLED_SDA 21
#define OLED_SCL 22
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1

#define SERIAL_BAUD 115200

#define PKT_START 0xAA
#define PKT_END 0x55

#define NODE_TIMEOUT_MS 30000

TinyGPSPlus gps;
HardwareSerial GPS_Serial(1);
XPowersPMU pmu;
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

volatile bool loraRxFlag = false;
uint8_t serialBuffer[PACKET_SIZE + 4];
uint8_t serialBufferPos = 0;

struct NodeInfo {
    uint16_t nodeId;
    uint32_t lastSeen;
};

NodeInfo activeNodes[100];
uint8_t activeNodeCount = 0;
uint32_t totalMessagesReceived = 0;
uint32_t lastDisplayUpdate = 0;

double currentLat = 0.0;
double currentLon = 0.0;
bool gpsValid = false;

int lastRssi = 0;
float lastSnr = 0.0f;

// Activity log — rolling buffer of last 3 events
#define LOG_MAX_LEN 22
#define LOG_BUF_SIZE 3
char logBuf[LOG_BUF_SIZE][LOG_MAX_LEN];
uint8_t logHead = 0;
uint8_t logSize = 0;

void addLog(const char* msg) {
    strncpy(logBuf[logHead], msg, LOG_MAX_LEN - 1);
    logBuf[logHead][LOG_MAX_LEN - 1] = '\0';
    logHead = (logHead + 1) % LOG_BUF_SIZE;
    if (logSize < LOG_BUF_SIZE) logSize++;
}

// ─────────────────────────────────────────────
//  Splash screen — Vietnamese military style
//  Layout (128x64):
//    y= 0-8 : Inverted header bar
//    y=12-27: "TRAM THU"  textSize(2) centered
//    y=30-45: "THAP LORA" textSize(2) centered
//    y=48   : Separator line
//    y=52-59: Status text
// ─────────────────────────────────────────────
void showSplashScreen() {
    display.clearDisplay();

    // Inverted header: "GATEWAY LORA MESH" = 17 chars × 6px = 102px → x=13
    display.fillRect(0, 0, SCREEN_WIDTH, 9, SSD1306_WHITE);
    display.setTextColor(SSD1306_BLACK);
    display.setTextSize(1);
    display.setCursor(13, 1);
    display.print(F("GATEWAY LORA MESH"));

    display.setTextColor(SSD1306_WHITE);

    // "GATEWAY" textSize(2): 7 chars × 12px = 84px → center x=22
    display.setTextSize(2);
    display.setCursor(22, 12);
    display.print(F("GATEWAY"));

    // "LORA MESH" textSize(2): 9 chars × 12px = 108px → center x=10
    display.setCursor(10, 30);
    display.print(F("LORA MESH"));

    // Separator
    display.drawFastHLine(8, 48, 112, SSD1306_WHITE);

    // Status line: ">> KHOI DONG... <<" = 18 chars × 6px = 108px → x=10
    display.setTextSize(1);
    display.setCursor(10, 52);
    display.print(F(">> KHOI DONG... <<"));

    display.display();
    delay(2000);
}

void updateNodeList(uint16_t nodeId) {
    uint32_t currentTime = millis();

    // Prune timed-out nodes
    uint8_t newCount = 0;
    for (uint8_t i = 0; i < activeNodeCount; i++) {
        if (currentTime - activeNodes[i].lastSeen < NODE_TIMEOUT_MS) {
            activeNodes[newCount] = activeNodes[i];
            newCount++;
        }
    }
    activeNodeCount = newCount;

    // Update existing node
    for (uint8_t i = 0; i < activeNodeCount; i++) {
        if (activeNodes[i].nodeId == nodeId) {
            activeNodes[i].lastSeen = currentTime;
            return;
        }
    }

    // Add new node
    if (activeNodeCount < 100) {
        activeNodes[activeNodeCount].nodeId = nodeId;
        activeNodes[activeNodeCount].lastSeen = currentTime;
        activeNodeCount++;
    }
}

uint8_t getActiveNodeCount() {
    uint32_t currentTime = millis();
    uint8_t count = 0;
    for (uint8_t i = 0; i < activeNodeCount; i++) {
        if (currentTime - activeNodes[i].lastSeen < NODE_TIMEOUT_MS) {
            count++;
        }
    }
    return count;
}

// ─────────────────────────────────────────────
//  Main display — Vietnamese military style
//  Layout (128x64):
//    y= 0- 7: [INV] "TRAM CONG  [●/○]  XXdBm"
//    y= 9   : separator line
//    y=11-18: "NUT:XX    GOI:XXXXX"
//    y=20   : separator line
//    y=22-29: GPS La: or "TIM GPS... SAT:XX"
//    y=31-38: GPS Lo: (khi có fix)
//    y=40   : separator line
//    y=42-49: log line 1 (mới nhất)
//    y=51-58: log line 2
//    y=59-63: log line 3 (cắt bớt - 5px)
// ─────────────────────────────────────────────
void updateDisplay() {
    uint8_t ac = getActiveNodeCount();

    display.clearDisplay();

    // ── Dòng 0: Header bar (inverted) ──────────────────
    display.fillRect(0, 0, SCREEN_WIDTH, 9, SSD1306_WHITE);
    display.setTextColor(SSD1306_BLACK);
    display.setTextSize(1);
    display.setCursor(2, 1);
    display.print(F("GATEWAY"));

    // GPS indicator: filled = fix, outline = searching
    if (gpsValid) {
        display.fillCircle(68, 4, 3, SSD1306_BLACK);
    } else {
        display.drawCircle(68, 4, 3, SSD1306_BLACK);
    }

    // RSSI ở phải: "XXXdB" hoặc "-- dB"
    display.setCursor(75, 1);
    if (totalMessagesReceived > 0) {
        display.print(lastRssi);
        display.print(F("dB"));
    } else {
        display.print(F("-- dB"));
    }

    display.setTextColor(SSD1306_WHITE);

    // ── Dòng 1: Separator ──────────────────────────────
    display.drawFastHLine(0, 9, SCREEN_WIDTH, SSD1306_WHITE);

    // ── Dòng 2: Số nút + tổng gói (y=11) ──────────────
    // "NUT:" = node count, "GOI:" = total packets
    display.setCursor(0, 11);
    display.print(F("NUT:"));
    if (ac < 10) display.print('0');
    display.print(ac);

    display.setCursor(66, 11);
    display.print(F("GOI:"));
    if (totalMessagesReceived > 99999UL) {
        display.print(F("99999+"));
    } else {
        display.print(totalMessagesReceived);
    }

    // ── Dòng 3: Separator ──────────────────────────────
    display.drawFastHLine(0, 20, SCREEN_WIDTH, SSD1306_WHITE);

    // ── Dòng 4-5: Tọa độ GPS (y=22, y=31) ─────────────
    display.setCursor(0, 22);
    if (gpsValid) {
        display.print(F("La:"));
        display.print(currentLat, 5);
        display.setCursor(0, 31);
        display.print(F("Lo:"));
        display.print(currentLon, 5);
    } else {
        display.print(F("TIM GPS... SAT:"));
        if (gps.satellites.isValid()) {
            display.print((uint8_t)gps.satellites.value());
        } else {
            display.print('0');
        }
        display.setCursor(0, 31);
        display.print(F("CHUA CO VI TRI"));
    }

    // ── Separator ──────────────────────────────────────
    display.drawFastHLine(0, 40, SCREEN_WIDTH, SSD1306_WHITE);

    // ── Dòng 6-8: Activity log (y=42, 51, 59) ──────────
    if (logSize == 0) {
        display.setCursor(0, 42);
        display.print(F("CHO TIN HIEU..."));
    } else {
        for (uint8_t i = 0; i < logSize && i < 3; i++) {
            // Đọc từ mới nhất đến cũ hơn
            uint8_t idx = (logHead + LOG_BUF_SIZE - 1 - i) % LOG_BUF_SIZE;
            uint8_t y = 42 + i * 9;
            display.setCursor(0, y);
            display.print(logBuf[idx]);
        }
    }

    display.display();
}

void IRAM_ATTR onLoRaReceive() {
    loraRxFlag = true;
}

void sendPacketToSerial(const SoldierPacket *pkt) {
    Serial.write(PKT_START);
    Serial.write((uint8_t*)pkt, PACKET_SIZE);
    Serial.write(PKT_END);
}

void processSerialCommand() {
    if (Serial.available() > 0) {
        uint8_t start = Serial.peek();
        if (start == PKT_START) {
            if (Serial.available() >= PACKET_SIZE + 2) {
                Serial.read();
                SoldierPacket txPkt;
                Serial.readBytes((uint8_t*)&txPkt, PACKET_SIZE);
                uint8_t end = Serial.read();

                if (end == PKT_END && packetValidateCRC(&txPkt)) {
                    LoRa.beginPacket();
                    LoRa.write((uint8_t*)&txPkt, PACKET_SIZE);
                    LoRa.endPacket();
                    delay(50);
                    LoRa.receive();
                    Serial.println(F("TX: Packet sent via LoRa"));
                    addLog(">TX GOI -> LORA OK");
                }
            }
        } else if (start == 0x5A) {
            if (Serial.available() >= 32) {
                uint8_t cmdPkt[32];
                Serial.readBytes(cmdPkt, 32);
                if (cmdPkt[1] == 0xA5) {
                    LoRa.beginPacket();
                    LoRa.write(cmdPkt, 32);
                    LoRa.endPacket();
                    delay(50);
                    LoRa.receive();
                    Serial.println(F("TX: Command sent via LoRa"));
                    addLog(">TX LENH -> LORA OK");
                }
            }
        } else {
            Serial.read();
        }
    }
}

void setup() {
    Serial.begin(SERIAL_BAUD);

    Wire.begin(OLED_SDA, OLED_SCL);

    if (pmu.begin(Wire, AXP2101_SLAVE_ADDRESS, OLED_SDA, OLED_SCL)) {
        Serial.println(F("AXP2101 initialized"));

        pmu.setALDO3Voltage(3300);
        pmu.enableALDO3();
        pmu.setALDO2Voltage(3300);
        pmu.enableALDO2();
        pmu.setDC1Voltage(3300);
        pmu.enableDC1();

        Serial.println(F("PMU: GPS power enabled on ALDO3"));
        delay(500);
    } else {
        Serial.println(F("AXP2101 init failed - GPS may not work"));
    }

    if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
        Serial.println(F("SSD1306 allocation failed"));
    } else {
        showSplashScreen();
    }

    SPI.begin(LORA_SCK, LORA_MISO, LORA_MOSI, LORA_CS);
    LoRa.setPins(LORA_CS, LORA_RST, LORA_DIO0);

    if (!LoRa.begin(LORA_FREQ)) {
        Serial.println(F("LoRa init failed"));
        while (1);
    }

    Serial.println(F("LoRa initialized"));

	LoRa.setSpreadingFactor(LORA_SPREADING_FACTOR);
    LoRa.setSignalBandwidth(125E3);
    LoRa.setSyncWord(LORA_SYNC_WORD);
    LoRa.enableCrc();
    LoRa.setPreambleLength(8);
    LoRa.receive(); // Vô cùng quan trọng: Đặt chip vào chế độ lắng nghe liên tục!

    GPS_Serial.begin(GPS_BAUD, SERIAL_8N1, GPS_RX, GPS_TX);

    Serial.println(F("Gateway Ready"));
    addLog(">KHOI DONG THANH CONG");
    updateDisplay();
}

void loop() {
    // Process GPS data
    while (GPS_Serial.available() > 0) {
        char c = GPS_Serial.read();
        if (gps.encode(c)) {
            if (gps.location.isValid() && !gpsValid) {
                gpsValid = true;
                addLog(">GPS: DA CO VI TRI");
            }
            if (gps.location.isValid()) {
                currentLat = gps.location.lat();
                currentLon = gps.location.lng();
            }
        }
    }

    // Polling mode LoRa receive
    int packetSize = LoRa.parsePacket();

    if (packetSize > 0) {
        Serial.print(F("\n>>> PACKET DETECTED! Size: "));
        Serial.print(packetSize);
        Serial.print(F(" bytes (expecting "));
        Serial.print(PACKET_SIZE);
        Serial.println(F(")"));

        Serial.print(F("RSSI: "));
        Serial.print(LoRa.packetRssi());
        Serial.print(F(" dBm | SNR: "));
        Serial.println(LoRa.packetSnr());

        if (packetSize == PACKET_SIZE) {
            Serial.println(F("Size OK - Reading packet..."));

            SoldierPacket rxPkt;
            LoRa.readBytes((uint8_t*)&rxPkt, PACKET_SIZE);

            Serial.print(F("Node ID: "));
            Serial.print(rxPkt.nodeId);
            Serial.print(F(" | CRC: "));

            if (packetValidateCRC(&rxPkt)) {
                Serial.println(F("VALID"));

                lastRssi = LoRa.packetRssi();
                lastSnr  = LoRa.packetSnr();
                totalMessagesReceived++;
                updateNodeList(rxPkt.nodeId);

                // Log event: ">RX ID:3 -87dB" = 14 chars, fits LOG_MAX_LEN=22
                char logMsg[LOG_MAX_LEN];
                snprintf(logMsg, LOG_MAX_LEN, ">RX NUT:%d %ddB", rxPkt.nodeId, lastRssi);
                addLog(logMsg);

                if (gpsValid) {
                    Serial.print(F("GW_GPS:"));
                    Serial.print(currentLat, 6);
                    Serial.print(F(","));
                    Serial.println(currentLon, 6);
                }

                sendPacketToSerial(&rxPkt);

                Serial.print(F("DEBUG: RX Node "));
                Serial.print(rxPkt.nodeId);
                Serial.print(F(" | Total: "));
                Serial.print(totalMessagesReceived);
                Serial.print(F(" | Active: "));
                Serial.println(getActiveNodeCount());
            } else {
                Serial.println(F("INVALID - CRC MISMATCH"));
                addLog(">SAI CRC - TU CHOI");
            }
        } else if (packetSize == 8) {
            uint8_t ackBuf[8];
            LoRa.readBytes(ackBuf, 8);
            if (ackBuf[0] == 0x5A && ackBuf[1] == 0xA6) {
                Serial.write(ackBuf, 8);
                Serial.println(F("\nFWD: ACK relayed to USB"));
                addLog(">ACK -> USB OK");
            }
        } else {
            Serial.println(F("*** SIZE MISMATCH - PACKET REJECTED ***"));
            addLog(">SAI KICH THUOC");
            while (LoRa.available()) {
                LoRa.read();
            }
        }
        Serial.println();
    }

    processSerialCommand();

    if (millis() - lastDisplayUpdate >= 1000) {
        lastDisplayUpdate = millis();
        updateDisplay();
    }

    static uint32_t lastHeartbeat = 0;
    if (millis() - lastHeartbeat >= 5000) {
        lastHeartbeat = millis();
        Serial.print(F("."));
        if (totalMessagesReceived > 0) {
            Serial.print(F(" ["));
            Serial.print(totalMessagesReceived);
            Serial.print(F(" msgs]"));
        }
        Serial.print(F(" GPS: chars="));
        Serial.print(gps.charsProcessed());
        Serial.print(F(" sats="));
        Serial.print(gps.satellites.value());
        Serial.print(F(" fix="));
        Serial.print(gps.location.isValid() ? "Y" : "N");
        if (gps.charsProcessed() < 10) {
            Serial.print(F(" ** NO GPS DATA - check wiring/power **"));
        }
        Serial.println();
        Serial.flush();
    }
}
