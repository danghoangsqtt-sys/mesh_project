#include "mpu6050_sensor.h"
#include <Adafruit_MPU6050.h>

static Adafruit_MPU6050 mpu;

bool MPU6050Sensor::begin() {
    _ready = mpu.begin(cfg::MPU6050_ADDR);
    if (_ready) {
        mpu.setAccelerometerRange(MPU6050_RANGE_8_G);
        mpu.setGyroRange(MPU6050_RANGE_500_DEG);
        mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);
    }
    return _ready;
}

ImuData MPU6050Sensor::read() {
    ImuData d;
    if (!_ready) return d;

    sensors_event_t accel, gyro, temp;
    mpu.getEvent(&accel, &gyro, &temp);

    d.ax = accel.acceleration.x;
    d.ay = accel.acceleration.y;
    d.az = accel.acceleration.z;
    d.gx = gyro.gyro.x;
    d.gy = gyro.gyro.y;
    d.gz = gyro.gyro.z;
    return d;
}