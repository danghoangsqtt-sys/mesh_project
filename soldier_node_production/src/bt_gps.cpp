/**
 * bt_gps.cpp
 * Bluetooth Classic GPS NMEA Module – implementation
 *
 * Uses BluetoothSerial discoverAsync() for scanning (resolves names).
 * Rescans every SCAN_INTERVAL_MS. Duplicates filtered by MAC.
 * IO38 short press = next device, long press = connect.
 */

#include "config.h"

#if defined(BOARD_TBEAM)

#include "bt_gps.h"

namespace {

BluetoothSerial btSerial;

struct DeviceInfo {
    char     name[64];
    uint8_t  address[6];
    int      rssi;
};

DeviceInfo devices[btgps::MAX_DEVICES];
uint8_t    deviceCount   = 0;
uint8_t    pointerIndex  = 0;

btgps::State   moduleState    = btgps::State::SCANNING;
char           connectedName[64] = "";
Stream*        dbg             = nullptr;

btgps::GpsFix  currentFix;
bool           newFixFlag = false;
char           lastNmea[84] = "";

uint32_t btnPressTime   = 0;
bool     btnWasPressed  = false;
bool     longPressHandled = false;

uint32_t scanStartMs   = 0;
uint32_t scanEndMs     = 0;
bool     scanActive    = false;

// ---------- helpers ----------

void dbgPrint(const char* msg) {
    if (dbg) dbg->println(msg);
}
void dbgPrintf(const char* fmt, ...) {
    if (!dbg) return;
    char buf[128];
    va_list args;
    va_start(args, fmt);
    vsnprintf(buf, sizeof(buf), fmt, args);
    va_end(args);
    dbg->print(buf);
}

/** Find existing device by MAC, return index or -1 */
int8_t findByAddress(const uint8_t addr[6]) {
    for (uint8_t i = 0; i < deviceCount; ++i) {
        if (memcmp(devices[i].address, addr, 6) == 0) return i;
    }
    return -1;
}

// ---------- discoverAsync callback ----------

void onDeviceFound(BTAdvertisedDevice* pDevice) {
    // Get address as raw bytes
    esp_bd_addr_t rawAddr;
    memcpy(rawAddr, pDevice->getAddress().getNative(), 6);

    int8_t existingIdx = findByAddress(rawAddr);
    DeviceInfo* d;

    if (existingIdx >= 0) {
        d = &devices[existingIdx];
    } else {
        if (deviceCount >= btgps::MAX_DEVICES) return;
        d = &devices[deviceCount];
        memcpy(d->address, rawAddr, 6);
        d->name[0] = '\0';
        d->rssi    = -127;
    }

    // Update RSSI
    d->rssi = pDevice->getRSSI();

    // Update name if available (BluetoothSerial resolves names for us)
    std::string devName = pDevice->getName();
    if (devName.length() > 0) {
        strncpy(d->name, devName.c_str(), sizeof(d->name) - 1);
        d->name[sizeof(d->name) - 1] = '\0';
    }

    if (existingIdx >= 0) {
        dbgPrintf("[BT-GPS] Updated: %s (RSSI %d)\n", d->name, d->rssi);
    } else {
        // New device — set MAC placeholder if no name yet
        if (d->name[0] == '\0') {
            snprintf(d->name, sizeof(d->name), "[%02X:%02X:%02X:%02X:%02X:%02X]",
                     d->address[0], d->address[1], d->address[2],
                     d->address[3], d->address[4], d->address[5]);
        }
        dbgPrintf("[BT-GPS] Found: %s (RSSI %d)\n", d->name, d->rssi);
        ++deviceCount;
    }
}

// ---------- button ----------

enum class ButtonEvent { NONE, SHORT_PRESS, LONG_PRESS };

ButtonEvent pollButton() {
    bool pressed = (digitalRead(btgps::BUTTON_PIN) == LOW);

    if (pressed && !btnWasPressed) {
        btnPressTime     = millis();
        btnWasPressed    = true;
        longPressHandled = false;
        return ButtonEvent::NONE;
    }

    if (pressed && btnWasPressed && !longPressHandled) {
        if ((millis() - btnPressTime) >= btgps::LONG_PRESS_MS) {
            longPressHandled = true;
            return ButtonEvent::LONG_PRESS;
        }
    }

    if (!pressed && btnWasPressed) {
        uint32_t held = millis() - btnPressTime;
        btnWasPressed = false;
        if (!longPressHandled && held >= btgps::SHORT_PRESS_MS) {
            return ButtonEvent::SHORT_PRESS;
        }
    }

    return ButtonEvent::NONE;
}

// ---------- NMEA ----------

bool nmeaField(const char* sentence, uint8_t fieldIndex, char* out, uint8_t maxLen) {
    uint8_t field = 0;
    uint8_t j     = 0;
    out[0]        = '\0';
    for (uint8_t i = 0; sentence[i] != '\0' && sentence[i] != '*'; ++i) {
        if (sentence[i] == ',') {
            if (field == fieldIndex) { out[j] = '\0'; return j > 0; }
            ++field;
            j = 0;
        } else if (field == fieldIndex) {
            if (j < maxLen - 1) out[j++] = sentence[i];
        }
    }
    if (field == fieldIndex) { out[j] = '\0'; return j > 0; }
    return false;
}

double nmeaToDeg(const char* val, char hemi) {
    if (!val || val[0] == '\0') return 0.0;
    double raw     = atof(val);
    int    degrees = (int)(raw / 100);
    double minutes = raw - degrees * 100.0;
    double dd      = degrees + minutes / 60.0;
    if (hemi == 'S' || hemi == 'W') dd = -dd;
    return dd;
}

bool nmeaChecksum(const char* sentence) {
    const char* p = sentence;
    if (*p == '$') ++p;
    uint8_t calc = 0;
    while (*p && *p != '*') calc ^= (uint8_t)*p++;
    if (*p != '*') return false;
    uint8_t expected = (uint8_t)strtol(p + 1, nullptr, 16);
    return calc == expected;
}

void parseRMC(const char* sentence) {
    char field[20];
    nmeaField(sentence, 2, field, sizeof(field));
    currentFix.valid = (field[0] == 'A');
    if (!currentFix.valid) return;

    char lat[12], latH[2], lon[12], lonH[2];
    nmeaField(sentence, 3, lat,  sizeof(lat));
    nmeaField(sentence, 4, latH, sizeof(latH));
    nmeaField(sentence, 5, lon,  sizeof(lon));
    nmeaField(sentence, 6, lonH, sizeof(lonH));
    currentFix.latitude  = nmeaToDeg(lat,  latH[0]);
    currentFix.longitude = nmeaToDeg(lon,  lonH[0]);

    nmeaField(sentence, 7, field, sizeof(field));
    currentFix.speedKnots = atof(field);
    nmeaField(sentence, 8, field, sizeof(field));
    currentFix.courseDeg = atof(field);

    currentFix.timestampMs = millis();
    newFixFlag = true;
}

void parseGGA(const char* sentence) {
    char field[20];
    nmeaField(sentence, 6, field, sizeof(field));
    if (atoi(field) == 0) return;

    char lat[12], latH[2], lon[12], lonH[2];
    nmeaField(sentence, 2, lat,  sizeof(lat));
    nmeaField(sentence, 3, latH, sizeof(latH));
    nmeaField(sentence, 4, lon,  sizeof(lon));
    nmeaField(sentence, 5, lonH, sizeof(lonH));
    currentFix.latitude  = nmeaToDeg(lat,  latH[0]);
    currentFix.longitude = nmeaToDeg(lon,  lonH[0]);

    nmeaField(sentence, 7, field, sizeof(field));
    currentFix.satellites = atoi(field);
    nmeaField(sentence, 8, field, sizeof(field));
    currentFix.hdop = atof(field);
    nmeaField(sentence, 9, field, sizeof(field));
    currentFix.altitudeM = atof(field);

    currentFix.valid       = true;
    currentFix.timestampMs = millis();
    newFixFlag = true;
}

void dispatchNmea(const char* sentence) {
    if (!nmeaChecksum(sentence)) {
        dbgPrintf("[BT-GPS] Checksum fail: %s\n", sentence);
        return;
    }
    strncpy(lastNmea, sentence, sizeof(lastNmea) - 1);
    lastNmea[sizeof(lastNmea) - 1] = '\0';

    if (strncmp(sentence + 1, "GPRMC", 5) == 0 ||
        strncmp(sentence + 1, "GNRMC", 5) == 0) {
        parseRMC(sentence);
    } else if (strncmp(sentence + 1, "GPGGA", 5) == 0 ||
               strncmp(sentence + 1, "GNGGA", 5) == 0) {
        parseGGA(sentence);
    }
}

char    nmeaBuf[84];
uint8_t nmeaLen = 0;

void readNmeaStream() {
    while (btSerial.available()) {
        char c = btSerial.read();
        if (c == '$') nmeaLen = 0;
        if (nmeaLen < sizeof(nmeaBuf) - 1) nmeaBuf[nmeaLen++] = c;
        if (c == '\n') {
            nmeaBuf[nmeaLen] = '\0';
            while (nmeaLen > 0 &&
                   (nmeaBuf[nmeaLen-1] == '\r' || nmeaBuf[nmeaLen-1] == '\n'))
                nmeaBuf[--nmeaLen] = '\0';
            if (nmeaBuf[0] == '$') dispatchNmea(nmeaBuf);
            nmeaLen = 0;
        }
    }
}

// ---------- scan / connect ----------

void stopScan() {
    if (scanActive) {
        btSerial.discoverAsyncStop();
        scanActive = false;
        scanEndMs  = millis();
    }
}

void connectToDevice(uint8_t idx) {
    if (idx >= deviceCount) return;
    moduleState = btgps::State::CONNECTING;

    stopScan();
    delay(500);  // Allow BT stack to settle

    DeviceInfo& d = devices[idx];
    strncpy(connectedName, d.name, sizeof(connectedName) - 1);
    connectedName[sizeof(connectedName) - 1] = '\0';

    bool ok = false;

    // Try connecting by name first (more reliable for phones)
    if (d.name[0] != '\0' && d.name[0] != '[') {
        dbgPrintf("[BT-GPS] Connecting by name: %s\n", d.name);
        ok = btSerial.connect(d.name);
    }

    // Fall back to address-based connection
    if (!ok) {
        dbgPrintf("[BT-GPS] Connecting by address: %02X:%02X:%02X:%02X:%02X:%02X\n",
                  d.address[0], d.address[1], d.address[2],
                  d.address[3], d.address[4], d.address[5]);
        ok = btSerial.connect(d.address);
    }

    if (ok) {
        dbgPrintf("[BT-GPS] Connected to %s\n", d.name);
        moduleState = btgps::State::CONNECTED;
    } else {
        dbgPrint("[BT-GPS] Connection failed.");
        moduleState = btgps::State::ERROR;
    }
}

/** Start a new scan cycle.  keepDevices=true preserves the existing list. */
void startScan(bool keepDevices = false) {
    if (!keepDevices) {
        deviceCount  = 0;
        pointerIndex = 0;
    }
    scanActive  = true;
    scanStartMs = millis();

    btSerial.discoverAsync(onDeviceFound, btgps::SCAN_DURATION_MS);
    dbgPrint("[BT-GPS] BT scan started...");
}

} // anonymous namespace

