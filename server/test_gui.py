#!/usr/bin/env python3
"""
Test script for Windows COM0COM virtual ports
Sends simulated soldier data directly to a COM port
"""

import sys
import time
import serial
import argparse

# Add parent directory to path to import simulator
sys.path.insert(0, '.')
from simulate_hardware import HardwareSimulator

def main():
    parser = argparse.ArgumentParser(description='Send simulated data to COM port')
    parser.add_argument('--port', required=True, help='COM port (e.g., COM10)')
    parser.add_argument('--soldiers', type=int, default=5, help='Number of soldiers')
    parser.add_argument('--rate', type=float, default=1.0, help='Update rate in Hz')
    parser.add_argument('--lat', type=float, default=42.36, help='Center latitude')
    parser.add_argument('--lon', type=float, default=-71.05, help='Center longitude')
    parser.add_argument('--spread', type=float, default=2.0, help='Spread in km')
    
    args = parser.parse_args()
    
    print(f"Opening {args.port}...")
    try:
        ser = serial.Serial(args.port, 115200, timeout=1)
        print(f"✓ Connected to {args.port}")
    except serial.SerialException as e:
        print(f"✗ Failed to open {args.port}: {e}")
        print("\nTroubleshooting:")
        print("1. Check Device Manager that port exists")
        print("2. Ensure no other program is using the port")
        print("3. Try a different COM port number")
        sys.exit(1)
    
    # Create simulator
    simulator = HardwareSimulator(args.soldiers, args.lat, args.lon, args.spread)
    
    print(f"\nSending data at {args.rate} Hz...")
    print("Press Ctrl+C to stop\n")
    
    try:
        start_time = time.time()
        iteration = 0
        
        while True:
            current_time = time.time()
            dt = 1.0 / args.rate
            
            # Send gateway position
            if simulator.soldiers:
                gps_msg = f"GW_GPS:{simulator.soldiers[0].latitude:.6f},{simulator.soldiers[0].longitude:.6f}\n"
                ser.write(gps_msg.encode('utf-8'))
            
            # Update and send all soldiers
            for soldier in simulator.soldiers:
                soldier.update(dt)
                packet = soldier.get_packet()
                
                # Send with framing
                frame = bytes([0xAA]) + packet + bytes([0x55])
                ser.write(frame)
            
            iteration += 1
            
            # Print status
            if iteration % 10 == 0:
                elapsed = current_time - start_time
                print(f"[{elapsed:.1f}s] Sent {iteration * len(simulator.soldiers)} packets")
            
            # Sleep to maintain rate
            next_time = start_time + (iteration * dt)
            sleep_time = next_time - time.time()
            if sleep_time > 0:
                time.sleep(sleep_time)
                
    except KeyboardInterrupt:
        print("\n\nStopped")
    finally:
        ser.close()
        print(f"Closed {args.port}")

if __name__ == '__main__':
    main()