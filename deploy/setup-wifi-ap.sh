#!/bin/bash
# Script to configure Raspberry Pi 5 as a standalone WiFi Access Point using NetworkManager (nmcli).
# OS: Raspberry Pi OS Bookworm (which uses NetworkManager by default)

set -e

SSID="Mesh_Tactical_AP"
PASSWORD="meshadmin123"
IFACE="wlan0"
IP_ADDR="10.42.0.1/24"

if [ "$EUID" -ne 0 ]; then
  echo "Please run as root (sudo)"
  exit 1
fi

echo "Setting up WiFi Access Point..."

# Ensure NetworkManager is running
systemctl start NetworkManager
systemctl enable NetworkManager

# Delete existing hotspot profile if it exists
nmcli connection show "$SSID" >/dev/null 2>&1 && nmcli connection delete "$SSID"

# Create new hotspot profile
nmcli connection add type wifi ifname "$IFACE" con-name "$SSID" autoconnect yes ssid "$SSID"
nmcli connection modify "$SSID" 802-11-wireless.mode ap 802-11-wireless.band bg ipv4.method shared ipv4.address "$IP_ADDR"
nmcli connection modify "$SSID" wifi-sec.key-mgmt wpa-psk wifi-sec.psk "$PASSWORD"

# Bring up the connection
nmcli connection up "$SSID"

echo "WiFi Access Point '$SSID' configured and started."
echo "Connect your devices to '$SSID' with password '$PASSWORD'."
echo "Dashboard will be available at http://10.42.0.1"
