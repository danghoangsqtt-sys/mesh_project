#include "lora_manager.h"
#include <SPI.h>
#include <LoRa.h>
#include <Preferences.h>
#include "ota_handler.h"

#if defined(BOARD_TBEAM)
#include <XPowersLib.h>
extern XPowersAXP2101 pmu;
#endif

bool LoRaManager::begin(Stream& log) {
    SPI.begin(LORA_SCK, LORA_MISO, LORA_MOSI, LORA_CS);
    LoRa.setPins(LORA_CS, LORA_RST, LORA_IRQ);

    if (!LoRa.begin(cfg::RADIO_FREQ)) {
        log.println(F("LoRa init failed"));
        return false;
    }

    LoRa.setSpreadingFactor(cfg::RADIO_SF);
    LoRa.setSignalBandwidth(cfg::RADIO_BW);
    LoRa.setSyncWord(cfg::RADIO_SYNC_WORD);
    LoRa.setPreambleLength(cfg::RADIO_PREAMBLE);
    LoRa.enableCrc();

    log.println(F("LoRa initialized"));
    log.printf("  Freq: %.0f MHz  SF: %d  BW: %.0f kHz\n",
               cfg::RADIO_FREQ / 1E6, cfg::RADIO_SF, cfg::RADIO_BW / 1E3);
    return true;
}

float LoRaManager::readBattery() {
#if defined(BOARD_TBEAM)
    // T-Beam: read from AXP2101 PMU only when a real battery is attached.
    _batteryPresent = pmu.isBatteryConnect();
    _usbPowered = pmu.isVbusIn();
    if (!_batteryPresent) return NAN;
    return pmu.getBattVoltage() / 1000.0f;
#elif defined(BOARD_LORA32)
    // LoRa32: read from ADC on GPIO35 with voltage divider
    _batteryPresent = true;
    _usbPowered = false;
    uint32_t raw = 0;
    for (int i = 0; i < 8; i++) {
        raw += analogRead(cfg::VBAT_PIN);
    }
    raw /= 8;  // average 8 samples for stability
    float voltage = (raw / cfg::ADC_RESOLUTION) * cfg::ADC_REF * cfg::VBAT_DIVIDER;
    return voltage;
#else
    return 0.0f;
#endif
}

void LoRaManager::transmit(const SensorSnapshot& snap, const NavData& nav, uint16_t flags) {
    float batt = readBattery();
    if (_batteryPresent) {
        flags |= BATTERY_PRESENT;
        if (batt < cfg::BATT_CRITICAL)   flags |= CRITICAL_BATTERY;
        else if (batt < cfg::BATT_LOW)   flags |= LOW_BATTERY;
    }
    if (_usbPowered) flags |= USB_POWER;

    _pkt.nodeId         = cfg::NODE_ID;
    _pkt.timestamp      = millis() / 1000;
    _pkt.latitude       = nav.latitude;
    _pkt.longitude      = nav.longitude;
    _pkt.heading        = nav.heading;
    _pkt.heartRate      = snap.health.heartRate;
    _pkt.spo2           = snap.health.spo2;
    _pkt.temperature    = isnan(snap.env.temperature) ? 0.0f : snap.env.temperature;
    _pkt.humidity       = isnan(snap.env.humidity) ? 0.0f : snap.env.humidity;
    _pkt.pressure       = isnan(snap.env.pressure) ? 0.0f : snap.env.pressure;
    _pkt.batteryVoltage = _batteryPresent ? batt : 0.0f;
    _pkt.statusFlags    = flags;
    packetCalculateCRC(&_pkt);

    bool sent = false;
    for (int i = 0; i < 3 && !sent; i++) {
        if (LoRa.beginPacket()) {
            LoRa.write((uint8_t*)&_pkt, PACKET_SIZE);
            LoRa.endPacket(false);
            sent = true;
            delay(100);
        } else {
            delay(random(10, 50));
        }
    }
    
    // Đảm bảo đưa chip trở lại chế độ lắng nghe liên tục sau khi phát xong
    LoRa.receive();

    if(sent) {
        Serial.printf("\n<< LORA_TX: Heading: %.1f deg  HR: %d BPM  SpO2: %d%%  Temp: %.1f C  Pres: %.1f hPa  Hum: %.1f%%  Batt: ",
                      _pkt.heading, _pkt.heartRate, _pkt.spo2, _pkt.temperature, _pkt.pressure, _pkt.humidity);
        if (_batteryPresent) Serial.printf("%.2fV", _pkt.batteryVoltage);
        else Serial.print(_usbPowered ? F("USB") : F("--"));
        Serial.printf("  RSSI:%d SNR:%.1f\n",
                      LoRa.packetRssi(), LoRa.packetSnr());
    } else {
        Serial.println(F("\n<< LORA_TX: FAILED TO SEND PACKET"));
    }
}

