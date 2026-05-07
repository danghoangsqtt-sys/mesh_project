import os
import sys

# We'll use subprocess with paramiko if available, else we fall back to printing instructions.
try:
    import paramiko
except ImportError:
    print("Paramiko not installed. Installing...")
    os.system(f"{sys.executable} -m pip install paramiko")
    import paramiko

import stat

def sync_folder(sftp, local_dir, remote_dir):
    try:
        sftp.stat(remote_dir)
    except IOError:
        sftp.mkdir(remote_dir)

    for item in os.listdir(local_dir):
        if item == "__pycache__":
            continue
            
        local_path = os.path.join(local_dir, item)
        remote_path = f"{remote_dir}/{item}"
        
        if os.path.isfile(local_path):
            print(f"Uploading {local_path} -> {remote_path}")
            sftp.put(local_path, remote_path)
        elif os.path.isdir(local_path):
            sync_folder(sftp, local_path, remote_path)

def deploy():
    host = "10.42.0.1"
    port = 22
    username = "mesh"  # Default user for Pi, or it might be 'pi' or 'mesh'
    password = "mesh"

    print(f"Connecting to {username}@{host}...")
    
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    
    # Try username 'mesh' first, then 'pi' if it fails
    try:
        ssh.connect(host, port, username, password, timeout=10)
    except paramiko.AuthenticationException:
        print("Auth failed for user 'mesh'. Trying user 'pi'...")
        username = "pi"
        ssh.connect(host, port, username, password, timeout=10)
        
    sftp = ssh.open_sftp()
    
    local_backend = "backend"
    remote_backend = f"/home/{username}/backend"
    
    print(f"Syncing {local_backend} to {remote_backend}...")
    
    # Sync backend
    sync_folder(sftp, local_backend, remote_backend)
    
    sftp.close()
    
    local_frontend = "frontend/dist"
    remote_frontend = f"/home/{username}/frontend_dist"
    
    print(f"Syncing {local_frontend} to {remote_frontend}...")
    sftp = ssh.open_sftp()
    sync_folder(sftp, local_frontend, remote_frontend)
    sftp.close()
    
    print("Moving files to /opt/mesh_pi5_server with sudo...")
    ssh.exec_command(f"echo '{password}' | sudo -S cp -r {remote_backend}/* /opt/mesh_pi5_server/backend/")
    ssh.exec_command(f"echo '{password}' | sudo -S chown -R pi:pi /opt/mesh_pi5_server/backend")
    ssh.exec_command(f"echo '{password}' | sudo -S chmod -R 755 /opt/mesh_pi5_server/backend")
    
    ssh.exec_command(f"echo '{password}' | sudo -S rm -rf /opt/mesh_pi5_server/frontend/dist")
    ssh.exec_command(f"echo '{password}' | sudo -S cp -r {remote_frontend} /opt/mesh_pi5_server/frontend/dist")
    ssh.exec_command(f"echo '{password}' | sudo -S chown -R pi:pi /opt/mesh_pi5_server/frontend")
    ssh.exec_command(f"echo '{password}' | sudo -S chmod -R 755 /opt/mesh_pi5_server/frontend")
    
    # Restart the systemd service
    print("Restarting mesh-server.service...")
    stdin, stdout, stderr = ssh.exec_command(f"echo '{password}' | sudo -S systemctl restart mesh-server")
    print(stdout.read().decode())
    print(stderr.read().decode())
    
    ssh.close()
    print("Deployment complete.")

if __name__ == "__main__":
    deploy()
