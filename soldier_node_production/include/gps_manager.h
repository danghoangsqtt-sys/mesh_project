#pragma once

#include <Arduino.h>
#include "config.h"
#include "sensor.h"

class GPSManager {
public:
    bool beginUART(Stream& log);
    void update();

    float latitude()  const { return _lat; }
    float longitude() const { return _lon; }
    bool  fixed()     const { return _fixed; }
    uint8_t satellites() const;

private:
    float    _lat       = 0;
    float    _lon       = 0;
    bool     _fixed     = false;
    uint32_t _lastFix   = 0;

};