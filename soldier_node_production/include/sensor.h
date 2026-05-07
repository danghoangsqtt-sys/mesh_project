#pragma once

#include <Arduino.h>

struct ImuData {
    float ax = 0, ay = 0, az = 0;
    float gx = 0, gy = 0, gz = 0;
};

struct HealthData {
    uint8_t heartRate = 0;
    uint8_t spo2     = 0;
    bool hrValid     = false;
    bool spo2Valid   = false;
    bool fingerPresent = false;
    bool demoData    = false;
    uint32_t ir      = 0;
    uint32_t red     = 0;
};

struct EnvData {
    float temperature = NAN;
    float pressure    = NAN;
    float humidity    = NAN;
};

struct SensorSnapshot {
    uint32_t   timestamp = 0;
    ImuData    imu;
    HealthData health;
    EnvData    env;
};

struct NavData {
    float latitude  = 0;
    float longitude = 0;
    float heading   = 0;
};

class Sensor {
public:
    virtual ~Sensor() = default;
    virtual const char* name() const = 0;
    virtual bool begin() = 0;
    bool ready() const { return _ready; }
protected:
    bool _ready = false;
};

class ImuSensor : public Sensor {
public:
    virtual ImuData read() = 0;
};

class HealthSensor : public Sensor {
public:
    virtual HealthData read() = 0;
};

class EnvSensor : public Sensor {
public:
    virtual EnvData read() = 0;
};
