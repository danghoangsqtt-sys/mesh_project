import paramiko
import time

def ssh_exec(client, cmd, timeout=30):
    print(f"\n> {cmd}")
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    if out:
        print(out)
    if err:
        print(f"ERR: {err}")
    return out

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
print("Connecting to Pi...")
c.connect('10.42.0.1', username='pi', password='mesh', timeout=10)
print("Connected!")

# 1. Check Nginx Error Logs
print("\n=== Nginx Error Logs ===")
ssh_exec(c, 'sudo tail -n 20 /var/log/nginx/error.log')

# 2. Check File Permissions
print("\n=== File Permissions ===")
ssh_exec(c, 'ls -la /opt/mesh_pi5_server/frontend/dist')
ssh_exec(c, 'ls -la /opt/mesh_pi5_server/frontend')
ssh_exec(c, 'namei -l /opt/mesh_pi5_server/frontend/dist/index.html')

# 3. Check what user Nginx runs as
print("\n=== Nginx User ===")
ssh_exec(c, 'ps aux | grep nginx')

# 4. Check if we need to fix permissions
print("\n=== Fixing Permissions just in case ===")
ssh_exec(c, 'sudo chmod -R 755 /opt/mesh_pi5_server/frontend')
ssh_exec(c, 'sudo chown -R www-data:www-data /opt/mesh_pi5_server/frontend/dist')
ssh_exec(c, 'sudo chmod o+x /opt /opt/mesh_pi5_server /opt/mesh_pi5_server/frontend')

print("\n=== Done ===")
c.close()
