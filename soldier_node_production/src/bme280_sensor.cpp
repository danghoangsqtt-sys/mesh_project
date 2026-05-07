#include "bme280_sensor.h"
#include <Wire.h>
#include <Adafruit_BME280.h>

static Adafruit_BME280 bme;
static uint8_t bmeAddress = cfg::BME280_ADDR_PRIMARY;

bool BME280Sensor::begin() {
    _ready = bme.begin(cfg::BME280_ADDR_PRIMARY, &Wire);
    bmeAddress = cfg::BME280_ADDR_PRIMARY;
    if (!_ready) {
        _ready = bme.begin(cfg::BME280_ADDR_ALT, &Wire);
        bmeAddress = cfg::BME280_ADDR_ALT;
    }
    if (_ready) {
        bme.setSampling(
            Adafruit_BME280::MODE_NORMAL,
            Adafruit_BME280::SAMPLING_X8,
            Adafruit_BME280::SAMPLING_X4,
            Adafruit_BME280::SAMPLING_X2,
            Adafruit_BME280::FILTER_X4,
            Adafruit_BME280::STANDBY_MS_500
        );
        Serial.printf("BME280 address: 0x%02X\n", bmeAddress);
    }
    return _ready;
}

EnvData BME280Sensor::read() {
    EnvData d;
    if (!_ready) return d;
    d.temperature = bme.readTemperature();
    d.pressure    = bme.readPressure() / 100.0f;
    d.humidity    = bme.readHumidity();
    return d;
}
