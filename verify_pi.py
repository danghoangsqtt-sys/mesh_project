import paramiko

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
c.connect('10.42.0.1', username='pi', password='mesh')

def run(cmd):
    stdin, stdout, stderr = c.exec_command(cmd)
    stdout.channel.recv_exit_status()
    return stdout.read().decode().strip()

# Test nginx serving
print("=== Test nginx serving ===")
r = run('curl -s -o /dev/null -w "%{http_code} %{size_download}" http://localhost/assets/index-q1XsMXEP.js')
print(f"JS: {r}")
r = run('curl -s -o /dev/null -w "%{http_code} %{size_download}" http://localhost/assets/index-CCt_ws7B.css')
print(f"CSS: {r}")
r = run('curl -s -o /dev/null -w "%{http_code} %{size_download}" http://localhost/')
print(f"HTML: {r}")

# Check index.html
print()
print("=== index.html ===")
print(run('cat /opt/mesh_pi5_server/frontend/dist/index.html'))

# Check services
print()
print("=== Services ===")
print("mesh-server:", run('systemctl is-active mesh-server'))
print("nginx:", run('systemctl is-active nginx'))

c.close()
