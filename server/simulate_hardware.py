#!/usr/bin/env python3
"""
Soldier Tracking System - Hardware Simulator
Simulates multiple soldier nodes sending telemetry to test the GUI without physical hardware.
"""

import struct
import time
import random
import math
import argparse
from typing import List, Tuple

# Packet constants
PACKET_SIZE = 40
PKT_START = 0xAA
PKT_END = 0x55

# Status flags
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

class VirtualSoldier:
    """Simulates a single soldier node with realistic movement and vitals."""
    
    def __init__(self, node_id: int, start_lat: float, start_lon: float):
        self.node_id = node_id
        self.latitude = start_lat
        self.longitude = start_lon
        self.heading = random.uniform(0, 360)
        self.velocity = random.uniform(0.5, 2.0)  # m/s (walking speed)
        
        # Health parameters
        self.base_hr = random.randint(60, 80)
        self.base_spo2 = random.randint(95, 99)
        self.base_temp = random.uniform(36.0, 37.5)
        
        # Environment parameters
        self.base_humidity = random.uniform(40.0, 70.0)
        self.base_pressure = random.uniform(1005.0, 1020.0)

        # Battery (starts between 3.3V and 4.2V)
        self.battery = random.uniform(3.5, 4.2)
        self.battery_drain = random.uniform(0.0001, 0.0003)  # V per second
        
        # Movement pattern
        self.movement_mode = random.choice(['patrol', 'stationary', 'tactical'])
        self.time_in_mode = 0
        self.mode_duration = random.uniform(30, 120)
        
        # Special states
        self.is_alerted = False
        self.is_down = False
        self.heat_stressed = False
        
        self.timestamp = 0
    
    def update(self, dt: float):
        """Update soldier state based on elapsed time."""
        self.timestamp += 1
        
        # Update movement mode
        self.time_in_mode += dt
        if self.time_in_mode > self.mode_duration:
            self.movement_mode = random.choice(['patrol', 'stationary', 'tactical'])
            self.time_in_mode = 0
            self.mode_duration = random.uniform(30, 120)
        
        # Update position based on mode
        if self.movement_mode == 'patrol':
            self.velocity = random.uniform(0.8, 1.5)
            self.heading += random.uniform(-10, 10)
        elif self.movement_mode == 'stationary':
            self.velocity = random.uniform(0, 0.2)
        elif self.movement_mode == 'tactical':
            self.velocity = random.uniform(1.5, 2.5)
            self.heading += random.uniform(-20, 20)
        
        # Normalize heading
        self.heading = self.heading % 360
        
        # Update position (simple flat-earth approximation for short distances)
        delta_lat = (self.velocity * dt * math.cos(math.radians(self.heading))) / 111320
        delta_lon = (self.velocity * dt * math.sin(math.radians(self.heading))) / (111320 * math.cos(math.radians(self.latitude)))
        
        self.latitude += delta_lat
        self.longitude += delta_lon
        
        # Update battery
        self.battery -= self.battery_drain * dt
        self.battery = max(2.8, self.battery)
        
        # Update vitals with some variation
        if self.movement_mode == 'tactical':
            hr_variation = random.randint(-5, 15)
        else:
            hr_variation = random.randint(-3, 5)
        
        self.heart_rate = max(50, min(200, self.base_hr + hr_variation))
        self.spo2 = max(85, min(100, self.base_spo2 + random.randint(-2, 1)))
        self.temperature = self.base_temp + random.uniform(-0.5, 1.0)
        self.humidity = self.base_humidity + random.uniform(-5.0, 5.0)
        self.pressure = self.base_pressure + random.uniform(-2.0, 2.0)
        
        # Random events (low probability)
        if random.random() < 0.001:  # 0.1% chance per update
            self.is_alerted = True
        
        if random.random() < 0.0005:  # 0.05% chance
            self.is_down = True
            self.velocity = 0
            self.movement_mode = 'stationary'
        
        if self.temperature > 38.5 and self.heart_rate > 100:
            self.heat_stressed = True
        else:
            self.heat_stressed = False
    
    def get_packet(self) -> bytes:
        """Generate a binary packet in the correct format."""
        status_flags = 0
        
        # Set status flags
        status_flags |= GPS_FIX | IMU_VALID | HR_VALID | SPO2_VALID | TEMP_VALID | HUMIDITY_VALID | PRESSURE_VALID
        
        if self.battery < 3.0:
            status_flags |= CRITICAL_BATTERY
        elif self.battery < 3.3:
            status_flags |= LOW_BATTERY
        
        if self.is_alerted:
            status_flags |= ALERT
        
        if self.is_down:
            status_flags |= MAN_DOWN
        
        if self.heat_stressed:
            status_flags |= HEAT_STRESS
        
        # Pack packet (little-endian)
        # Format: node_id(H), timestamp(I), lat(f), lon(f), heading(f),
        #         hr(B), spo2(B), temp(f), humidity(f), pressure(f), battery(f), flags(H)
        # Total: 2+4+4+4+4+1+1+4+4+4+4+2 = 38 bytes before CRC
        packet_data = struct.pack(
            '<HIfffBBffffH',
            self.node_id,           # uint16 - 2 bytes
            self.timestamp,         # uint32 - 4 bytes
            self.latitude,          # float - 4 bytes
            self.longitude,         # float - 4 bytes
            self.heading,           # float - 4 bytes
            int(self.heart_rate),   # uint8 - 1 byte
            int(self.spo2),         # uint8 - 1 byte
            self.temperature,       # float - 4 bytes
            self.humidity,          # float - 4 bytes
            self.pressure,          # float - 4 bytes
            self.battery,           # float - 4 bytes
            status_flags            # uint16 - 2 bytes
        )
        
        # Calculate CRC
        crc = self._crc16_ccitt(packet_data)
        packet_data += struct.pack('<H', crc)
        
        return packet_data
    
    @staticmethod
    def _crc16_ccitt(data: bytes) -> int:
        """Calculate CRC-16-CCITT checksum."""
        crc = 0xFFFF
        for byte in data:
            crc ^= byte << 8
            for _ in range(8):
                if crc & 0x8000:
                    crc = ((crc << 1) ^ 0x1021) & 0xFFFF
                else:
                    crc = (crc << 1) & 0xFFFF
        return crc


