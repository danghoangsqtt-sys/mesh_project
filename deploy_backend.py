import os
import sys
import paramiko

PI_HOST = "10.42.0.1"
PI_PORT = 22
PI_USER = "pi"
PI_PASS = "mesh" # It tried "mesh", then "pi" in previous script but PI_PASS was "mesh"
REMOTE_BASE = "/opt/mesh_pi5_server"

def sync_folder(sftp, local_dir, remote_dir):
    try:
        sftp.stat(remote_dir)
    except IOError:
        sftp.mkdir(remote_dir)

    for item in sorted(os.listdir(local_dir)):
        if item.startswith('.') or item == '__pycache__':
            continue
        local_path = os.path.join(local_dir, item)
        remote_path = f"{remote_dir}/{item}"

        if os.path.isfile(local_path):
            sftp.put(local_path, remote_path)
            print(f"Uploaded {item}")
        elif os.path.isdir(local_path):
            sync_folder(sftp, local_path, remote_path)

def run_ssh(ssh, cmd, password=PI_PASS):
    full_cmd = f"echo '{password}' | sudo -S {cmd}"
    stdin, stdout, stderr = ssh.exec_command(full_cmd, timeout=30)
    print(stdout.read().decode())
    print(stderr.read().decode())

def deploy():
    local_backend = os.path.join(os.path.dirname(__file__), "backend", "app")
    
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(PI_HOST, PI_PORT, PI_USER, PI_PASS, timeout=10)
    print("Connected to Pi.")

    sftp = ssh.open_sftp()
    
    # Upload to staging
    staging = f"/home/{PI_USER}/backend_app_new"
    try:
        sftp.stat(staging)
        run_ssh(ssh, f"rm -rf {staging}")
    except:
        pass
    sftp.mkdir(staging)
    
    sync_folder(sftp, local_backend, staging)
    
    # Replace remote
    run_ssh(ssh, f"cp -r {staging}/* {REMOTE_BASE}/backend/app/")
    run_ssh(ssh, f"chown -R pi:pi {REMOTE_BASE}/backend")
    
    # Restart service
    print("Restarting service...")
    run_ssh(ssh, "systemctl restart mesh-server")
    print("Backend deploy complete.")

if __name__ == '__main__':
    deploy()
