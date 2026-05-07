#!/usr/bin/env python3
"""
Debug script to test packet parsing and serial communication
"""

import sys
import struct
import serial
import time

def crc16_ccitt(data: bytes) -> int:
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

def create_test_packet(node_id=1):
    """Create a single test packet"""
    # Pack packet data
    packet_data = struct.pack(
        '<HIfffBBffH',
        node_id,        # uint16 - node ID
        1000,           # uint32 - timestamp
        42.3600,        # float - latitude
        -71.0500,       # float - longitude
        180.0,          # float - heading
        72,             # uint8 - heart rate
        98,             # uint8 - spo2
        20.0,           # float - temperature
        3.7,            # float - battery
        0x001F          # uint16 - status flags (all valid)
    )
    
    # Calculate and append CRC
    crc = crc16_ccitt(packet_data)
    packet_data += struct.pack('<H', crc)
    
    return packet_data

def test_packet_creation():
    """Test 1: Verify packet creation"""
    print("=" * 60)
    print("TEST 1: Packet Creation")
    print("=" * 60)
    
    packet = create_test_packet(1)
    print(f"Packet size: {len(packet)} bytes (expected: 32)")
    print(f"Packet hex: {packet.hex()}")
    
    # Parse it back
    try:
        unpacked = struct.unpack('<HIfffBBffHH', packet)
        print(f"\nParsed packet:")
        print(f"  Node ID: {unpacked[0]}")
        print(f"  Timestamp: {unpacked[1]}")
        print(f"  Latitude: {unpacked[2]:.6f}")
        print(f"  Longitude: {unpacked[3]:.6f}")
        print(f"  Heading: {unpacked[4]:.1f}")
        print(f"  HR: {unpacked[5]}")
        print(f"  SpO2: {unpacked[6]}")
        print(f"  Temp: {unpacked[7]:.1f}")
        print(f"  Battery: {unpacked[8]:.2f}")
        print(f"  Flags: 0x{unpacked[9]:04X}")
        print(f"  CRC: 0x{unpacked[10]:04X}")
        
        # Verify CRC
        calc_crc = crc16_ccitt(packet[:30])
        print(f"\nCRC Check: {'✓ PASS' if calc_crc == unpacked[10] else '✗ FAIL'}")
        
        return True
    except Exception as e:
        print(f"✗ FAIL: {e}")
        return False

def test_serial_write_read(port1, port2):
    """Test 2: Serial communication"""
    print("\n" + "=" * 60)
    print("TEST 2: Serial Communication")
    print("=" * 60)
    
    print(f"Opening {port1} for writing...")
    try:
        ser_write = serial.Serial(port1, 115200, timeout=1)
        print(f"✓ Opened {port1}")
    except Exception as e:
        print(f"✗ Failed to open {port1}: {e}")
        return False
    
    print(f"Opening {port2} for reading...")
    try:
        ser_read = serial.Serial(port2, 115200, timeout=2)
        print(f"✓ Opened {port2}")
    except Exception as e:
        print(f"✗ Failed to open {port2}: {e}")
        ser_write.close()
        return False
    
    # Send GPS message
    print("\nSending GPS message...")
    gps_msg = b"GW_GPS:42.360000,-71.050000\n"
    ser_write.write(gps_msg)
    ser_write.flush()
    print(f"Sent: {gps_msg.decode().strip()}")
    
    time.sleep(0.5)
    
    # Send packet
    print("\nSending test packet...")
    packet = create_test_packet(1)
    frame = bytes([0xAA]) + packet + bytes([0x55])
    ser_write.write(frame)
    ser_write.flush()
    print(f"Sent: {len(frame)} bytes (0xAA + 32 packet + 0x55)")
    print(f"Frame hex: {frame[:10].hex()}...{frame[-10:].hex()}")
    
    time.sleep(0.5)
    
    # Try to read
    print("\nReading from receive port...")
    data = ser_read.read(1000)
    print(f"Received: {len(data)} bytes")
    
    if len(data) > 0:
        print(f"Data hex: {data[:50].hex()}...")
        print(f"Data preview: {data[:100]}")
        
        # Check for GPS message
        if b'GW_GPS' in data:
            print("✓ GPS message received")
        else:
            print("✗ GPS message NOT found")
        
        # Check for packet frame
        if 0xAA in data and 0x55 in data:
            print("✓ Packet framing detected")
            
            # Try to extract packet
            start_idx = data.find(0xAA)
            if start_idx != -1 and len(data) >= start_idx + 34:
                received_packet = data[start_idx+1:start_idx+33]
                print(f"Extracted packet: {len(received_packet)} bytes")
                
                # Verify CRC
                calc_crc = crc16_ccitt(received_packet[:30])
                recv_crc = struct.unpack('<H', received_packet[30:32])[0]
                print(f"CRC: calculated=0x{calc_crc:04X}, received=0x{recv_crc:04X}")
                if calc_crc == recv_crc:
                    print("✓ CRC valid")
                else:
                    print("✗ CRC mismatch")
        else:
            print("✗ Packet framing NOT detected")
    else:
        print("✗ No data received")
    
    ser_write.close()
    ser_read.close()
    print("\nPorts closed")
    
    return len(data) > 0

