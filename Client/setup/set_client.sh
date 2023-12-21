#!/bin/bash

# Check if the script is run as root
if [ "$(id -u)" -ne 0 ]; then
  echo "This script needs to be run as root (with sudo)."
  exit 1
fi

# Path to the file with the content
INTERFACES_CONTENT_FILE="set_client.txt"

# Path to the /etc/network/interfaces file
INTERFACES_FILE="/etc/network/interfaces"

# Backup of the original file
cp "$INTERFACES_FILE" "$INTERFACES_FILE.bak"

# Remove the existing file before copying
rm -f "$INTERFACES_FILE"

# Add the content to /etc/network/interfaces
cat "$INTERFACES_CONTENT_FILE" | sudo tee "$INTERFACES_FILE" > /dev/null

echo "Configuration applied successfully."

sudo apt-get update -y
sleep 1
sudo apt install maven -y
sleep 1

# Restart the NetworkManager service
echo "Restarting NetworkManager service"
sudo systemctl restart NetworkManager

