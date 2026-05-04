"""
Mesh Pi5 Server — Packet Parser

File: services/packet_parser.py
Description: Binary packet parser/builder for the LoRa mesh protocol (40 bytes).
             Ported from Kotlin PacketParser to Python.
"""

import struct
from dataclasses import dataclass

# Frame markers
FRAME_START = 0xAA
FRAME_END = 0x55
PACKET_SIZE = 40
FRAME_SIZE = PACKET_SIZE + 2  # start + packet + end

# Struct format: little-endian, matches SoldierPacket layout
# H=uint16, I=uint32, f=float32, B=uint8
PACKET_FORMAT = "<HIfffBBffffHH"


@dataclass
class SoldierPacket:
    """Parsed soldier packet from LoRa mesh network."""

    node_id: int          # uint16
    timestamp: int        # uint32 (epoch)
    latitude: float       # float32
    longitude: float      # float32
    heading: float        # float32
    heart_rate: int       # uint8
    spo2: int             # uint8
    temperature: float    # float32
    humidity: float       # float32
    pressure: float       # float32
    battery_voltage: float  # float32
    status_flags: int     # uint16
    crc16: int            # uint16

    def to_dict(self) -> dict:
        """Convert to JSON-serializable dictionary."""
        return {
            "node_id": self.node_id,
            "timestamp": self.timestamp,
            "latitude": round(self.latitude, 6),
            "longitude": round(self.longitude, 6),
            "heading": round(self.heading, 1),
            "heart_rate": self.heart_rate,
            "spo2": self.spo2,
            "temperature": round(self.temperature, 1),
            "humidity": round(self.humidity, 1),
            "pressure": round(self.pressure, 1),
            "battery_voltage": round(self.battery_voltage, 2),
            "status_flags": self.status_flags,
            "flags": self._decode_flags(),
        }

    def _decode_flags(self) -> dict[str, bool]:
        """Decode status flags bitmap into named booleans."""
        f = self.status_flags
        return {
            "gps_fix": bool(f & 0x0001),
            "imu_valid": bool(f & 0x0002),
            "hr_valid": bool(f & 0x0004),
            "spo2_valid": bool(f & 0x0008),
            "temp_valid": bool(f & 0x0010),
            "low_battery": bool(f & 0x0020),
            "critical_battery": bool(f & 0x0040),
            "sensor_error": bool(f & 0x0080),
            "alert": bool(f & 0x0100),
            "man_down": bool(f & 0x0200),
            "heat_stress": bool(f & 0x0400),
            "humidity_valid": bool(f & 0x0800),
            "pressure_valid": bool(f & 0x1000),
        }


def compute_crc16(data: bytes) -> int:
    """Compute CRC16-CCITT over data bytes.

    Polynomial: 0x1021, Initial: 0xFFFF.
    """
    crc = 0xFFFF
    for byte in data:
        crc ^= byte << 8
        for _ in range(8):
            if crc & 0x8000:
                crc = (crc << 1) ^ 0x1021
            else:
                crc = crc << 1
            crc &= 0xFFFF
    return crc


def parse_packet(data: bytes) -> SoldierPacket | None:
    """Parse a 40-byte SoldierPacket from raw bytes.

    Args:
        data: Exactly 40 bytes of packet data (without frame markers).

    Returns:
        SoldierPacket if CRC is valid, None otherwise.
    """
    if len(data) != PACKET_SIZE:
        return None

    # Validate CRC: compute over first 38 bytes, compare with last 2
    payload = data[:38]
    expected_crc = struct.unpack_from("<H", data, 38)[0]
    actual_crc = compute_crc16(payload)

    if actual_crc != expected_crc:
        return None

    # Unpack all fields
    fields = struct.unpack(PACKET_FORMAT, data)
    return SoldierPacket(*fields)


def extract_frames(buffer: bytearray) -> tuple[list[bytes], bytearray]:
    """Extract complete frames from a byte buffer.

    Scans for 0xAA...0x55 frame markers and extracts 40-byte packets.

    Args:
        buffer: Incoming serial byte buffer (mutable).

    Returns:
        Tuple of (list of extracted packet payloads, remaining buffer).
    """
    packets: list[bytes] = []
    i = 0

    while i <= len(buffer) - FRAME_SIZE:
        if buffer[i] == FRAME_START and buffer[i + FRAME_SIZE - 1] == FRAME_END:
            packet_data = bytes(buffer[i + 1 : i + 1 + PACKET_SIZE])
            packets.append(packet_data)
            i += FRAME_SIZE
        else:
            i += 1

    return packets, bytearray(buffer[i:])


def build_frame(packet_data: bytes) -> bytes:
    """Wrap packet data in frame markers (0xAA + data + 0x55)."""
    return bytes([FRAME_START]) + packet_data + bytes([FRAME_END])