void LoRaManager::pollReceive() {
    // Process all pending packets in the FIFO
    while (true) {
        int sz = LoRa.parsePacket();
        if (sz == 0) break; // no more packets
        
        if (sz == 32) {
            handleRxPacket();
        } else {
            while (LoRa.available()) LoRa.read(); // discard bad packet
        }
    }
}

void LoRaManager::handleRxPacket() {
    uint8_t buf[32];
    int idx = 0;
    while (LoRa.available() && idx < 32) buf[idx++] = LoRa.read();
    if (idx != 32 || buf[0] != 0x5A || buf[1] != 0xA5) return;

    uint8_t target = buf[2];
    if (target != cfg::NODE_ID && target != 0) return;

    uint16_t rxCRC   = buf[30] | (buf[31] << 8);
    uint16_t calcCRC = crc16_ccitt(buf, 30);
    if (rxCRC != calcCRC) return;

    if (buf[3] == 0xFE) {
        uint32_t fwSize = buf[4] | (buf[5] << 8) | (buf[6] << 16) | (buf[7] << 24);
        OtaHandler::begin(fwSize);
        return;
    } else if (buf[3] == 0xFC) {
        uint16_t chunkIdx = buf[4] | (buf[5] << 8);
        OtaHandler::writeChunk(chunkIdx, &buf[6], 24);
        return;
    } else if (buf[3] == 0xFD) {
        OtaHandler::end();
        return;
    }

    char msg[28];
    uint8_t out = 0;
    for (uint8_t i = 0; i < 27 && out < 27; i++) {
        uint8_t b = buf[3 + i];
        if (b == 0) break;
        if (b >= 32 && b <= 126) msg[out++] = (char)b;
        // skip non-ASCII bytes (e.g. UTF-8 multi-byte sequences for Vietnamese)
    }
    msg[out] = '\0';

    _lastMsg     = String(msg);
    _lastMsgTime = millis();

    Serial.printf("\n>> MSG [%s]: %s  RSSI:%d SNR:%.1f\n",
                  target == 0 ? "BCAST" : String(target).c_str(),
                  msg, LoRa.packetRssi(), LoRa.packetSnr());

    if (strncmp(msg, "CFG:SF=", 7) == 0) {
        int newSf = atoi(&msg[7]);
        if (newSf >= 7 && newSf <= 12) {
            cfg::RADIO_SF = newSf;
            Preferences prefs;
            prefs.begin("mesh", false);
            prefs.putUChar("sf", cfg::RADIO_SF);
            prefs.end();
            LoRa.setSpreadingFactor(cfg::RADIO_SF);
            Serial.printf("++ Applied new Spreading Factor: %d\n", cfg::RADIO_SF);
        }
    }

    if (target == cfg::NODE_ID) {
        // Send ACK back
        delay(50); // wait for gateway to switch to rx
        uint8_t ackPkt[8];
        ackPkt[0] = 0x5A;
        ackPkt[1] = 0xA6;
        ackPkt[2] = cfg::NODE_ID;
        ackPkt[3] = 0; // gateway
        ackPkt[4] = buf[30]; // include command CRC as ID
        ackPkt[5] = buf[31];
        uint16_t ackCrc = crc16_ccitt(ackPkt, 6);
        ackPkt[6] = ackCrc & 0xFF;
        ackPkt[7] = ackCrc >> 8;
        
        LoRa.beginPacket();
        LoRa.write(ackPkt, 8);
        LoRa.endPacket();
        LoRa.receive();
        Serial.println(F("<< FWD: Sent ACK"));
    }
}
