"""Deep gateway diagnostics on Pi"""
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('10.42.0.1', 22, 'pi', 'mesh', timeout=10)

def run(cmd, timeout=15):
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    err_clean = '\n'.join(l for l in err.splitlines() if '[sudo]' not in l and 'password' not in l.lower())
    return out, err_clean

# 1. Serial port detection
print("=" * 60)
print("1. SERIAL PORT DETECTION")
print("=" * 60)
out, err = run("ls -la /dev/ttyACM* /dev/ttyUSB* 2>&1")
print(out or err or "No serial ports found!")

# 2. USB devices
print("\n" + "=" * 60)
print("2. USB DEVICES CONNECTED")
print("=" * 60)
out, _ = run("lsusb")
print(out)

# 3. dmesg for USB/serial
print("\n" + "=" * 60)
print("3. DMESG USB/SERIAL (last 20)")
print("=" * 60)
out, _ = run("dmesg | grep -iE 'ttyACM|ttyUSB|usb|serial|cp210|ch34|ftdi' | tail -20")
print(out)

# 4. Check what process holds serial port
print("\n" + "=" * 60)
print("4. PROCESS HOLDING SERIAL PORT")
print("=" * 60)
out, _ = run("echo 'mesh' | sudo -S fuser /dev/ttyACM0 2>&1")
print(out or "No process holding /dev/ttyACM0")

# 5. Check the backend serial_reader code
print("\n" + "=" * 60)
print("5. BACKEND SERIAL READER CONFIG")
print("=" * 60)
out, _ = run("grep -rn 'serial\\|ttyACM\\|ttyUSB\\|baud\\|Serial' /opt/mesh_pi5_server/backend/ --include='*.py' | head -20")
print(out or "No serial config found")

# 6. Read serial data with timeout
print("\n" + "=" * 60)
print("6. RAW SERIAL READ (5 seconds)")
print("=" * 60)
out, err = run("echo 'mesh' | sudo -S timeout 5 cat /dev/ttyACM0 2>&1", timeout=10)
print(out[:2000] if out else "NO DATA in 5 seconds!")

# 7. Check API response for nodes
print("\n" + "=" * 60)
print("7. API /api/nodes/ RESPONSE")
print("=" * 60)
out, _ = run("curl -s http://localhost:8000/api/nodes/ 2>&1")
print(out[:2000] if out else "No response")

# 8. API status
print("\n" + "=" * 60)
print("8. API /api/status/ RESPONSE")
print("=" * 60)
out, _ = run("curl -s http://localhost:8000/api/status/ 2>&1")
print(out[:1000] if out else "No response")

# 9. Check WebSocket serial handler
print("\n" + "=" * 60)
print("9. WEBSOCKET/SERIAL HANDLER CODE")
print("=" * 60)
out, _ = run("grep -rn 'serial_port\\|read_serial\\|on_data\\|process_line\\|incoming' /opt/mesh_pi5_server/backend/ --include='*.py' | head -15")
print(out)

ssh.close()
print("\nDiagnostics complete.")
