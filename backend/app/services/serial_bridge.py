"""
Mesh Pi5 Server — Serial Bridge

File: services/serial_bridge.py
Description: Background thread reading serial data from LoRa Gateway,
             bridging to async context via asyncio.Queue.
"""

import asyncio
import logging
import threading
from typing import Callable

import serial

from app.config import settings
from app.services.packet_parser import SoldierPacket, extract_frames, parse_packet

logger = logging.getLogger(__name__)


class SerialBridge:
    """Manages serial connection to LoRa Gateway in a background thread.

    Reads raw bytes from USB serial, extracts frames, parses packets,
    and pushes them into an asyncio.Queue for WebSocket broadcasting.
    """

    def __init__(self):
        self._serial: serial.Serial | None = None
        self._thread: threading.Thread | None = None
        self._running = False
        self._buffer = bytearray()
        self._loop: asyncio.AbstractEventLoop | None = None

        # Public queue for consumers (WebSocket, Repository)
        self.packet_queue: asyncio.Queue[SoldierPacket] = asyncio.Queue(maxsize=500)

        # Gateway GPS sideband data
        self.gateway_gps: dict[str, float] = {"latitude": 0.0, "longitude": 0.0}

        # Connection status
        self.is_connected: bool = False

        # Callbacks for event processing
        self._on_packet_callbacks: list[Callable] = []

    async def start(self):
        """Start the serial reader background thread."""
        self._loop = asyncio.get_running_loop()
        self._running = True
        self._thread = threading.Thread(target=self._reader_loop, daemon=True, name="serial-reader")
        self._thread.start()
        logger.info("Serial bridge started (port=%s, baud=%d)", settings.SERIAL_PORT, settings.SERIAL_BAUDRATE)

    async def stop(self):
        """Stop the serial reader thread and close the port."""
        self._running = False
        if self._thread and self._thread.is_alive():
            self._thread.join(timeout=5.0)
        if self._serial and self._serial.is_open:
            self._serial.close()
        self.is_connected = False
        logger.info("Serial bridge stopped.")

    def _connect(self) -> bool:
        """Attempt to open the serial port. Returns True on success."""
        try:
            self._serial = serial.Serial(
                port=settings.SERIAL_PORT,
                baudrate=settings.SERIAL_BAUDRATE,
                timeout=settings.SERIAL_TIMEOUT,
                bytesize=serial.EIGHTBITS,
                parity=serial.PARITY_NONE,
                stopbits=serial.STOPBITS_ONE,
            )
            self.is_connected = True
            logger.info("Serial port opened: %s", settings.SERIAL_PORT)
            return True
        except serial.SerialException as e:
            self.is_connected = False
            logger.warning("Failed to open serial port %s: %s", settings.SERIAL_PORT, e)
            return False

    def _reader_loop(self):
        """Background thread: continuously read serial data and parse packets."""
        while self._running:
            # Auto-reconnect logic
            if not self._serial or not self._serial.is_open:
                if not self._connect():
                    import time
                    time.sleep(settings.SERIAL_RECONNECT_DELAY)
                    continue

            try:
                # Read available bytes
                if self._serial.in_waiting > 0:
                    raw = self._serial.read(self._serial.in_waiting)
                    self._process_raw_bytes(raw)
                else:
                    import time
                    time.sleep(0.01)  # Small sleep to avoid busy-wait

            except Exception as e:
                logger.error("Serial read error: %s. Reconnecting...", e)
                self.is_connected = False
                if self._serial and self._serial.is_open:
                    self._serial.close()
                self._serial = None
                import time
                time.sleep(settings.SERIAL_RECONNECT_DELAY)

    def _process_raw_bytes(self, raw: bytes):
        """Process raw serial bytes: check for Gateway GPS sideband or binary packets."""
        self._buffer.extend(raw)

        # 1. Process Gateway GPS text without corrupting binary packets
        while b"GW_GPS:" in self._buffer:
            start_idx = self._buffer.find(b"GW_GPS:")
            end_idx = self._buffer.find(b"\n", start_idx)
            
            if end_idx != -1:
                # Complete line found
                line_bytes = self._buffer[start_idx:end_idx]
                try:
                    line = line_bytes.decode("ascii", errors="ignore").strip()
                    self._parse_gateway_gps(line)
                except Exception:
                    pass
                # Remove this processed text from the buffer
                del self._buffer[start_idx:end_idx + 1]
            else:
                # Wait for the rest of the line
                break

        # 2. Extract binary frames
        packets_data, self._buffer = extract_frames(self._buffer)

        for pkt_bytes in packets_data:
            packet = parse_packet(pkt_bytes)
            if packet:
                self._enqueue_packet(packet)
            else:
                logger.warning(
                    "Frame dropped: CRC mismatch or bad size (%d bytes) | raw=%s",
                    len(pkt_bytes), pkt_bytes[:8].hex()
                )

    def _parse_gateway_gps(self, line: str):
        """Parse Gateway GPS sideband: 'GW_GPS:<lat>,<lng>'."""
        try:
            coords = line[7:]  # Strip "GW_GPS:"
            lat_str, lng_str = coords.split(",")
            self.gateway_gps = {
                "latitude": float(lat_str),
                "longitude": float(lng_str),
            }
        except (ValueError, IndexError):
            logger.warning("Invalid Gateway GPS line: %s", line)

    def _enqueue_packet(self, packet: SoldierPacket):
        """Thread-safe enqueue of parsed packet to the async queue."""
        logger.info(f"Parsed packet for Node ID: {packet.node_id}")
        if self._loop:
            self._loop.call_soon_threadsafe(self._safe_put, packet)

    def _safe_put(self, packet: SoldierPacket):
        """Put packet into queue, dropping oldest if full (backpressure)."""
        if self.packet_queue.full():
            try:
                self.packet_queue.get_nowait()  # Drop oldest
            except asyncio.QueueEmpty:
                pass
        self.packet_queue.put_nowait(packet)

    async def write(self, data: bytes) -> bool:
        """Write data to the serial port (for sending commands to Gateway).

        Args:
            data: Raw bytes to send (should be a framed packet).

        Returns:
            True if write succeeded, False otherwise.
        """
        if not self._serial or not self._serial.is_open:
            logger.warning("Cannot write: serial port not connected.")
            return False
        try:
            self._serial.write(data)
            self._serial.flush()
            return True
        except serial.SerialException as e:
            logger.error("Serial write error: %s", e)
            return False


# Singleton instance
serial_bridge = SerialBridge()
