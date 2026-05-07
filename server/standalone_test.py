#!/usr/bin/env python3
"""
Standalone test - inject simulated data directly into the server
No COM ports needed!
"""

import sys
import time
import threading
from soldier_manager import SoldierManager
from simulate_hardware import VirtualSoldier

# Create global soldier manager
soldier_manager = SoldierManager()

def simulate_soldiers(num_soldiers=5, duration=300):
    """Simulate soldiers and update manager directly"""
    print(f"Starting simulation with {num_soldiers} soldiers...")
    
    # Create virtual soldiers
    soldiers = []
    for i in range(1, num_soldiers + 1):
        soldier = VirtualSoldier(i, 42.36 + (i * 0.001), -71.05 + (i * 0.001))
        soldiers.append(soldier)
    
    # Update gateway position
    soldier_manager.update_gateway_position(42.36, -71.05)
    print("Gateway position set")
    
    # Simulation loop
    start_time = time.time()
    iteration = 0
    
    while time.time() - start_time < duration:
        # Update all soldiers
        for soldier in soldiers:
            soldier.update(1.0)
            
            # Create packet structure manually
            from packet_handler import SoldierPacket
            packet = SoldierPacket(
                node_id=soldier.node_id,
                timestamp=soldier.timestamp,
                latitude=soldier.latitude,
                longitude=soldier.longitude,
                heading=soldier.heading,
                heart_rate=int(soldier.heart_rate),
                spo2=int(soldier.spo2),
                temperature=soldier.temperature,
                battery_voltage=soldier.battery,
                status_flags=0x001F,  # All sensors valid
                crc16=0  # Not needed for direct injection
            )
            
            # Update soldier manager directly
            soldier_manager.update_soldier(packet)
        
        iteration += 1
        if iteration % 10 == 0:
            print(f"[{time.time()-start_time:.1f}s] Updated {num_soldiers} soldiers, total tracked: {len(soldier_manager.soldiers)}")
        
        time.sleep(1.0)
    
    print("Simulation completed")

# Start Flask app
from flask import Flask, render_template, jsonify, request, Response

app = Flask(__name__)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/soldiers')
def get_soldiers():
    soldiers = soldier_manager.get_all_soldiers()
    return jsonify(soldiers)

@app.route('/api/soldier/<int:node_id>')
def get_soldier(node_id):
    soldier = soldier_manager.get_soldier(node_id)
    if soldier:
        return jsonify(soldier.to_dict())
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
    neighbors = soldier_manager.get_nearest_neighbors(node_id, radius)
    return jsonify(neighbors)

@app.route('/api/send_message', methods=['POST'])
def send_message():
    data = request.json
    print(f"Message request: {data}")
    return jsonify({'success': True})

@app.route('/api/gateway')
def get_gateway():
    if soldier_manager.gateway_position:
        return jsonify(soldier_manager.gateway_position)
    return jsonify({'error': 'No gateway position'}), 404

if __name__ == '__main__':
    import argparse
    
    parser = argparse.ArgumentParser(description='Standalone test server (no hardware needed)')
    parser.add_argument('--soldiers', type=int, default=10, help='Number of soldiers to simulate')
    parser.add_argument('--port', type=int, default=5000, help='Web server port')
    parser.add_argument('--host', type=str, default='0.0.0.0', help='Web server host')
    
    args = parser.parse_args()
    
    print("=" * 60)
    print("STANDALONE TEST SERVER - NO HARDWARE REQUIRED")
    print("=" * 60)
    print(f"Simulating {args.soldiers} soldiers")
    print(f"Web server: http://localhost:{args.port}")
    print("=" * 60)
    
    # Start simulation in background thread
    sim_thread = threading.Thread(target=simulate_soldiers, args=(args.soldiers, 3600), daemon=True)
    sim_thread.start()
    
    # Give simulation a moment to start
    time.sleep(2)
    
    print(f"\nServer ready! Open http://localhost:{args.port} in your browser")
    print("You should see soldiers within 5 seconds\n")
    
    # Start Flask
    app.run(host=args.host, port=args.port, debug=False, threaded=True)