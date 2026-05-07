#pragma once

#include "sensor.h"
#include "config.h"

class GPSManager;

class Navigation {
public:
    void begin();
    void update(const ImuData& imu, const GPSManager& gps);

    NavData position() const { return _nav; }
    float   heading()  const { return _nav.heading; }

    void setMoveMode(cfg::MoveMode m) { _mode = m; }

private:
    NavData       _nav;
    cfg::MoveMode _mode = cfg::MoveMode::WALK;

    float _lastStepTime    = 0;
    bool  _aboveThreshold  = false;
};