import paramiko

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
c.connect('10.42.0.1', username='pi', password='mesh')

cmds = [
    ('CPU', 'lscpu | head -15'),
    ('RAM', 'free -h'),
    ('DISK', 'df -h /'),
    ('NODE', 'node -v 2>/dev/null || echo "Node.js NOT installed"'),
    ('NPM', 'npm -v 2>/dev/null || echo "npm NOT installed"'),
    ('PYTHON', 'python3 --version'),
    ('ARCH', 'uname -m'),
    ('OS', 'cat /etc/os-release | head -4'),
    ('UPTIME', 'uptime'),
    ('SERVICES', 'systemctl list-units --type=service --state=running --no-pager 2>/dev/null | grep -iE "mesh|nginx|uvicorn" || echo "no matching services"'),
]

for label, cmd in cmds:
    print(f"\n=== {label} ===")
    _, stdout, _ = c.exec_command(cmd)
    print(stdout.read().decode().strip())

c.close()
