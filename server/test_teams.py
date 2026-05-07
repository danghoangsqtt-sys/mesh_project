#!/usr/bin/env python3
"""
Team-Based Test Server
Tests the complete team functionality including messaging
"""

import sys
import time
import threading
from soldier_manager import SoldierManager
from team_manager import TeamManager
from packet_handler import SoldierPacket
from flask import Flask, render_template, jsonify, request, Response
import random

soldier_manager = SoldierManager()
team_manager = TeamManager()

def simulate_team_soldiers():
    """Create soldiers matching team configuration"""
    print("Starting team-based simulation...")
    
    # Get all teams
    teams = team_manager.get_all_teams()
    print(f"\nLoaded {len(teams)} teams:")
    for team in teams:
        print(f"  - {team['name']}: {team['count']} soldiers (IDs: {min(team['soldierIds'])}-{max(team['soldierIds'])})")
    
    # Base positions for each team (spread across Boston area)
    team_positions = {
        'Alpha Team': (12.285000, 109.202000),
        'Bravo Team': (12.291389, 109.208502),
        'Charlie Team': (12.297777, 109.215004),
        'Delta Team': (12.304165, 109.221506),
        'Unassigned': (12.310552, 109.228009)
    }
    
    # Create virtual soldiers
    virtual_soldiers = {}
    for team in teams:
        base_lat, base_lon = team_positions.get(team['name'], (12.285000, 109.202000))
        
        for soldier_id in team['soldierIds'][:10]:  # Limit to first 10 of each team for demo
            offset_lat = random.uniform(-0.002, 0.002)
            offset_lon = random.uniform(-0.002, 0.002)
            
            virtual_soldiers[soldier_id] = {
                'lat': base_lat + offset_lat,
                'lon': base_lon + offset_lon,
                'heading': random.uniform(0, 360),
                'hr': random.randint(60, 90),
                'spo2': random.randint(95, 99),
                'temp': random.uniform(36.0, 37.5),
                'env_temp': random.uniform(18.0, 28.0),
                'battery': random.uniform(3.5, 4.2),
                'timestamp': 0
            }
    
    soldier_manager.update_gateway_position(42.360, -71.050)
    
    print(f"\nSimulating {len(virtual_soldiers)} soldiers across {len(teams)} teams")
    print("Dashboard: http://localhost:5000\n")
    
    # Simulation loop
    iteration = 0
    while True:
        for soldier_id, data in virtual_soldiers.items():
            # Small random movement
            data['lat'] += random.uniform(-0.0001, 0.0001)
            data['lon'] += random.uniform(-0.0001, 0.0001)
            data['heading'] = (data['heading'] + random.uniform(-10, 10)) % 360
            data['timestamp'] += 1
            
            # Vary vitals
            data['hr'] = max(50, min(120, data['hr'] + random.randint(-3, 3)))
            data['spo2'] = max(90, min(100, data['spo2'] + random.randint(-1, 1)))
            data['temp'] = max(35.0, min(39.0, data['temp'] + random.uniform(-0.2, 0.2)))
            data['env_temp'] = max(10.0, min(35.0, data['env_temp'] + random.uniform(-0.5, 0.5)))
            data['battery'] = max(2.8, data['battery'] - 0.0001)
            
            # Create packet
            status_flags = 0x001F  # All sensors valid
            if data['battery'] < 3.0:
                status_flags |= 0x0040  # Critical battery
            elif data['battery'] < 3.3:
                status_flags |= 0x0020  # Low battery
            
            packet = SoldierPacket(
                node_id=soldier_id,
                timestamp=data['timestamp'],
                latitude=data['lat'],
                longitude=data['lon'],
                heading=data['heading'],
                heart_rate=int(data['hr']),
                spo2=int(data['spo2']),
                temperature=data['env_temp'],  # Using env temp
                battery_voltage=data['battery'],
                status_flags=status_flags,
                crc16=0
            )
            
            soldier_manager.update_soldier(packet)
        
        iteration += 1
        if iteration % 10 == 0:
            print(f"[{iteration}s] Updated {len(virtual_soldiers)} soldiers")
        
        time.sleep(1.0)

# Flask app
app = Flask(__name__)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/soldiers')
def get_soldiers():
    soldiers = soldier_manager.get_all_soldiers()
    for soldier in soldiers:
        soldier['team'] = team_manager.get_team_name(soldier['nodeId'])
    return jsonify(soldiers)

@app.route('/api/teams')
def get_teams():
    soldiers = soldier_manager.get_all_soldiers()
    for soldier in soldiers:
        soldier['team'] = team_manager.get_team_name(soldier['nodeId'])
    grouped = team_manager.get_soldiers_by_teams(soldiers)
    return jsonify(grouped)

@app.route('/api/teams/list')
def get_teams_list():
    return jsonify(team_manager.get_all_teams())

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
    data = request.json
    target_type = data.get('targetType', 'soldier')
    message = data.get('message', '')
    
    soldier_ids = []
    
    if target_type == 'broadcast':
        soldier_ids = [0]
    elif target_type == 'team':
        team_name = data.get('team')
        soldier_ids = list(team_manager.get_team_soldiers(team_name))
    elif target_type == 'range':
        range_spec = data.get('range', '')
        soldier_ids = list(team_manager._parse_soldier_spec(range_spec))
    else:
        node_id = data.get('nodeId', 0)
        soldier_ids = [node_id]
    
    print(f"Message '{message}' to {target_type}: {soldier_ids}")
    
    return jsonify({
        'success': True,
        'sentTo': soldier_ids,
        'count': len(soldier_ids)
    })

@app.route('/api/gateway')
def get_gateway():
    if soldier_manager.gateway_position:
        return jsonify(soldier_manager.gateway_position)
    return jsonify({'error': 'No gateway position'}), 404

if __name__ == '__main__':
    print("=" * 70)
    print("TEAM-BASED SOLDIER TRACKING TEST SERVER")
    print("=" * 70)
    
    sim_thread = threading.Thread(target=simulate_team_soldiers, daemon=True)
    sim_thread.start()
    
    time.sleep(2)
    
    print("\nServer ready! Features to test:")
    print("1. Collapsible team groups")
    print("2. Soldier cards with Lat/Lon, Env Temp, Pressure, Humidity")
    print("3. Message panel with Broadcast/Team/Soldier/Range options")
    print("4. Path tracking when clicking 'Track' button")
    print("5. Team-based organization in sidebar")
    print("\nOpen: http://localhost:5000\n")
    
    app.run(host='0.0.0.0', port=5000, debug=False, threaded=True)