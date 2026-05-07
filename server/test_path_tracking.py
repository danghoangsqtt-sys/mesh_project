#!/usr/bin/env python3
"""
Test script to verify path tracking feature
Creates a soldier that moves in a pattern to demonstrate path visualization
"""

import sys
import time
import threading
from soldier_manager import SoldierManager
from packet_handler import SoldierPacket
from flask import Flask, render_template, jsonify, request, Response

# Create soldier manager
soldier_manager = SoldierManager()

def create_moving_soldier():
    """Create a soldier that moves in a square pattern"""
    print("Creating soldier that moves in a square pattern...")
    
    # Starting position (Boston)
    base_lat = 42.3600
    base_lon = -71.0500
    
    # Movement pattern: square
    # Each step is approximately 50 meters
    movements = [
        (0.0005, 0),      # North
        (0.0005, 0),      # North
        (0.0005, 0),      # North
        (0, 0.0005),      # East
        (0, 0.0005),      # East
        (0, 0.0005),      # East
        (-0.0005, 0),     # South
        (-0.0005, 0),     # South
        (-0.0005, 0),     # South
        (0, -0.0005),     # West
        (0, -0.0005),     # West
        (0, -0.0005),     # West
    ]
    
    lat = base_lat
    lon = base_lon
    
    soldier_manager.update_gateway_position(base_lat, base_lon)
    
    timestamp = 0
    for idx, (dlat, dlon) in enumerate(movements):
        lat += dlat
        lon += dlon
        timestamp += 1
        
        # Calculate heading based on movement
        if dlat > 0:
            heading = 0.0  # North
        elif dlat < 0:
            heading = 180.0  # South
        elif dlon > 0:
            heading = 90.0  # East
        elif dlon < 0:
            heading = 270.0  # West
        else:
            heading = 0.0
        
        packet = SoldierPacket(
            node_id=1,
            timestamp=timestamp,
            latitude=lat,
            longitude=lon,
            heading=heading,
            heart_rate=75 + (idx % 10),  # Varying heart rate
            spo2=97,
            temperature=36.5,
            battery_voltage=3.8,
            status_flags=0x001F,
            crc16=0
        )
        
        soldier_manager.update_soldier(packet)
        print(f"Step {idx+1}/{len(movements)}: Position ({lat:.6f}, {lon:.6f}), Heading: {heading:.0f}°")
        time.sleep(2)  # Move every 2 seconds
    
    print("\nSquare pattern completed!")
    print(f"Total points in history: {len(soldier_manager.soldiers[1].position_history)}")
    
    # Continue with random small movements
    import random
    while True:
        lat += random.uniform(-0.0001, 0.0001)
        lon += random.uniform(-0.0001, 0.0001)
        timestamp += 1
        
        packet = SoldierPacket(
            node_id=1,
            timestamp=timestamp,
            latitude=lat,
            longitude=lon,
            heading=random.uniform(0, 360),
            heart_rate=random.randint(70, 85),
            spo2=97,
            temperature=36.5,
            battery_voltage=3.8,
            status_flags=0x001F,
            crc16=0
        )
        
        soldier_manager.update_soldier(packet)
        time.sleep(3)

# Flask app
app = Flask(__name__)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/soldiers')
def get_soldiers():
    return jsonify(soldier_manager.get_all_soldiers())

@app.route('/api/soldier/<int:node_id>')
def get_soldier(node_id):
    soldier = soldier_manager.get_soldier(node_id)
    if soldier:
        return jsonify(soldier.to_dict())
    return jsonify({'error': 'Soldier not found'}), 404

@app.route('/api/soldier/<int:node_id>/history')
def get_soldier_history(node_id):
    soldier = soldier_manager.get_soldier(node_id)
    if soldier:
        return jsonify(soldier.position_history)
    return jsonify({'error': 'Soldier not found'}), 404

@app.route('/api/soldier/<int:node_id>/kml')
def get_soldier_kml(node_id):
    kml = soldier_manager.get_soldier_kml(node_id)
    if kml:
        return Response(kml, mimetype='application/vnd.google-earth.kml+xml',
                       headers={'Content-Disposition': f'attachment; filename=soldier_{node_id}_path.kml'})
    return jsonify({'error': 'Soldier not found'}), 404

@app.route('/api/soldier/<int:node_id>/neighbors')
def get_neighbors(node_id):
    radius = request.args.get('radius', default=200.0, type=float)
    return jsonify(soldier_manager.get_nearest_neighbors(node_id, radius))

@app.route('/api/send_message', methods=['POST'])
def send_message():
    return jsonify({'success': True})

@app.route('/api/gateway')
def get_gateway():
    if soldier_manager.gateway_position:
        return jsonify(soldier_manager.gateway_position)
    return jsonify({'error': 'No gateway position'}), 404

if __name__ == '__main__':
    print("=" * 60)
    print("PATH TRACKING TEST SERVER")
    print("=" * 60)
    print("\nThis test creates a soldier that moves in a square pattern")
    print("to demonstrate the path tracking feature.")
    print("\nInstructions:")
    print("1. Open http://localhost:5000 in your browser")
    print("2. Wait for soldier to appear (2 seconds)")
    print("3. Click the 'Track' button on Soldier 1")
    print("4. Watch the path appear as the soldier moves")
    print("\nExpected behavior:")
    print("- Red line shows the path traveled")
    print("- Green marker = start position")
    print("- Red pulsing marker = current position")
    print("- Path statistics show points and distance")
    print("=" * 60)
    
    # Start movement simulation in background
    movement_thread = threading.Thread(target=create_moving_soldier, daemon=True)
    movement_thread.start()
    
    # Wait a moment for first position
    time.sleep(1)
    
    # Start Flask
    app.run(host='0.0.0.0', port=5000, debug=False, threaded=True)