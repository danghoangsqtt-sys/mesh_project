#ifndef PACKET_H
#define PACKET_H

#include <Arduino.h>

#define PACKET_SIZE 40

struct __attribute__((packed)) SoldierPacket {
    uint16_t nodeId;
    uint32_t timestamp;
    float latitude;
    float longitude;
    float heading;
    uint8_t heartRate;
    uint8_t spo2;
    float temperature;
    float humidity;
    float pressure;
    float batteryVoltage;
    uint16_t statusFlags;
    uint16_t crc16;
};

enum StatusFlags {
    GPS_FIX = 0x0001,
    IMU_VALID = 0x0002,
    HR_VALID = 0x0004,
    SPO2_VALID = 0x0008,
    TEMP_VALID = 0x0010,
    LOW_BATTERY = 0x0020,
    CRITICAL_BATTERY = 0x0040,
    SENSOR_ERROR = 0x0080,
    ALERT = 0x0100,
    MAN_DOWN = 0x0200,
    HEAT_STRESS = 0x0400,
    HUMIDITY_VALID = 0x0800,
    PRESSURE_VALID = 0x1000,
    BATTERY_PRESENT = 0x2000,
    USB_POWER = 0x4000
};

inline uint16_t crc16_ccitt(const uint8_t *data, size_t length) {
    uint16_t crc = 0xFFFF;
    for (size_t i = 0; i < length; i++) {
        crc ^= (uint16_t)data[i] << 8;
        for (uint8_t j = 0; j < 8; j++) {
            if (crc & 0x8000)
                crc = (crc << 1) ^ 0x1021;
            else
                crc = crc << 1;
        }
    }
    return crc;
}

inline void packetCalculateCRC(SoldierPacket *pkt) {
    pkt->crc16 = crc16_ccitt((uint8_t*)pkt, PACKET_SIZE - 2);
}

inline bool packetValidateCRC(const SoldierPacket *pkt) {
    uint16_t calculated = crc16_ccitt((const uint8_t*)pkt, PACKET_SIZE - 2);
    return calculated == pkt->crc16;
}

#endif
