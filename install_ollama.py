import paramiko
import time

PI_HOST = "10.42.0.1"
PI_USER = "pi"
PI_PASS = "mesh"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(PI_HOST, 22, PI_USER, PI_PASS, timeout=10)

def run_cmd(cmd):
    print(f"Running: {cmd}")
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=300)
    out = stdout.read().decode()
    err = stderr.read().decode()
    print("STDOUT:", out)
    print("STDERR:", err)
    return out

# Check internet
print("Checking internet...")
run_cmd("ping -c 1 google.com")

# Check ollama
print("Checking Ollama...")
out = run_cmd("ollama --version")
if "ollama version" not in out:
    print("Installing Ollama...")
    run_cmd("curl -fsSL https://ollama.com/install.sh | sh")
    print("Starting Ollama service...")
    run_cmd("echo 'mesh' | sudo -S systemctl start ollama")

print("Pulling Qwen 2.5 0.5B...")
# Just pull it in the background if it takes long
run_cmd("ollama pull qwen2.5:0.5b")
print("Done!")
