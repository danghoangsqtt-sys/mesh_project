#pragma once

#include "sensor.h"

class SensorHub {
public:
    void setImu(ImuSensor* s)       { _imu = s; }
    void setHealth(HealthSensor* s)  { _health = s; }
    void setEnv(EnvSensor* s)        { _env = s; }

    bool beginAll(Stream& log) {
        bool ok = true;
        Sensor* list[] = {_imu, _health, _env};
        for (auto* s : list) {
            if (!s) continue;
            log.print(s->name());
            log.print(F("... "));
            if (s->begin()) {
                log.println(F("OK"));
            } else {
                log.println(F("FAIL"));
                ok = false;
            }
        }
        return ok;
    }

    ImuSensor*    imu()    const { return _imu; }
    HealthSensor* health() const { return _health; }
    EnvSensor*    env()    const { return _env; }

    SensorSnapshot snapshot() {
        SensorSnapshot snap;
        snap.timestamp = millis();
        if (_imu    && _imu->ready())    snap.imu    = _imu->read();
        if (_health && _health->ready()) snap.health = _health->read();
        if (_env    && _env->ready())    snap.env    = _env->read();
        return snap;
    }

private:
    ImuSensor*    _imu    = nullptr;
    HealthSensor* _health = nullptr;
    EnvSensor*    _env    = nullptr;
};