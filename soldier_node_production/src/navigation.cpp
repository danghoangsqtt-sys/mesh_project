#include "navigation.h"
#include "gps_manager.h"
#include "madgwick.h"
#include "ekf.h"

static Madgwick  ahrs(0.1f, 200.0f);
static SimpleEKF ekf;

void Navigation::begin() {
    ekf.setState(0, 0);
}

void Navigation::update(const ImuData& imu, const GPSManager& gps) {
    ahrs.update(imu.gx, imu.gy, imu.gz, imu.ax, imu.ay, imu.az);

    float yaw = ahrs.getYaw();
    if (yaw < 0) yaw += 360.0f;

    float mag = sqrtf(imu.ax * imu.ax + imu.ay * imu.ay + imu.az * imu.az);

    // Step detection with hysteresis (peak detection)
    bool step = false;
    if (!_aboveThreshold && mag > cfg::STEP_THRESHOLD) {
        _aboveThreshold = true;
    } else if (_aboveThreshold && mag < (cfg::STEP_THRESHOLD - cfg::STEP_HYSTERESIS)) {
        _aboveThreshold = false;
        float now = millis() / 1000.0f;
        if (now - _lastStepTime > cfg::STEP_MIN_INTERVAL) {
            step = true;
            _lastStepTime = now;
        }
    }

    if (gps.fixed()) {
        _nav.latitude  = gps.latitude();
        _nav.longitude = gps.longitude();
        _nav.heading   = yaw;

        float lonM = gps.longitude() * 111320.0f * cosf(gps.latitude() * DEG_TO_RAD);
        float latM = gps.latitude()  * 111320.0f;
        ekf.updateGPS(lonM, latM);
    } else {
        if (step) {
            float len = cfg::stepLength(_mode);
            float rad = yaw * DEG_TO_RAD;
            ekf.predict(len * sinf(rad), len * cosf(rad), yaw);
        }

        float ex, ey, eh, ev;
        ekf.getState(ex, ey, eh, ev);
        _nav.latitude  = ey / 111320.0f;
        _nav.longitude = ex / (111320.0f * cosf(_nav.latitude * DEG_TO_RAD));
        _nav.heading   = yaw;
    }
}