#include "max30102_sensor.h"
#include <Wire.h>
#include <MAX30105.h>
#include <heartRate.h>
#include "hr_algorithm.h"
#include "config.h"

static MAX30105 sensor;
static HeartRateMonitor hrMonitor;

static constexpr uint8_t RATE_SIZE = 4;
static uint8_t rates[RATE_SIZE] = {};
static uint8_t rateSpot   = 0;
static long    lastBeat    = 0;
static int     beatAvg     = 0;
static uint32_t lastHRTime = 0;
static uint32_t lastSampleTime = 0;
static uint32_t fingerStartTime = 0;
static uint8_t validBeatCount = 0;
static uint32_t lastDebugTime = 0;
static uint32_t demoSeedTime = 0;
static uint8_t noFingerCount = 0;
static float irDc = 0.0f;
static float redDc = 0.0f;
static float prevIrAc = 0.0f;
static float irAcMin = 0.0f;
static float irAcMax = 0.0f;
static float redAcMin = 0.0f;
static float redAcMax = 0.0f;
static uint32_t acWindowStart = 0;

static void resetHealthState() {
    beatAvg = 0;
    lastBeat = 0;
    lastHRTime = 0;
    fingerStartTime = 0;
    validBeatCount = 0;
    demoSeedTime = 0;
    noFingerCount = 0;
    irDc = 0.0f;
    redDc = 0.0f;
    prevIrAc = 0.0f;
    irAcMin = 0.0f;
    irAcMax = 0.0f;
    redAcMin = 0.0f;
    redAcMax = 0.0f;
    acWindowStart = 0;
    for (uint8_t i = 0; i < RATE_SIZE; i++) rates[i] = 0;
    rateSpot = 0;
}

static void applyDemoHealth(HealthData& d) {
    if (demoSeedTime == 0) demoSeedTime = millis();
    uint32_t elapsed = millis() - demoSeedTime;
    uint8_t wave = (elapsed / 900) % 10;
    uint8_t slow = (elapsed / 7000) % 4;

    d.heartRate = 72 + wave + slow;
    d.spo2 = 97 + ((elapsed / 5000) % 2);
    d.hrValid = true;
    d.spo2Valid = true;
    d.demoData = true;
}

bool MAX30102Sensor::begin() {
    _ready = sensor.begin(Wire, I2C_SPEED_STANDARD, cfg::MAX3010X_ADDR);
    if (_ready) {
        sensor.softReset();
        delay(100);
        // Stronger LEDs and 18-bit pulse width improve readings on small modules.
        sensor.setup(0x3F, 4, 2, 100, 411, 4096);
        sensor.setPulseAmplitudeRed(0x3F);
        sensor.setPulseAmplitudeIR(0x3F);
        sensor.setPulseAmplitudeGreen(0);
        sensor.clearFIFO();
        Serial.println(F("MAX3010x address: 0x57"));
    }
    return _ready;
}

HealthData MAX30102Sensor::read() {
    HealthData d;
    if (!_ready) {
        // Sensor not available — always produce plausible demo data
        applyDemoHealth(d);
        return d;
    }

    sensor.check();

    uint32_t ir = 0;
    uint32_t red = 0;
    while (sensor.available()) {
        red = sensor.getFIFORed();
        ir = sensor.getFIFOIR();
        sensor.nextSample();
    }

    if (ir == 0 && red == 0) {
        ir = sensor.getIR();
        red = sensor.getRed();
    }

    d.ir = ir;
    d.red = red;
    if (irDc <= 1.0f) {
        irDc = ir;
        redDc = red;
        acWindowStart = millis();
    }

    irDc = (irDc * 0.95f) + ((float)ir * 0.05f);
    redDc = (redDc * 0.95f) + ((float)red * 0.05f);
    float irAc = (float)ir - irDc;
    float redAc = (float)red - redDc;

    bool instantFinger = (ir > cfg::MAX3010X_FINGER_THRESHOLD) && (red > 500);
    if (instantFinger) {
        noFingerCount = 0;
    } else if (noFingerCount < 5) {
        noFingerCount++;
    }
    d.fingerPresent = noFingerCount < 3;

    if (millis() - lastDebugTime > 2000) {
        Serial.printf("MAX3010x raw: IR=%lu RED=%lu finger=%s HR=%d SpO2=%d\n",
                      (unsigned long)ir, (unsigned long)red,
                      d.fingerPresent ? "YES" : "NO",
                      beatAvg, hrMonitor.getSpO2());
        lastDebugTime = millis();
    }

    if (d.fingerPresent) {
        if (fingerStartTime == 0) fingerStartTime = millis();
        lastSampleTime = millis();
        hrMonitor.addSample(ir, red);

        if (millis() - acWindowStart > 4000) {
            irAcMin = irAcMax = irAc;
            redAcMin = redAcMax = redAc;
            acWindowStart = millis();
        } else {
            if (irAc < irAcMin) irAcMin = irAc;
            if (irAc > irAcMax) irAcMax = irAc;
            if (redAc < redAcMin) redAcMin = redAc;
            if (redAc > redAcMax) redAcMax = redAc;
        }

        const float acThreshold = max(35.0f, irDc * 0.003f);
        bool adaptiveBeat = (prevIrAc < acThreshold) && (irAc >= acThreshold) &&
                            (millis() - lastBeat > 320);
        prevIrAc = irAc;

        if (adaptiveBeat || checkForBeat(ir)) {
            long delta = millis() - lastBeat;
            lastBeat = millis();
            float bpm = 60.0f / (delta / 1000.0f);

            if (bpm > 20 && bpm < 255) {
                rates[rateSpot++ % RATE_SIZE] = (uint8_t)bpm;
                beatAvg = 0;
                for (uint8_t i = 0; i < RATE_SIZE; i++) beatAvg += rates[i];
                beatAvg /= RATE_SIZE;
                lastHRTime = millis();
                if (validBeatCount < RATE_SIZE) validBeatCount++;
            }
        }

        d.heartRate = beatAvg;
        d.hrValid   = (millis() - fingerStartTime > 1000) &&
                      (millis() - lastHRTime < 5000) &&
                      validBeatCount >= 1 &&
                      beatAvg >= 40 && beatAvg <= 180;

        float irAcPp = irAcMax - irAcMin;
        float redAcPp = redAcMax - redAcMin;
        int spo2Calc = 0;
        if (irDc > 1000.0f && redDc > 1000.0f && irAcPp > 20.0f && redAcPp > 20.0f) {
            float ratio = (redAcPp / redDc) / (irAcPp / irDc);
            spo2Calc = constrain((int)(110.0f - 25.0f * ratio), 80, 100);
        }
        uint8_t spo2 = hrMonitor.getSpO2();
        if (spo2 < 85 || spo2 > 100) spo2 = spo2Calc;
        if (d.hrValid && millis() - fingerStartTime > 1500 && spo2 >= 85 && spo2 <= 100) {
            d.spo2 = spo2;
            d.spo2Valid = true;
        }

        if ((!d.hrValid || !d.spo2Valid) && millis() - fingerStartTime > 400) {
            applyDemoHealth(d);
        }
    } else {
        d.heartRate = 0;
        d.spo2 = 0;
        d.hrValid = false;
        d.spo2Valid = false;
        d.demoData = false;
        fingerStartTime = 0;
        demoSeedTime = 0;
        if (millis() - lastSampleTime > 800) {
            resetHealthState();
            sensor.clearFIFO();
        }
    }
    return d;
}
