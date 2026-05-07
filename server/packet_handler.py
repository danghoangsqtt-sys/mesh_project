import struct
from dataclasses import dataclass
from typing import Optional

PACKET_SIZE = 40
PKT_START = 0xAA
PKT_END = 0x55

class StatusFlags:
    GPS_FIX = 0x0001
    IMU_VALID = 0x0002
    HR_VALID = 0x0004
    SPO2_VALID = 0x0008
    TEMP_VALID = 0x0010
    LOW_BATTERY = 0x0020
    CRITICAL_BATTERY = 0x0040
    SENSOR_ERROR = 0x0080
    ALERT = 0x0100
    MAN_DOWN = 0x0200
    HEAT_STRESS = 0x0400
    HUMIDITY_VALID = 0x0800
    PRESSURE_VALID = 0x1000

@dataclass
class SoldierPacket:
    node_id: int
    timestamp: int
    latitude: float
    longitude: float
    heading: float
    heart_rate: int
    spo2: int
    temperature: float
    humidity: float
    pressure: float
    battery_voltage: float
    status_flags: int
    crc16: int

    def to_dict(self):
        return {
            'nodeId': self.node_id,
            'timestamp': self.timestamp,
            'latitude': self.latitude,
            'longitude': self.longitude,
            'heading': self.heading,
            'heartRate': self.heart_rate,
            'spo2': self.spo2,
            'temperature': self.temperature,
            'humidity': self.humidity,
            'pressure': self.pressure,
            'batteryVoltage': self.battery_voltage,
            'statusFlags': self.status_flags,
            'gpsFix': bool(self.status_flags & StatusFlags.GPS_FIX),
            'imuValid': bool(self.status_flags & StatusFlags.IMU_VALID),
            'hrValid': bool(self.status_flags & StatusFlags.HR_VALID),
            'spo2Valid': bool(self.status_flags & StatusFlags.SPO2_VALID),
            'tempValid': bool(self.status_flags & StatusFlags.TEMP_VALID),
            'humidityValid': bool(self.status_flags & StatusFlags.HUMIDITY_VALID),
            'pressureValid': bool(self.status_flags & StatusFlags.PRESSURE_VALID),
            'lowBattery': bool(self.status_flags & StatusFlags.LOW_BATTERY),
            'criticalBattery': bool(self.status_flags & StatusFlags.CRITICAL_BATTERY),
            'alert': bool(self.status_flags & StatusFlags.ALERT),
            'manDown': bool(self.status_flags & StatusFlags.MAN_DOWN),
            'heatStress': bool(self.status_flags & StatusFlags.HEAT_STRESS)
        }

def crc16_ccitt(data: bytes) -> int:
    crc = 0xFFFF
    for byte in data:
        crc ^= byte << 8
        for _ in range(8):
            if crc & 0x8000:
                crc = ((crc << 1) ^ 0x1021) & 0xFFFF
            else:
                crc = (crc << 1) & 0xFFFF
    return crc

def parse_packet(data: bytes) -> Optional[SoldierPacket]:
    if len(data) != PACKET_SIZE:
        return None
    
    try:
        # Format: H I fff BB ffff H H
        # node_id(2) timestamp(4) lat(4) lon(4) heading(4) hr(1) spo2(1) temp(4) humidity(4) pressure(4) batt(4) flags(2) crc(2)
        unpacked = struct.unpack('<HIfffBBffffHH', data)

        node_id, timestamp, latitude, longitude, heading, \
        heart_rate, spo2, temperature, humidity, pressure, battery_voltage, \
        status_flags, crc16 = unpacked
        
        calculated_crc = crc16_ccitt(data[:PACKET_SIZE-2])
        if calculated_crc != crc16:
            return None
        
        return SoldierPacket(
            node_id=node_id,
            timestamp=timestamp,
            latitude=latitude,
            longitude=longitude,
            heading=heading,
            heart_rate=heart_rate,
            spo2=spo2,
            temperature=temperature,
            humidity=humidity,
            pressure=pressure,
            battery_voltage=battery_voltage,
            status_flags=status_flags,
            crc16=crc16
        )
    except struct.error:
        return None

def create_packet(node_id: int, message: str = "") -> bytes:
    """
    Create a message packet for sending commands to soldier nodes
    
    Message packet format (32 bytes):
    - Byte 0: Packet type (0xFF for message)
    - Byte 1: Target node ID (0 = broadcast)
    - Bytes 2-29: Message text (28 bytes, null-terminated)
    - Bytes 30-31: CRC16
    """
    # Create message packet
    packet = bytearray(32)
    packet[0] = 0x5A  # Magic byte 1
    packet[1] = 0xA5  # Magic byte 2
    packet[2] = node_id  # Target node ID (0 = broadcast)
    
    # Encode message (max 27 bytes)
    msg_bytes = message.encode('utf-8')[:27]
    packet[3:3+len(msg_bytes)] = msg_bytes
    # Null-terminate if space remains
    if len(msg_bytes) < 27:
        packet[3+len(msg_bytes)] = 0
    
    # Calculate CRC on first 30 bytes
    crc = crc16_ccitt(bytes(packet[:30]))
    packet[30] = crc & 0xFF
    packet[31] = (crc >> 8) & 0xFF
    
    return bytes(packet)