def test_simulator_output(port):
    """Test 3: Capture simulator output"""
    print("\n" + "=" * 60)
    print("TEST 3: Simulator Output Test")
    print("=" * 60)
    print(f"Instructions:")
    print(f"1. In another terminal, run:")
    print(f"   python test_com_port.py --port {port} --soldiers 2 --rate 1.0")
    print(f"2. Press Enter here when simulator is running...")
    input()
    
    print(f"\nOpening {port} for reading...")
    try:
        ser = serial.Serial(port, 115200, timeout=5)
        print(f"✓ Opened {port}")
    except Exception as e:
        print(f"✗ Failed: {e}")
        return False
    
    print("Reading for 10 seconds...")
    start_time = time.time()
    total_bytes = 0
    gps_count = 0
    packet_count = 0
    
    while time.time() - start_time < 10:
        data = ser.read(1000)
        total_bytes += len(data)
        
        # Count GPS messages
        gps_count += data.count(b'GW_GPS')
        
        # Count packets (0xAA markers)
        packet_count += data.count(0xAA)
        
        if len(data) > 0:
            print(f"[{time.time()-start_time:.1f}s] Received {len(data)} bytes")
    
    ser.close()
    
    print(f"\nResults:")
    print(f"  Total bytes: {total_bytes}")
    print(f"  GPS messages: {gps_count}")
    print(f"  Packets (0xAA markers): {packet_count}")
    print(f"  Expected: ~10 GPS messages, ~20 packets (2 soldiers × 10 seconds)")
    
    if packet_count > 0:
        print("✓ Simulator is sending data")
        return True
    else:
        print("✗ No packets received")
        return False

def main():
    import argparse
    
    parser = argparse.ArgumentParser(description='Debug serial communication')
    parser.add_argument('--test', choices=['packet', 'serial', 'simulator', 'all'], 
                       default='all', help='Which test to run')
    parser.add_argument('--write-port', default='COM10', help='Port to write to')
    parser.add_argument('--read-port', default='COM11', help='Port to read from')
    
    args = parser.parse_args()
    
    print("SOLDIER TRACKING SYSTEM - DEBUG TOOL")
    print("=" * 60)
    
    results = []
    
    if args.test in ['packet', 'all']:
        results.append(('Packet Creation', test_packet_creation()))
    
    if args.test in ['serial', 'all']:
        results.append(('Serial Communication', test_serial_write_read(args.write_port, args.read_port)))
    
    if args.test in ['simulator', 'all']:
        results.append(('Simulator Output', test_simulator_output(args.read_port)))
    
    # Summary
    print("\n" + "=" * 60)
    print("TEST SUMMARY")
    print("=" * 60)
    for test_name, passed in results:
        status = "✓ PASS" if passed else "✗ FAIL"
        print(f"{test_name:30} {status}")
    
    all_passed = all(r[1] for r in results)
    print("\n" + ("=" * 60))
    if all_passed:
        print("ALL TESTS PASSED ✓")
        print("\nYour setup should work. If server still shows 0 soldiers:")
        print("1. Check server console for errors")
        print("2. Check browser console (F12) for errors")
        print("3. Verify firewall isn't blocking port 5000")
    else:
        print("SOME TESTS FAILED ✗")
        print("\nTroubleshooting:")
        print("1. Ensure COM ports are created: setupc list")
        print("2. Close all programs using the ports")
        print("3. Try different COM port numbers")
        print("4. Check Windows Device Manager")

if __name__ == '__main__':
    main()