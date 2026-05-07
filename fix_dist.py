import paramiko

def ssh_exec(client, cmd):
    print(f"\n> {cmd}")
    stdin, stdout, stderr = client.exec_command(cmd)
    out = stdout.read().decode().strip()
    if out: print(out)

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
print("Connecting to Pi...")
c.connect('10.42.0.1', username='pi', password='mesh')
print("Connected!")

print("\n=== Fixing dist folder structure ===")
# Move everything from frontend_dist up to dist
ssh_exec(c, 'sudo mv /opt/mesh_pi5_server/frontend/dist/frontend_dist/* /opt/mesh_pi5_server/frontend/dist/')
ssh_exec(c, 'sudo rm -rf /opt/mesh_pi5_server/frontend/dist/frontend_dist')

# Ensure permissions are correct for nginx
ssh_exec(c, 'sudo chown -R www-data:www-data /opt/mesh_pi5_server/frontend/dist')
ssh_exec(c, 'sudo chmod -R 755 /opt/mesh_pi5_server/frontend/dist')

# Verify
ssh_exec(c, 'ls -la /opt/mesh_pi5_server/frontend/dist | head -n 10')

print("\nDone")
c.close()
