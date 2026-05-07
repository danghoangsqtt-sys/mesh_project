#!/usr/bin/env python3
"""Fix commands.py: Add DIRECT_MESSAGE to cmd_map"""
filepath = '/opt/mesh_pi5_server/backend/app/routers/commands.py'
with open(filepath, 'r') as f:
    content = f.read()

old_map = '"BROADCAST": 0x03}'
new_map = '"BROADCAST": 0x03, "DIRECT_MESSAGE": 0x03}'
content = content.replace(old_map, new_map)

with open(filepath, 'w') as f:
    f.write(content)

print("Fixed cmd_map in commands.py")
