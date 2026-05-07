import serial
import threading
import time
from typing import Callable, Optional
from packet_handler import parse_packet, create_packet, PKT_START, PKT_END, PACKET_SIZE

class SerialCommunicator:
    def __init__(self, port: str, baudrate: int = 115200):
        self.port = port
        self.baudrate = baudrate
        self.serial_conn: Optional[serial.Serial] = None
        self.running = False
        self.read_thread: Optional[threading.Thread] = None
        self.packet_callback: Optional[Callable] = None
        self.gps_callback: Optional[Callable] = None
        self.raw_serial_callback: Optional[Callable] = None
        self.buffer = bytearray()
        
    def connect(self) -> bool:
        if self.port == 'MOCK':
            print("Running in MOCK mode (no actual serial connection)")
            return True
            
        try:
            self.serial_conn = serial.Serial(
                self.port,
                self.baudrate,
                timeout=0.1
            )
            time.sleep(2)
            return True
        except serial.SerialException as e:
            print(f"Serial connection failed: {e}")
            return False
    
    def disconnect(self):
        self.running = False
        if self.read_thread:
            self.read_thread.join(timeout=2)
        if self.serial_conn and self.serial_conn.is_open:
            self.serial_conn.close()
    
    def set_packet_callback(self, callback: Callable):
        self.packet_callback = callback
    
    def set_gps_callback(self, callback: Callable):
        self.gps_callback = callback
        
    def set_raw_serial_callback(self, callback: Callable):
        self.raw_serial_callback = callback
    
    def start_reading(self):
        if self.port == 'MOCK':
            self.running = True
            return True
            
        if not self.serial_conn or not self.serial_conn.is_open:
            return False
        
        self.running = True
        self.read_thread = threading.Thread(target=self._read_loop, daemon=True)
        self.read_thread.start()
        return True
    
    def _read_loop(self):
        while self.running:
            try:
                if self.serial_conn.in_waiting > 0:
                    data = self.serial_conn.read(self.serial_conn.in_waiting)
                    if self.raw_serial_callback:
                        self.raw_serial_callback(f"RX: {data.hex(' ').upper()}")
                    self.buffer.extend(data)
                    self._process_buffer()
                else:
                    time.sleep(0.01)
            except serial.SerialException as e:
                print(f"Serial read error: {e}")
                time.sleep(0.1)
    
    def _process_buffer(self):
        while True:
            if len(self.buffer) < 10:
                break
            
            # Check for GPS message first
            if self.buffer[0:7] == b'GW_GPS:':
                newline_idx = self.buffer.find(b'\n')
                if newline_idx != -1:
                    gps_line = self.buffer[7:newline_idx].decode('utf-8', errors='ignore')
                    self.buffer = self.buffer[newline_idx+1:]
                    
                    if self.gps_callback:
                        try:
                            lat_str, lon_str = gps_line.split(',')
                            lat = float(lat_str)
                            lon = float(lon_str)
                            print(f"DEBUG: Gateway GPS: {lat:.6f}, {lon:.6f}")
                            self.gps_callback(lat, lon)
                        except (ValueError, IndexError) as e:
                            print(f"DEBUG: GPS parse error: {e}")
                    continue
                else:
                    break
            
            # Look for packet start marker
            start_idx = self.buffer.find(PKT_START)
            if start_idx == -1:
                self.buffer.clear()
                break
            
            if start_idx > 0:
                print(f"DEBUG: Skipping {start_idx} bytes before packet start")
                self.buffer = self.buffer[start_idx:]
            
            if len(self.buffer) < PACKET_SIZE + 2:
                break
            
            if self.buffer[PACKET_SIZE + 1] == PKT_END:
                packet_data = bytes(self.buffer[1:PACKET_SIZE + 1])
                self.buffer = self.buffer[PACKET_SIZE + 2:]
                
                print(f"DEBUG: Received packet, size: {len(packet_data)} bytes")
                packet = parse_packet(packet_data)
                if packet:
                    print(f"DEBUG: Valid packet from soldier {packet.node_id}")
                    if self.packet_callback:
                        self.packet_callback(packet)
                else:
                    print(f"DEBUG: Invalid packet (CRC failed or parse error)")
            else:
                print(f"DEBUG: Expected end marker 0x55, got 0x{self.buffer[PACKET_SIZE + 1]:02X}")
                self.buffer = self.buffer[1:]
    
    def send_packet(self, node_id: int, message: str = "") -> bool:
        if not self.serial_conn or not self.serial_conn.is_open:
            return False

        try:
            packet_data = create_packet(node_id, message)
            # create_packet returns 32 bytes starting with 0x5A 0xA5 — send raw,
            # do NOT wrap in PKT_START/PKT_END (0xAA/0x55) because the gateway
            # dispatches on the first byte: 0x5A triggers the command path,
            # 0xAA triggers the SoldierPacket path which requires 40 bytes.
            if self.raw_serial_callback:
                self.raw_serial_callback(f"TX: {packet_data.hex(' ').upper()}")
            self.serial_conn.write(packet_data)
            return True
        except serial.SerialException as e:
            print(f"Serial write error: {e}")
            return False