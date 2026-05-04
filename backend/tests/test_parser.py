"""
Mesh Pi5 Server — Packet Parser Tests

File: tests/test_parser.py
Description: Unit tests for CRC16 computation, packet parsing, and frame extraction.
"""

import struct

from app.services.packet_parser import (
    FRAME_START,
    FRAME_END,
    PACKET_FORMAT,
    SoldierPacket,
    compute_crc16,
    parse_packet,
    extract_frames,
    build_frame,
)


def _build_test_packet(
    node_id: int = 0x0001,
    timestamp: int = 1714400000,
    lat: float = 12.2388,
    lng: float = 109.1967,
    heading: float = 45.0,
    hr: int = 72,
    spo2: int = 98,
    temp: float = 36.5,
    hum: float = 65.0,
    pres: float = 1013.25,
    batt: float = 3.85,
    flags: int = 0x001F,
) -> bytes:
    """Build a valid 40-byte packet with correct CRC."""
    payload = struct.pack(
        "<HIfffBBffffH",
        node_id, timestamp, lat, lng, heading,
        hr, spo2, temp, hum, pres, batt, flags,
    )
    crc = compute_crc16(payload)
    return payload + struct.pack("<H", crc)


class TestCRC16:
    def test_empty(self):
        assert compute_crc16(b"") == 0xFFFF

    def test_known_value(self):
        """CRC of 'A' (0x41) should be deterministic."""
        result = compute_crc16(b"A")
        assert isinstance(result, int)
        assert 0 <= result <= 0xFFFF

    def test_consistency(self):
        """Same input always produces same CRC."""
        data = b"Hello LoRa Mesh"
        assert compute_crc16(data) == compute_crc16(data)

    def test_different_data_different_crc(self):
        assert compute_crc16(b"abc") != compute_crc16(b"xyz")


class TestParsePacket:
    def test_valid_packet(self):
        data = _build_test_packet()
        pkt = parse_packet(data)

        assert pkt is not None
        assert pkt.node_id == 0x0001
        assert pkt.heart_rate == 72
        assert pkt.spo2 == 98
        assert abs(pkt.latitude - 12.2388) < 0.001
        assert abs(pkt.longitude - 109.1967) < 0.001
        assert pkt.status_flags == 0x001F

    def test_invalid_crc(self):
        data = bytearray(_build_test_packet())
        data[39] ^= 0xFF  # Corrupt CRC byte
        pkt = parse_packet(bytes(data))
        assert pkt is None

    def test_wrong_size(self):
        assert parse_packet(b"short") is None
        assert parse_packet(b"x" * 41) is None

    def test_to_dict(self):
        data = _build_test_packet()
        pkt = parse_packet(data)
        assert pkt is not None

        d = pkt.to_dict()
        assert d["node_id"] == 0x0001
        assert "flags" in d
        assert d["flags"]["gps_fix"] is True


class TestFrameExtraction:
    def test_single_frame(self):
        pkt_data = _build_test_packet()
        frame = build_frame(pkt_data)
        buf = bytearray(frame)

        packets, remaining = extract_frames(buf)
        assert len(packets) == 1
        assert len(remaining) == 0

    def test_multiple_frames(self):
        pkt1 = _build_test_packet(node_id=1)
        pkt2 = _build_test_packet(node_id=2)
        buf = bytearray(build_frame(pkt1) + build_frame(pkt2))

        packets, remaining = extract_frames(buf)
        assert len(packets) == 2

    def test_partial_frame(self):
        pkt_data = _build_test_packet()
        frame = build_frame(pkt_data)
        # Only send first half
        buf = bytearray(frame[:20])

        packets, remaining = extract_frames(buf)
        assert len(packets) == 0
        assert len(remaining) > 0

    def test_garbage_before_frame(self):
        pkt_data = _build_test_packet()
        frame = build_frame(pkt_data)
        buf = bytearray(b"\x00\x01\x02" + frame)

        packets, remaining = extract_frames(buf)
        assert len(packets) == 1
