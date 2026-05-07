from PySide6.QtWidgets import (
    QWidget, QVBoxLayout, QHBoxLayout, QLabel, QPushButton, 
    QFrame, QComboBox, QLineEdit, QTableWidget, QTableWidgetItem, QHeaderView, QAbstractItemView,
    QPlainTextEdit
)
from PySide6.QtCore import Qt, Signal
from PySide6.QtGui import QFont, QColor

# Military Green Colors
MIL_BG_DARK = "#3b5323"
MIL_BG_LIGHT = "#4a5d23"
MIL_ACCENT = "#556b2f"
MIL_TEXT_PRIMARY = "#ffffff"
MIL_TEXT_SECONDARY = "#e0e0e0"

# Colors for statuses
STATUS_OK = "#4CAF50" # Bright green for OK
STATUS_WARN = "#FFC107"
STATUS_CRIT = "#F44336"
STATUS_OFFLINE = "#9E9E9E"

# Common QSS
CARD_STYLE = f"""
    QFrame {{
        background-color: {MIL_BG_LIGHT};
        border-radius: 4px;
        border: 1px solid {MIL_ACCENT};
    }}
"""

BUTTON_STYLE = f"""
    QPushButton {{
        background-color: #6B8E23;
        color: white;
        border-radius: 4px;
        padding: 8px;
        font-weight: bold;
        font-size: 14px;
        border: 2px solid #8F9779;
    }}
    QPushButton:hover {{
        background-color: #8F9779;
        border: 2px solid #FFFFFF;
    }}
    QPushButton:pressed {{
        background-color: #2c3e2d;
    }}
"""

INPUT_STYLE = f"""
    QLineEdit, QComboBox {{
        background-color: #243315;
        color: #FFFFFF;
        border-radius: 4px;
        padding: 6px 12px;
        border: 1px solid #8F9779;
    }}
    QLineEdit:focus, QComboBox:focus {{
        border: 1px solid #FFFFFF;
        background-color: #2c3e2d;
    }}
    QComboBox::drop-down {{
        border-left: 1px solid #8F9779;
        width: 25px;
    }}
    QComboBox::down-arrow {{
        image: none;
        width: 0px;
        height: 0px;
        border-left: 5px solid transparent;
        border-right: 5px solid transparent;
        border-top: 5px solid #FFFFFF;
        margin-right: 8px;
    }}
"""

TABLE_STYLE = f"""
    QTableWidget {{
        background-color: {MIL_BG_DARK};
        color: {MIL_TEXT_PRIMARY};
        border: 1px solid {MIL_ACCENT};
        gridline-color: {MIL_ACCENT};
        selection-background-color: {MIL_BG_LIGHT};
    }}
    QTableWidget::item {{
        padding: 4px;
        border-bottom: 1px solid {MIL_ACCENT};
    }}
    QHeaderView::section {{
        background-color: {MIL_BG_LIGHT};
        color: white;
        padding: 5px;
        border: 1px solid {MIL_ACCENT};
        font-weight: bold;
    }}
    QScrollBar:vertical {{
        background: {MIL_BG_DARK};
        width: 12px;
    }}
    QScrollBar::handle:vertical {{
        background: {MIL_ACCENT};
        border-radius: 6px;
    }}
"""

