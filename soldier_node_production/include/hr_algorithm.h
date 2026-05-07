#ifndef HR_ALGORITHM_H
#define HR_ALGORITHM_H

#include <Arduino.h>

#define HR_BUFFER_SIZE 100
#define BEAT_THRESHOLD 50000  // IR threshold for beat detection

class HeartRateMonitor {
private:
    uint32_t irBuffer[HR_BUFFER_SIZE];
    uint32_t redBuffer[HR_BUFFER_SIZE];
    uint8_t bufferIndex;
    uint32_t lastBeatTime;
    uint8_t heartRate;
    uint8_t spo2;
    bool bufferFull;
    
public:
    HeartRateMonitor() : bufferIndex(0), lastBeatTime(0), heartRate(0), spo2(0), bufferFull(false) {}
    
    void addSample(uint32_t irValue, uint32_t redValue) {
        irBuffer[bufferIndex] = irValue;
        redBuffer[bufferIndex] = redValue;
        bufferIndex++;
        
        if (bufferIndex >= HR_BUFFER_SIZE) {
            bufferIndex = 0;
            bufferFull = true;
        }
        
        // Simple peak detection
        if (bufferFull && bufferIndex >= 2) {
            uint8_t prev = (bufferIndex - 1 + HR_BUFFER_SIZE) % HR_BUFFER_SIZE;
            uint8_t prev2 = (bufferIndex - 2 + HR_BUFFER_SIZE) % HR_BUFFER_SIZE;
            
            // Detect peak: previous > prev2 AND previous > current
            if (irBuffer[prev] > BEAT_THRESHOLD &&
                irBuffer[prev] > irBuffer[prev2] &&
                irBuffer[prev] > irBuffer[bufferIndex]) {
                
                uint32_t currentTime = millis();
                if (lastBeatTime > 0) {
                    uint32_t beatInterval = currentTime - lastBeatTime;
                    if (beatInterval > 300 && beatInterval < 2000) {  // 30-200 BPM range
                        heartRate = 60000 / beatInterval;
                    }
                }
                lastBeatTime = currentTime;
            }
        }
        
        // Calculate SpO2 using Red/IR ratio (simplified)
        if (bufferFull) {
            calculateSpO2();
        }
    }
    
    void calculateSpO2() {
        // Simplified SpO2 calculation
        // Real algorithm: SpO2 = 110 - 25 * (ACred/DCred) / (ACir/DCir)
        
        uint32_t irAC = 0, irDC = 0, redAC = 0, redDC = 0;
        
        // Calculate DC (average)
        for (int i = 0; i < HR_BUFFER_SIZE; i++) {
            irDC += irBuffer[i];
            redDC += redBuffer[i];
        }
        irDC /= HR_BUFFER_SIZE;
        redDC /= HR_BUFFER_SIZE;
        
        // Calculate AC (peak-to-peak)
        uint32_t irMin = irDC, irMax = irDC;
        uint32_t redMin = redDC, redMax = redDC;
        
        for (int i = 0; i < HR_BUFFER_SIZE; i++) {
            if (irBuffer[i] < irMin) irMin = irBuffer[i];
            if (irBuffer[i] > irMax) irMax = irBuffer[i];
            if (redBuffer[i] < redMin) redMin = redBuffer[i];
            if (redBuffer[i] > redMax) redMax = redBuffer[i];
        }
        
        irAC = irMax - irMin;
        redAC = redMax - redMin;
        
        // Avoid division by zero
        if (irDC > 0 && redDC > 0 && irAC > 0) {
            float ratio = ((float)redAC / (float)redDC) / ((float)irAC / (float)irDC);
            
            // Empirical formula (adjust based on calibration)
            spo2 = (uint8_t)(110.0f - 25.0f * ratio);
            
            // Clamp to valid range
            if (spo2 > 100) spo2 = 100;
            if (spo2 < 80) spo2 = 80;
        }
    }
    
    uint8_t getHeartRate() {
        // Return 0 if no beats detected in last 3 seconds
        if (millis() - lastBeatTime > 3000) {
            return 0;
        }
        return heartRate;
    }
    
    uint8_t getSpO2() {
        return bufferFull ? spo2 : 0;
    }
    
    bool isValid() {
        return bufferFull && (millis() - lastBeatTime < 3000);
    }
};

#endif
