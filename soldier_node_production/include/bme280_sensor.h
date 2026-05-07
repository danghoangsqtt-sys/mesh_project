#pragma once

#include "sensor.h"
#include "config.h"

class BME280Sensor : public EnvSensor {
public:
    const char* name() const override { return "BME280"; }
    bool begin() override;
    EnvData read() override;
};