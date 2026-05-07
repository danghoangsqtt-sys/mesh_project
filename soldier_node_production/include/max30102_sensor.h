#pragma once

#include "sensor.h"

class MAX30102Sensor : public HealthSensor {
public:
    const char* name() const override { return "MAX30102"; }
    bool begin() override;
    HealthData read() override;
};