class HardwareSimulator:
    """Simulates the gateway hardware sending packets via serial."""
    
    def __init__(self, num_soldiers: int, center_lat: float, center_lon: float, spread_km: float):
        self.soldiers: List[VirtualSoldier] = []
        
        # Create soldiers in a random pattern around center point
        for i in range(1, num_soldiers + 1):
            # Random offset from center (within spread_km radius)
            angle = random.uniform(0, 2 * math.pi)
            distance = random.uniform(0, spread_km * 1000)  # meters
            
            lat_offset = (distance * math.cos(angle)) / 111320
            lon_offset = (distance * math.sin(angle)) / (111320 * math.cos(math.radians(center_lat)))
            
            soldier_lat = center_lat + lat_offset
            soldier_lon = center_lon + lon_offset
            
            self.soldiers.append(VirtualSoldier(i, soldier_lat, soldier_lon))
        
        print(f"Initialized {num_soldiers} virtual soldiers")
        print(f"Center: {center_lat:.6f}, {center_lon:.6f}")
        print(f"Spread: {spread_km} km radius")
    
    def send_packet(self, packet: bytes):
        """Send a packet in the correct framing format."""
        import sys
        frame = bytes([PKT_START]) + packet + bytes([PKT_END])
        sys.stdout.buffer.write(frame)
        sys.stdout.buffer.flush()
    
    def send_gps_position(self, lat: float, lon: float):
        """Send gateway GPS position."""
        import sys
        gps_msg = f"GW_GPS:{lat:.6f},{lon:.6f}\n"
        sys.stdout.write(gps_msg)
        sys.stdout.flush()
    
    def run(self, update_rate: float = 1.0):
        """Main simulation loop."""
        print(f"\nStarting simulation at {update_rate} Hz...")
        print("Pipe this output to the server's virtual serial port")
        print("Press Ctrl+C to stop\n")
        
        try:
            start_time = time.time()
            iteration = 0
            
            while True:
                current_time = time.time()
                dt = 1.0 / update_rate
                
                # Send gateway position (use first soldier's position as reference)
                if self.soldiers:
                    self.send_gps_position(self.soldiers[0].latitude, self.soldiers[0].longitude)
                
                # Update and send all soldiers
                for soldier in self.soldiers:
                    soldier.update(dt)
                    packet = soldier.get_packet()
                    self.send_packet(packet)
                
                iteration += 1
                
                # Print status every 10 iterations
                if iteration % 10 == 0:
                    elapsed = current_time - start_time
                    print(f"[{elapsed:.1f}s] Sent {iteration * len(self.soldiers)} packets from {len(self.soldiers)} soldiers", 
                          file=__import__('sys').stderr)
                
                # Sleep to maintain update rate
                next_time = start_time + (iteration * dt)
                sleep_time = next_time - time.time()
                if sleep_time > 0:
                    time.sleep(sleep_time)
                
        except KeyboardInterrupt:
            print("\n\nSimulation stopped", file=__import__('sys').stderr)


def main():
    parser = argparse.ArgumentParser(
        description='Simulate soldier hardware for testing the GUI',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Simulate 5 soldiers at 1 Hz
  python simulate_hardware.py --soldiers 5 --rate 1.0

  # Simulate 20 soldiers around a specific location
  python simulate_hardware.py --soldiers 20 --lat 42.36 --lon -71.05

  # Pipe to socat virtual serial port
  python simulate_hardware.py --soldiers 10 | socat - PTY,link=/tmp/vserial,raw,echo=0

  # Use with server (in separate terminal)
  python app.py --port /tmp/vserial --web-port 5000
        """
    )
    
    parser.add_argument('--soldiers', type=int, default=5,
                       help='Number of soldiers to simulate (default: 5)')
    parser.add_argument('--rate', type=float, default=1.0,
                       help='Update rate in Hz (default: 1.0)')
    parser.add_argument('--lat', type=float, default=42.36,
                       help='Center latitude (default: 42.36 - Boston)')
    parser.add_argument('--lon', type=float, default=-71.05,
                       help='Center longitude (default: -71.05 - Boston)')
    parser.add_argument('--spread', type=float, default=2.0,
                       help='Spread radius in km (default: 2.0)')
    
    args = parser.parse_args()
    
    # Validate arguments
    if args.soldiers < 1 or args.soldiers > 100:
        parser.error("Number of soldiers must be between 1 and 100")
    
    if args.rate <= 0 or args.rate > 10:
        parser.error("Update rate must be between 0 and 10 Hz")
    
    # Create and run simulator
    simulator = HardwareSimulator(args.soldiers, args.lat, args.lon, args.spread)
    simulator.run(args.rate)


if __name__ == '__main__':
    main()