// ======================================================================
namespace btgps {

void begin(Stream* serialDebug) {
    dbg = serialDebug;
    dbgPrint("[BT-GPS] Initialising module...");

    pinMode(BUTTON_PIN, INPUT_PULLUP);

    if (!btSerial.begin(LOCAL_BT_NAME, true)) {  // true = master mode (client)
        dbgPrint("[BT-GPS] ERROR: BluetoothSerial init failed.");
        moduleState = State::ERROR;
        return;
    }

    memset(&currentFix, 0, sizeof(currentFix));
    newFixFlag       = false;
    nmeaLen          = 0;
    connectedName[0] = '\0';
    moduleState      = State::SCANNING;

    startScan(false);
}

void update() {
    ButtonEvent btn = pollButton();

    switch (moduleState) {

    case State::SCANNING:
        // Check if scan duration elapsed
        if (scanActive && (millis() - scanStartMs) >= SCAN_DURATION_MS + 500) {
            stopScan();
            dbgPrintf("[BT-GPS] Scan done. %d device(s) found.\n", deviceCount);
            moduleState = State::SELECTING;
        }
        // Short press while scanning: move pointer if devices found
        if (btn == ButtonEvent::SHORT_PRESS && deviceCount > 0) {
            pointerIndex = (pointerIndex + 1) % deviceCount;
            dbgPrintf("[BT-GPS] Pointer -> %d: %s\n",
                      pointerIndex, devices[pointerIndex].name);
        }
        // Long press while scanning: select if devices found
        if (btn == ButtonEvent::LONG_PRESS && deviceCount > 0) {
            connectToDevice(pointerIndex);
        }
        break;

    case State::SELECTING:
        if (deviceCount == 0) {
            dbgPrint("[BT-GPS] No devices found. Rescanning...");
            startScan(false);
            moduleState = State::SCANNING;
            break;
        }

        if (btn == ButtonEvent::SHORT_PRESS) {
            pointerIndex = (pointerIndex + 1) % deviceCount;
            dbgPrintf("[BT-GPS] Pointer -> %d: %s\n",
                      pointerIndex, devices[pointerIndex].name);
        } else if (btn == ButtonEvent::LONG_PRESS) {
            connectToDevice(pointerIndex);
        }

        // Auto-rescan every SCAN_INTERVAL_MS while user is browsing
        if (!scanActive && (millis() - scanEndMs) >= SCAN_INTERVAL_MS) {
            dbgPrint("[BT-GPS] Auto-rescan (keeping list)...");
            startScan(true);
        }
        break;

    case State::CONNECTING:
        break;

    case State::CONNECTED:
        if (!btSerial.connected()) {
            dbgPrint("[BT-GPS] Disconnected. Returning to scan.");
            moduleState = State::ERROR;
            break;
        }
        readNmeaStream();

        if (btn == ButtonEvent::LONG_PRESS) {
            btSerial.disconnect();
            startScan(false);
            moduleState = State::SCANNING;
        }
        break;

    case State::ERROR:
        if (btn != ButtonEvent::NONE) {
            begin(dbg);
        }
        break;
    }
}

State       getState()               { return moduleState; }
bool        isConnected()            { return moduleState == State::CONNECTED; }
const char* getConnectedDeviceName() { return connectedName; }
bool        isScanActive()           { return scanActive; }

bool hasNewFix() {
    if (newFixFlag) { newFixFlag = false; return true; }
    return false;
}
GpsFix      getFix()      { return currentFix; }
const char* getLastNmea() { return lastNmea; }

uint8_t     getDeviceCount()       { return deviceCount; }
const char* getDeviceName(uint8_t i) {
    return (i < deviceCount) ? devices[i].name : "";
}
uint8_t getPointerIndex() { return pointerIndex; }

} // namespace btgps

#endif // BOARD_TBEAM