class NodeTableWidget(QTableWidget):
    track_clicked = Signal(int)
    
    def __init__(self, parent=None):
        super().__init__(parent)
        self.init_ui()
        self.node_rows = {} # Map node_id to row_index
        
    def init_ui(self):
        self.setColumnCount(8)
        self.setHorizontalHeaderLabels([
            "ID", "Team", "Status", "Position", "HR", "SpO₂", "Temp", "Bat"
        ])
        
        self.setStyleSheet(TABLE_STYLE)
        self.setSelectionBehavior(QAbstractItemView.SelectionBehavior.SelectRows)
        self.setSelectionMode(QAbstractItemView.SelectionMode.SingleSelection)
        self.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers)
        self.setAlternatingRowColors(True)
        self.verticalHeader().setVisible(False)
        
        # Sizing
        header = self.horizontalHeader()
        header.setSectionResizeMode(0, QHeaderView.ResizeMode.ResizeToContents) # ID
        header.setSectionResizeMode(1, QHeaderView.ResizeMode.Stretch) # Team
        header.setSectionResizeMode(2, QHeaderView.ResizeMode.ResizeToContents) # Status
        header.setSectionResizeMode(3, QHeaderView.ResizeMode.Stretch) # Position
        header.setSectionResizeMode(4, QHeaderView.ResizeMode.ResizeToContents) # HR
        header.setSectionResizeMode(5, QHeaderView.ResizeMode.ResizeToContents) # SpO2
        header.setSectionResizeMode(6, QHeaderView.ResizeMode.ResizeToContents) # Temp
        header.setSectionResizeMode(7, QHeaderView.ResizeMode.ResizeToContents) # Bat
        
        self.cellDoubleClicked.connect(self._on_double_click)
        
    def _on_double_click(self, row, col):
        item = self.item(row, 0)
        if item:
            node_id = int(item.text())
            self.track_clicked.emit(node_id)
            
    def update_soldier(self, data):
        node_id = data.get("nodeId", 0)
        if node_id == 0: return
        
        online = data.get("online", False)
        alert_level = data.get("alertLevel", 0)
        
        if node_id not in self.node_rows:
            # Add new row
            row = self.rowCount()
            self.insertRow(row)
            self.node_rows[node_id] = row
            
            # Create items
            for c in range(8):
                it = QTableWidgetItem()
                it.setTextAlignment(Qt.AlignmentFlag.AlignCenter)
                self.setItem(row, c, it)
                
        row = self.node_rows[node_id]
        
        # Status calculation
        status_text = "Offline"
        status_color = STATUS_OFFLINE
        if online:
            if alert_level == 0:
                status_text = "OK"
                status_color = STATUS_OK
            elif alert_level == 1:
                status_text = "Warning"
                status_color = STATUS_WARN
            else:
                status_text = "CRITICAL"
                status_color = STATUS_CRIT
                
        # Update cells
        self.item(row, 0).setText(str(node_id))
        self.item(row, 1).setText(data.get("team", "unassigned").upper())
        
        status_item = self.item(row, 2)
        status_item.setText(status_text)
        status_item.setForeground(QColor(status_color))
        # Optional: Make text bold
        font = status_item.font()
        font.setBold(True)
        status_item.setFont(font)
        
        pos = f"{data.get('latitude', 0):.4f}, {data.get('longitude', 0):.4f}"
        self.item(row, 3).setText(pos)
        self.item(row, 4).setText(f"{data.get('heartRate', 0)}")
        self.item(row, 5).setText(f"{data.get('spo2', 0)}%")
        self.item(row, 6).setText(f"{data.get('temperature', 0):.1f}°C")
        self.item(row, 7).setText(f"{data.get('batteryVoltage', 0):.2f}V")


class MessagePanelWidget(QFrame):
    send_requested = Signal(str, str, str) # type, target, message
    
    def __init__(self, parent=None):
        super().__init__(parent)
        self.init_ui()
        
    def init_ui(self):
        self.setStyleSheet(CARD_STYLE)
        layout = QVBoxLayout()
        
        lbl = QLabel("📡 COMMAND & CONTROL")
        lbl.setStyleSheet(f"color: {MIL_TEXT_PRIMARY}; font-weight: bold; font-size: 14px; background: transparent; border: none;")
        
        self.msg_input = QLineEdit()
        self.msg_input.setPlaceholderText("Type message...")
        self.msg_input.setStyleSheet(INPUT_STYLE)
        
        hlayout = QHBoxLayout()
        self.type_combo = QComboBox()
        self.type_combo.addItems(["Broadcast", "Team", "Soldier"])
        self.type_combo.setStyleSheet(INPUT_STYLE)
        
        self.target_combo = QComboBox()
        self.target_combo.addItem("All")
        self.target_combo.setStyleSheet(INPUT_STYLE)
        
        hlayout.addWidget(self.type_combo)
        hlayout.addWidget(self.target_combo)
        
        btn_send = QPushButton("Send Message")
        btn_send.setStyleSheet(BUTTON_STYLE)
        btn_send.clicked.connect(self._on_send)
        
        layout.addWidget(lbl)
        layout.addWidget(self.msg_input)
        layout.addLayout(hlayout)
        layout.addWidget(btn_send)
        
        self.setLayout(layout)
        
    def _on_send(self):
        msg = self.msg_input.text().strip()
        if not msg: return
        t_type = self.type_combo.currentText().lower()
        t_target = self.target_combo.currentText()
        self.send_requested.emit(t_type, t_target, msg)
        self.msg_input.clear()

