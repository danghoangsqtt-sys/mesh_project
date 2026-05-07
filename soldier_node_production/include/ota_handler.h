#pragma once

#include <Arduino.h>

class OtaHandler {
public:
    static void begin(uint32_t firmwareSize);
    static void writeChunk(uint16_t chunkIndex, const uint8_t* data, uint8_t length);
    static void end();
};
