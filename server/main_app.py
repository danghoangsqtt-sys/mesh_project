import sys
import os
import json
import threading
import time
from PySide6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QHBoxLayout, QVBoxLayout, 
    QScrollArea, QLabel, QFrame, QTabWidget, QSplitter
)
from PySide6.QtWebEngineWidgets import QWebEngineView
from PySide6.QtCore import QUrl, QTimer, Qt, Slot, Signal, QObject, QThread

# Import existing backend logic
from serial_comm import SerialCommunicator
from soldier_manager import SoldierManager
from team_manager import TeamManager
from alert_manager import get_alert_manager
from ui_components import MessagePanelWidget, NodeTableWidget, ConnectionBarWidget, LogPanelWidget, MIL_BG_DARK

class BackendWorker(QObject):
    """
    Worker to run the backend loop without blocking PySide6 UI.
    """
    data_updated = Signal(str)  # Sends JSON string to UI
    gateway_updated = Signal(float, float)
    log_msg = Signal(str) # For Event Log
    serial_msg = Signal(str) # For Raw Serial
    
    def __init__(self, serial_comm, soldier_manager, team_manager, alert_manager):
        super().__init__()
        self.serial = serial_comm
        self.soldier_manager = soldier_manager
        self.team_manager = team_manager
        self.alert_manager = alert_manager
        self.running = True

    def loop(self):
        # The serial port is already reading via its own thread.
        # This loop just polls the manager state and emits to the UI periodically.
        while self.running:
            soldiers = self.soldier_manager.get_all_soldiers()
            
            # Prepare payload for UI and Map
            payload = []
            for s in soldiers:
                node_id = s.get("nodeId", 0)
                alert_level, alerts = self.alert_manager.evaluate_soldier(s)
                
                data = {
                    "nodeId": node_id,
                    "team": self.team_manager.get_team_name(node_id),
                    "online": s.get("online", False),
                    "latitude": s.get("latitude", 0),
                    "longitude": s.get("longitude", 0),
                    "heartRate": s.get("heartRate", 0),
                    "spo2": s.get("spo2", 0),
                    "temperature": s.get("temperature", 0),
                    "batteryVoltage": s.get("batteryVoltage", 0),
                    "alertLevel": int(alert_level),
                    "alerts": alerts
                }
                payload.append(data)
                
                # If there are alerts, send to log
                if alert_level > 0:
                    for a in alerts:
                        self.log_msg.emit(f"[{s.get('team', 'Unassigned').upper()}] Node {node_id}: {a}")
                
            self.data_updated.emit(json.dumps(payload))
            
            time.sleep(1.0)
            # Emit Gateway position if we have it
            if self.soldier_manager.gateway_position:
                self.gateway_updated.emit(*self.soldier_manager.gateway_position)
                
            time.sleep(1.0) # Update UI at 1Hz

