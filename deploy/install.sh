#!/bin/bash
# One-liner install script for Mesh Pi5 Server
# Run on fresh Raspberry Pi OS Bookworm

set -e

if [ "$EUID" -ne 0 ]; then
  echo "Please run as root: sudo ./install.sh"
  exit 1
fi

echo "--- Installing dependencies ---"
apt-get update
apt-get install -y python3 python3-pip python3-venv nginx network-manager nodejs npm git

APP_DIR="/opt/mesh_pi5_server"
CURRENT_DIR=$(pwd)

echo "--- Setting up directory ---"
# Assuming script is run from inside the repo root OR we just copy from current working dir
mkdir -p $APP_DIR
cp -r ../* $APP_DIR/ || true  # Handle if running from deploy folder
chown -R pi:pi $APP_DIR

echo "--- Setting up Backend ---"
cd $APP_DIR/backend
python3 -m venv venv
./venv/bin/pip install -r requirements.txt || ./venv/bin/pip install -e .

echo "--- Building Frontend ---"
cd $APP_DIR/frontend
sudo -u pi npm install
sudo -u pi npm run build

echo "--- Configuring Systemd Service ---"
cp $APP_DIR/deploy/mesh-server.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable mesh-server
systemctl start mesh-server

echo "--- Configuring Nginx ---"
cp $APP_DIR/deploy/nginx.conf /etc/nginx/sites-available/mesh-server
ln -sf /etc/nginx/sites-available/mesh-server /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
systemctl restart nginx

echo "--- Setting up WiFi AP ---"
bash $APP_DIR/deploy/setup-wifi-ap.sh

echo "--- Installation Complete ---"
echo "Connect to WiFi 'Mesh_Tactical_AP' and go to http://10.42.0.1"
