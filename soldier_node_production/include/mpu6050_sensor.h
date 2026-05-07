#pragma once

#include "sensor.h"
#include "config.h"

class MPU6050Sensor : public ImuSensor {
public:
    const char* name() const override { return "MPU6050"; }
    bool begin() override;
    ImuData read() override;
};