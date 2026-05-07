#pragma once

#include "config.h"

#if defined(BOARD_TBEAM)


#include <Arduino.h>
#include <BluetoothSerial.h>

namespace btgps {

/** Parsed GPS fix from NMEA $GPRMC / $GPGGA sentences */
struct GpsFix {
    double   latitude   = 0.0;   ///< decimal degrees, + = N
    double   longitude  = 0.0;   ///< decimal degrees, + = E
    float    speedKnots = 0.0;
    float    courseDeg  = 0.0;
    float    altitudeM  = 0.0;
    uint8_t  satellites = 0;
    float    hdop       = 99.9f;
    bool     valid      = false;  ///< true only when NMEA reports 'A' / fix type >= 1
    uint32_t timestampMs = 0;     ///< millis() when fix was parsed
};

/** Module operating states (read-only for callers) */
enum class State : uint8_t {
    SCANNING,    ///< actively scanning for BT devices
    SELECTING,   ///< scan done, user is browsing the list
    CONNECTING,  ///< pairing / connecting in progress
    CONNECTED,   ///< connected, streaming NMEA
    ERROR        ///< unrecoverable error – call begin() to restart
};

/** Pin for the IO38 navigation button (active-LOW on T-Beam) */
constexpr uint8_t  BUTTON_PIN        = 38;
/** Short-press threshold in ms */
constexpr uint32_t SHORT_PRESS_MS    = 50;
/** Long-press threshold in ms */
constexpr uint32_t LONG_PRESS_MS     = 800;
/** How long each scan cycle lasts (ms) */
constexpr uint32_t SCAN_DURATION_MS  = 5000;
/** Pause between scan cycles while selecting (ms) */
constexpr uint32_t SCAN_INTERVAL_MS  = 5000;
/** Maximum devices kept in the scan list */
constexpr uint8_t  MAX_DEVICES       = 20;
/** Bluetooth device name shown to peers */
constexpr char     LOCAL_BT_NAME[]   = "TBeam-DR";

/**
 * Initialise the module.  Safe to call again to restart after an error.
 * @param serialDebug  optional Stream for debug output (e.g. &Serial)
 */
void begin(Stream* serialDebug = nullptr);

/**
 * Must be called from loop() as frequently as possible.
 * Handles scanning, button debounce, connection management and NMEA parsing.
 */
void update();

State       getState();
bool        isConnected();
bool        isScanActive();
const char* getConnectedDeviceName();

/** Returns true once per new valid fix; clears the flag internally */
bool        hasNewFix();
/** Returns the most recently parsed fix (may be invalid if no fix yet) */
GpsFix      getFix();
/** Raw last NMEA sentence (null-terminated, up to 82 chars + NUL) */
const char* getLastNmea();

/** Number of devices found in last scan */
uint8_t     getDeviceCount();
/** Device name at index i (empty string if out of range) */
const char* getDeviceName(uint8_t i);
/** Currently highlighted index in the selection list */
uint8_t     getPointerIndex();

} // namespace btgps

#endif // BOARD_TBEAM