class MainWindow(QMainWindow):
    def __init__(self, serial_comm, soldier_manager, team_manager, alert_manager):
        super().__init__()
        self.serial = serial_comm
        self.soldier_manager = soldier_manager
        self.team_manager = team_manager
        self.alert_manager = alert_manager
        
        self.setWindowTitle("Tactical Command Center - Map View")
        self.resize(1400, 850)
        self.setStyleSheet(f"background-color: {MIL_BG_DARK};")
        
        self.init_ui()
        self.init_backend()
        
    def init_ui(self):
        central = QWidget()
        self.setCentralWidget(central)
        main_layout = QVBoxLayout(central)
        main_layout.setContentsMargins(10, 10, 10, 10)
        main_layout.setSpacing(10)
        
        # --- Top Panel (Connection) ---
        self.conn_bar = ConnectionBarWidget()
        self.conn_bar.connect_requested.connect(self.handle_connect)
        self.conn_bar.disconnect_requested.connect(self.handle_disconnect)
        self.conn_bar.map_mode_changed.connect(self.handle_map_mode)
        main_layout.addWidget(self.conn_bar, 0) # 0 = don't stretch
        
        splitter = QSplitter(Qt.Orientation.Horizontal)
        splitter.setStyleSheet("QSplitter::handle { background-color: #556b2f; }")
        
        # --- Left Panel (Tabs) ---
        self.tabs = QTabWidget()
        self.tabs.setStyleSheet("""
            QTabWidget::pane { border: 1px solid #556b2f; background: #3b5323; }
            QTabBar::tab { background: #2c3e2d; color: #e0e0e0; padding: 10px 20px; border: 1px solid #556b2f; font-weight: bold; }
            QTabBar::tab:selected { background: #4a5d23; color: white; border-bottom-color: #4a5d23; }
        """)
        
        # Tab 1: Tactical Control
        tab_tactical = QWidget()
        tac_layout = QVBoxLayout(tab_tactical)
        tac_layout.setContentsMargins(5, 5, 5, 5)
        tac_layout.setSpacing(10)
        
        # Message Panel
        self.msg_panel = MessagePanelWidget()
        self.msg_panel.send_requested.connect(self.handle_send_message)
        tac_layout.addWidget(self.msg_panel)
        
        # Table
        self.node_table = NodeTableWidget()
        self.node_table.track_clicked.connect(self.handle_track_soldier)
        tac_layout.addWidget(self.node_table)
        
        # Tab 2: System Logs
        tab_system = QWidget()
        sys_layout = QVBoxLayout(tab_system)
        sys_layout.setContentsMargins(5, 5, 5, 5)
        
        self.log_panel = LogPanelWidget()
        sys_layout.addWidget(self.log_panel)
        
        self.tabs.addTab(tab_tactical, "TACTICAL CONTROL")
        self.tabs.addTab(tab_system, "SYSTEM & HARDWARE")
        
        # --- Right Panel (Map) ---
        right_panel = QWidget()
        right_layout = QVBoxLayout(right_panel)
        right_layout.setContentsMargins(0, 0, 0, 0)
        
        self.web_view = QWebEngineView()
        
        # Enable loading remote CDN resources from local map.html
        from PySide6.QtWebEngineCore import QWebEngineSettings
        settings = self.web_view.settings()
        settings.setAttribute(QWebEngineSettings.WebAttribute.LocalContentCanAccessRemoteUrls, True)
        settings.setAttribute(QWebEngineSettings.WebAttribute.LocalContentCanAccessFileUrls, True)
        
        # Load local map.html
        base_dir = os.path.dirname(os.path.abspath(__file__))
        map_url = QUrl.fromLocalFile(os.path.join(base_dir, "resources", "map.html"))
        self.web_view.load(map_url)
        
        right_layout.addWidget(self.web_view)
        
        # Assemble
        splitter.addWidget(self.tabs)
        splitter.addWidget(right_panel)
        splitter.setSizes([600, 800])
        main_layout.addWidget(splitter, 1) # 1 = stretch to fill remaining space
        
    def init_backend(self):
        self.worker = BackendWorker(self.serial, self.soldier_manager, self.team_manager, self.alert_manager)
        self.worker.data_updated.connect(self.on_data_updated)
        self.worker.gateway_updated.connect(self.on_gateway_updated)
        self.worker.log_msg.connect(self.log_panel.append_event)
        self.worker.serial_msg.connect(self.log_panel.append_serial)
        
        self.serial.set_raw_serial_callback(self.worker.serial_msg.emit)
        
        self.thread = QThread()
        self.worker.moveToThread(self.thread)
        self.thread.started.connect(self.worker.loop)
        self.thread.start()
        
    @Slot(str)
    def on_data_updated(self, json_str):
        # Update Web Map
        script = f"updateSoldiers('{json_str}');"
        self.web_view.page().runJavaScript(script)
        
        # Update Table
        soldiers = json.loads(json_str)
        for s in soldiers:
            self.node_table.update_soldier(s)
                
    @Slot(float, float)
    def on_gateway_updated(self, lat, lon):
        script = f"updateGateway({lat}, {lon});"
        self.web_view.page().runJavaScript(script)
        
    @Slot(int)
    def handle_track_soldier(self, node_id):
        script = f"highlightSoldier({node_id});"
        self.web_view.page().runJavaScript(script)
        
    @Slot(str, str, str)
    def handle_send_message(self, target_type, target, msg):
        log_msg = f"Sending MSG: {msg} to {target_type} {target}"
        print(log_msg)
        self.log_panel.append_event(log_msg)
        
        if target_type == "broadcast":
            self.serial.send_packet(255, msg)
            self.log_panel.append_serial(f"TX [255]: {msg}")
        elif target_type == "soldier":
            try:
                node_id = int(target)
                self.serial.send_packet(node_id, msg)
                self.log_panel.append_serial(f"TX [{node_id}]: {msg}")
            except:
                pass
                
    @Slot(str, int)
    def handle_connect(self, port, baudrate):
        self.log_panel.append_event(f"Attempting to connect to {port} @ {baudrate}...")
        self.serial.port = port
        self.serial.baudrate = baudrate
        
        if self.serial.connect():
            self.conn_bar.set_connected_state(True)
            self.serial.start_reading()
            self.log_panel.append_event("Connected successfully.")
        else:
            self.log_panel.append_event("Connection failed.")
            
    @Slot()
    def handle_disconnect(self):
        self.log_panel.append_event("Disconnecting...")
        self.serial.disconnect()
        self.conn_bar.set_connected_state(False)
        
    @Slot(str)
    def handle_map_mode(self, mode):
        base_dir = os.path.dirname(os.path.abspath(__file__))
        tiles_path = QUrl.fromLocalFile(os.path.join(base_dir, "resources", "tiles")).toString()
        offline_url = f"{tiles_path}/{{z}}/{{x}}/{{y}}.png"
        
        if mode == "Offline Map":
            script = f"setMapMode('offline', '{offline_url}');"
            self.log_panel.append_event("Switched to Offline Map")
        else:
            script = "setMapMode('online', '');"
            self.log_panel.append_event("Switched to Online Map")
        self.web_view.page().runJavaScript(script)
        
    def closeEvent(self, event):
        self.worker.running = False
        self.serial.disconnect()
        event.accept()

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description='Tactical Command Center (Native)')
    parser.add_argument('--port', type=str, default="", help='Serial port (e.g., MOCK or COM3) - Optional, can be set via UI')
    args = parser.parse_args()
    
    # Initialize Core Managers
    serial_comm = SerialCommunicator(args.port)
    soldier_manager = SoldierManager()
    team_manager = TeamManager()
    alert_manager = get_alert_manager('alerts.conf')
    
    # Wire up callbacks
    serial_comm.set_packet_callback(soldier_manager.update_soldier)
    serial_comm.set_gps_callback(soldier_manager.update_gateway_position)
    
    # If port is provided via CLI, auto-connect
    if args.port:
        if not serial_comm.connect():
            print(f"Failed to connect to {args.port}")
        else:
            serial_comm.start_reading()
    
    # Create App
    app = QApplication(sys.argv)
    
    # Use high DPI scaling
    QApplication.setHighDpiScaleFactorRoundingPolicy(Qt.HighDpiScaleFactorRoundingPolicy.PassThrough)
    
    window = MainWindow(serial_comm, soldier_manager, team_manager, alert_manager)
    window.show()
    
    sys.exit(app.exec())
