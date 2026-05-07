#!/usr/bin/env python3
"""Fix mesh-server.service: Add SERIAL_PORT environment variable"""
filepath = '/etc/systemd/system/mesh-server.service'
with open(filepath, 'r') as f:
    content = f.read()

if 'SERIAL_PORT' not in content:
    content = content.replace(
        'Environment=PYTHONUNBUFFERED=1',
        'Environment=PYTHONUNBUFFERED=1\nEnvironment=SERIAL_PORT=/dev/ttyACM0'
    )
    with open(filepath, 'w') as f:
        f.write(content)
    print("Added SERIAL_PORT=/dev/ttyACM0 to service")
else:
    print("SERIAL_PORT already present")
