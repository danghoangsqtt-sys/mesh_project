"""
Deploy Map Fix to Raspberry Pi 5
=================================
1. Uploads the rebuilt frontend/dist to Pi
2. Clears Nginx cache
3. Restarts services
4. Verifies deployment

Usage: python deploy_map_fix.py
"""
import os
import sys
import time

try:
    import paramiko
except ImportError:
    print("Installing paramiko...")
    os.system(f"{sys.executable} -m pip install paramiko")
    import paramiko

PI_HOST = "10.42.0.1"
PI_PORT = 22
PI_USER = "mesh"
PI_PASS = "mesh"
REMOTE_BASE = "/opt/mesh_pi5_server"

def sync_folder(sftp, local_dir, remote_dir):
    """Recursively upload a local directory to remote."""
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
            size = os.path.getsize(local_path)
            print(f"  [UPLOAD] {item} ({size:,} bytes)")
            sftp.put(local_path, remote_path)
        elif os.path.isdir(local_path):
            sync_folder(sftp, local_path, remote_path)


def run_ssh(ssh, cmd, password=PI_PASS, timeout=15):
    """Run a command via SSH, optionally with sudo."""
    full_cmd = f"echo '{password}' | sudo -S {cmd}" if password else cmd
    stdin, stdout, stderr = ssh.exec_command(full_cmd, timeout=timeout)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    # Filter out the sudo password prompt from stderr
    err_lines = [l for l in err.splitlines() if 'password' not in l.lower() and '[sudo]' not in l.lower()]
    err = '\n'.join(err_lines)
    return out, err


def deploy():
    local_dist = os.path.join(os.path.dirname(__file__), "frontend", "dist")
    if not os.path.isdir(local_dist):
        print("[ERROR] frontend/dist not found! Run 'npm run build' first.")
        sys.exit(1)

    print(f"\n{'='*60}")
    print(f"  MAP FIX DEPLOYMENT TO Pi 5")
    print(f"  Host: {PI_HOST} | User: {PI_USER}")
    print(f"{'='*60}\n")

    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    # Try connecting
    username = PI_USER
    for user in [PI_USER, "pi"]:
        try:
            print(f"[CONNECT] Connecting as {user}@{PI_HOST}...")
            ssh.connect(PI_HOST, PI_PORT, user, PI_PASS, timeout=10)
            username = user
            print(f"[OK] Connected as {username}")
            break
        except Exception as e:
            if user == "pi":
                print(f"[ERROR] Connection failed: {e}")
                sys.exit(1)
            print(f"  [WARN] Failed with {user}, trying next...")

    # Step 1: Upload dist
    print(f"\n[STEP 1] Uploading frontend/dist...")
    sftp = ssh.open_sftp()
    
    # Create staging dir
    staging = f"/home/{username}/frontend_dist_new"
    try:
        sftp.stat(staging)
        run_ssh(ssh, f"rm -rf {staging}")
    except:
        pass
    
    sync_folder(sftp, local_dist, staging)
    sftp.close()
    print(f"  [OK] Upload complete")

    # Step 2: Replace dist on Pi
    print(f"\n[STEP 2] Replacing {REMOTE_BASE}/frontend/dist...")
    run_ssh(ssh, f"rm -rf {REMOTE_BASE}/frontend/dist")
    run_ssh(ssh, f"mv {staging} {REMOTE_BASE}/frontend/dist")
    run_ssh(ssh, f"chown -R {username}:{username} {REMOTE_BASE}/frontend/dist")
    run_ssh(ssh, f"chmod -R 755 {REMOTE_BASE}/frontend/dist")
    print(f"  [OK] Frontend replaced")

    # Step 3: Clear Nginx cache and restart
    print(f"\n[STEP 3] Clearing caches and restarting services...")
    
    # Clear any Nginx proxy cache
    run_ssh(ssh, "rm -rf /var/cache/nginx/*")
    
    # Add cache-busting headers to nginx
    run_ssh(ssh, "nginx -t")
    run_ssh(ssh, "systemctl restart nginx")
    print(f"  [OK] Nginx restarted")

    # Restart mesh server
    run_ssh(ssh, "systemctl restart mesh-server")
    print(f"  [OK] Mesh server restarted")

    # Step 4: Verify
    print(f"\n[STEP 4] Verifying deployment...")
    time.sleep(3)
    
    out, err = run_ssh(ssh, "systemctl is-active mesh-server", password=None)
    mesh_status = "[OK] RUNNING" if "active" in out else f"[FAIL] {out}"
    
    out, err = run_ssh(ssh, "systemctl is-active nginx", password=None)
    nginx_status = "[OK] RUNNING" if "active" in out else f"[FAIL] {out}"
    
    out, _ = run_ssh(ssh, f"ls -la {REMOTE_BASE}/frontend/dist/assets/ | head -5", password=None)
    
    print(f"\n{'='*60}")
    print(f"  DEPLOYMENT REPORT")
    print(f"{'='*60}")
    print(f"  Mesh Server:  {mesh_status}")
    print(f"  Nginx:        {nginx_status}")
    print(f"  Frontend:     {REMOTE_BASE}/frontend/dist/")
    print(f"\n  Assets:")
    for line in out.splitlines():
        print(f"     {line.strip()}")
    print(f"\n  Open http://{PI_HOST} in browser (force refresh Ctrl+Shift+R)")
    print(f"{'='*60}\n")

    ssh.close()
    print("Deployment complete!")


if __name__ == "__main__":
    deploy()
