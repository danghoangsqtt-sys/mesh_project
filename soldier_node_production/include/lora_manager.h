#pragma once

#include <Arduino.h>
#include "config.h"
#include "packet.h"
#include "sensor.h"

class GPSManager;
class Navigation;

class LoRaManager {
public:
    bool begin(Stream& log);
    void transmit(const SensorSnapshot& snap, const NavData& nav, uint16_t flags);
    void pollReceive();

    const SoldierPacket& packet()     const { return _pkt; }
    const String& lastMessage()       const { return _lastMsg; }
    uint32_t      lastMessageTime()   const { return _lastMsgTime; }
    bool          batteryPresent()    const { return _batteryPresent; }
    bool          usbPowered()        const { return _usbPowered; }

private:
    SoldierPacket _pkt         = {};
    String        _lastMsg;
    uint32_t      _lastMsgTime = 0;
    bool          _batteryPresent = false;
    bool          _usbPowered = false;

    float readBattery();
    void  handleRxPacket();
};
