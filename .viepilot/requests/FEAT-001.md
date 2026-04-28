# FEAT: Desktop Parity — Gateway UI + Command Messaging + Full Sensor Display

## Meta
- **ID**: FEAT-001
- **Type**: Feature
- **Status**: new
- **Priority**: high
- **Created**: 2026-04-28
- **Reporter**: User
- **Assignee**: AI

## Summary
Android app thiếu 3 tính năng quan trọng so với desktop Python app:
1. Gateway Connection UI (chọn kết nối, baud rate, connect/disconnect)
2. Command & Control messaging (gửi tin nhắn broadcast/direct đến nodes)
3. Full sensor data display (SpO2, Temperature trong node table)

## Reference
Desktop app screenshot cho thấy:
- Gateway Connection: COM port dropdown, Baud 115200, Connect button, Online/Offline status
- Command & Control: message input, type (Broadcast/Direct), target (All/specific node), Send button
- Node table: ID, Team, Status, Position, HR, SpO2, Temp, Bat

## Tasks Required

### FEAT-001-A: Gateway Connection Settings UI
- Settings panel hiển thị USB device selection
- WiFi AP host/port configuration
- Connect / Disconnect buttons
- Real-time connection status indicator (Online/Offline)
- Baud rate selector (9600, 115200, etc.)

### FEAT-001-B: Command & Control Messaging
- Message input field
- Message type: Broadcast / Direct / Command
- Target selector: All / specific node ID
- Send button → serialize message → send via USB serial or WiFi TCP
- Message history in event log
- Requires: add `write()` method to UsbSerialManager and WifiCommManager

### FEAT-001-C: Full Sensor Data in Node Table
- Add SpO2 column to TacticalPanel NodeTable
- Add Temperature column to TacticalPanel NodeTable
- Color-code SpO2 (green >95, yellow 90-95, red <90)
- Color-code Temperature (green <37.5, yellow 37.5-38.5, red >38.5)

## Acceptance Criteria
- [ ] Gateway connection panel visible in TacticalPanel
- [ ] User can select USB device or WiFi AP and connect/disconnect
- [ ] Connection status shows Online/Offline with colored indicator
- [ ] Message can be typed and sent via Broadcast or Direct mode
- [ ] Sent messages appear in event log
- [ ] SpO2 and Temp columns visible in node table with color coding
- [ ] All features work in landscape split-screen layout

## Related
- Phase: New Phase 4 (Desktop Parity)
- Files: TacticalPanel.kt, TacticalViewModel.kt, UsbSerialManager.kt, WifiCommManager.kt, MeshForegroundService.kt
- Dependencies: None (builds on Phase 1-3 infrastructure)

## Discussion
Tham khảo UI design từ desktop app (firmware_mesh_lilygo/server/ui_components.py).
Android version nên giữ military green theme nhưng adapt cho touch interface.