class ConnectionBarWidget(QFrame):
    connect_requested = Signal(str, int) # port, baudrate
    disconnect_requested = Signal()
    map_mode_changed = Signal(str)
    
    def __init__(self, parent=None):
        super().__init__(parent)
        self.init_ui()
        self.is_connected = False
        
    def init_ui(self):
        self.setStyleSheet(CARD_STYLE)
        self.setMaximumHeight(60) # Ensure it never expands vertically
        layout = QHBoxLayout()
        layout.setContentsMargins(10, 5, 10, 5)
        
        lbl = QLabel("📡 GATEWAY CONNECTION:")
        lbl.setStyleSheet(f"color: {MIL_TEXT_PRIMARY}; font-weight: bold; background: transparent; border: none;")
        
        self.port_combo = QComboBox()
        self.port_combo.addItems(["MOCK", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8"])
        self.port_combo.setStyleSheet(INPUT_STYLE)
        
        self.baud_combo = QComboBox()
        self.baud_combo.addItems(["115200", "9600", "38400", "57600"])
        self.baud_combo.setStyleSheet(INPUT_STYLE)
        
        self.map_mode_combo = QComboBox()
        self.map_mode_combo.addItems(["Online Map", "Offline Map"])
        self.map_mode_combo.setStyleSheet(INPUT_STYLE)
        self.map_mode_combo.currentTextChanged.connect(self._on_map_mode_changed)
        
        self.btn_connect = QPushButton("Connect")
        self.btn_connect.setStyleSheet(BUTTON_STYLE)
        self.btn_connect.clicked.connect(self._on_toggle_connect)
        
        self.status_lbl = QLabel("🔴 Offline")
        self.status_lbl.setStyleSheet(f"color: {STATUS_CRIT}; font-weight: bold; background: transparent; border: none;")
        
        layout.addWidget(lbl)
        layout.addWidget(self.port_combo)
        layout.addWidget(QLabel("Baud:"))
        layout.addWidget(self.baud_combo)
        layout.addWidget(QLabel("Map:"))
        layout.addWidget(self.map_mode_combo)
        layout.addWidget(self.btn_connect)
        layout.addWidget(self.status_lbl)
        layout.addStretch()
        
        self.setLayout(layout)
        
    def _on_toggle_connect(self):
        if not self.is_connected:
            self.connect_requested.emit(self.port_combo.currentText(), int(self.baud_combo.currentText()))
        else:
            self.disconnect_requested.emit()
            
    def _on_map_mode_changed(self, mode):
        self.map_mode_changed.emit(mode)
            
    def set_connected_state(self, connected: bool):
        self.is_connected = connected
        if connected:
            self.btn_connect.setText("Disconnect")
            self.status_lbl.setText("🟢 Connected")
            self.status_lbl.setStyleSheet(f"color: {STATUS_OK}; font-weight: bold; background: transparent; border: none;")
            self.port_combo.setEnabled(False)
            self.baud_combo.setEnabled(False)
        else:
            self.btn_connect.setText("Connect")
            self.status_lbl.setText("🔴 Offline")
            self.status_lbl.setStyleSheet(f"color: {STATUS_CRIT}; font-weight: bold; background: transparent; border: none;")
            self.port_combo.setEnabled(True)
            self.baud_combo.setEnabled(True)

class LogPanelWidget(QFrame):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.init_ui()
        
    def init_ui(self):
        self.setStyleSheet(CARD_STYLE)
        layout = QVBoxLayout()
        
        # Event Log
        lbl_event = QLabel("📋 EVENT LOG")
        lbl_event.setStyleSheet(f"color: {MIL_TEXT_PRIMARY}; font-weight: bold; font-size: 14px; background: transparent; border: none;")
        
        self.event_log = QPlainTextEdit()
        self.event_log.setReadOnly(True)
        self.event_log.setStyleSheet(f"background-color: {MIL_BG_DARK}; color: white; border: 1px solid {MIL_ACCENT}; font-family: Consolas, monospace;")
        
        # Raw Serial
        lbl_serial = QLabel("🔌 RAW SERIAL MONITOR")
        lbl_serial.setStyleSheet(f"color: {MIL_TEXT_PRIMARY}; font-weight: bold; font-size: 14px; background: transparent; border: none; margin-top: 10px;")
        
        self.serial_log = QPlainTextEdit()
        self.serial_log.setReadOnly(True)
        self.serial_log.setStyleSheet(f"background-color: #000000; color: #4CAF50; border: 1px solid {MIL_ACCENT}; font-family: Consolas, monospace;")
        
        layout.addWidget(lbl_event)
        layout.addWidget(self.event_log, 2) # takes more space
        layout.addWidget(lbl_serial)
        layout.addWidget(self.serial_log, 1)
        
        self.setLayout(layout)
        
    def append_event(self, text: str):
        self.event_log.appendPlainText(text)
        
    def append_serial(self, text: str):
        self.serial_log.appendPlainText(text)

