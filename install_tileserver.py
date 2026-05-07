import paramiko

def ssh_exec(client, cmd, timeout=120):
    print(f"\n> {cmd}")
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    code = stdout.channel.recv_exit_status()
    if out:
        for line in out.split('\n'):
            print(f"  {line}")
    if err:
        for line in err.split('\n')[-5:]:
            print(f"  WARN: {line}")
    return out, err, code

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
c.connect('10.42.0.1', username='pi', password='mesh')

# The fonts nginx alias points to /opt/mesh_pi5_server/fonts/
# But actual fonts are in /opt/tileserver/fonts/
# Solution: symlink or copy

print("=== Symlink fonts to mesh_pi5_server dir ===")
ssh_exec(c, 'ls /opt/mesh_pi5_server/fonts/ 2>/dev/null || echo "DIR EMPTY OR MISSING"')
ssh_exec(c, 'sudo rm -rf /opt/mesh_pi5_server/fonts')
ssh_exec(c, 'sudo ln -s /opt/tileserver/fonts /opt/mesh_pi5_server/fonts')
ssh_exec(c, 'ls -la /opt/mesh_pi5_server/fonts/')

# Also create a "Noto Sans Bold" alias since our map code uses it
# but protomaps only has "Noto Sans Medium"
print("\n=== Create Noto Sans Bold symlink ===")
ssh_exec(c, 'ls /opt/tileserver/fonts/')
ssh_exec(c, 'cd /opt/tileserver/fonts && ln -sf "Noto Sans Medium" "Noto Sans Bold" 2>/dev/null || echo "already exists"')
ssh_exec(c, 'ls -la /opt/tileserver/fonts/')

# Final test: the exact URL the frontend requests
print("\n=== Final verification ===")
ssh_exec(c, 'curl -s -o /dev/null -w "Noto Sans Regular 0-255: %{http_code}" "http://127.0.0.1/fonts/Noto%20Sans%20Regular/0-255.pbf" 2>&1')
ssh_exec(c, 'curl -s -o /dev/null -w "Noto Sans Bold 0-255: %{http_code}" "http://127.0.0.1/fonts/Noto%20Sans%20Bold/0-255.pbf" 2>&1')
ssh_exec(c, 'curl -s -o /dev/null -w "Noto Sans Italic 0-255: %{http_code}" "http://127.0.0.1/fonts/Noto%20Sans%20Italic/0-255.pbf" 2>&1')

print("\nDone!")
c.close()
