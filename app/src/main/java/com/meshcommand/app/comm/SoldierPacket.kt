package com.meshcommand.app.comm

data class SoldierPacket(
    val nodeId: Int, // uint16
    val timestamp: Long, // uint32
    val latitude: Float,
    val longitude: Float,
    val heading: Float,
    val heartRate: Int, // uint8
    val spo2: Int, // uint8
    val temperature: Float,
    val humidity: Float,
    val pressure: Float,
    val batteryVoltage: Float,
    val statusFlags: Int, // uint16
    val crc16: Int = 0 // uint16
) {
    companion object {
        const val PACKET_SIZE = 40
        
        // Status Flags
        const val GPS_FIX = 0x0001
        const val IMU_VALID = 0x0002
        const val HR_VALID = 0x0004
        const val SPO2_VALID = 0x0008
        const val TEMP_VALID = 0x0010
        const val HUMIDITY_VALID = 0x0800
        const val PRESSURE_VALID = 0x1000
        const val LOW_BATTERY = 0x0020
        const val CRITICAL_BATTERY = 0x0040
        const val SENSOR_ERROR = 0x0080
        const val ALERT = 0x0100
        const val MAN_DOWN = 0x0200
        const val HEAT_STRESS = 0x0400
    }
}
