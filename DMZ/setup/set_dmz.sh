#!/bin/bash

# Check if the script is run as root
if [ "$(id -u)" -ne 0 ]; then
  echo "This script needs to be run as root (with sudo)."
  exit 1
fi

# Path to the file with the content
INTERFACES_CONTENT_FILE="set_dmz.txt"

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

sysctl net.ipv4.ip_forward=1

sysctl net.ipv4.conf.all.forwarding

sudo iptables -F

iptables -P FORWARD ACCEPT

iptables -F FORWARD 

iptables -t nat -F  
                                                                                            
iptables -t nat -A POSTROUTING  -o eth3 -j MASQUERADE

#iptables -A PREROUTING -t nat -p icmp --icmp-type echo-request -j DNAT --to-destination 192.168.0.100

iptables -A PREROUTING -t nat -p tcp --dport 443 -j DNAT --to-destination 192.168.0.100

# Restart the NetworkManager service
echo "Restarting NetworkManager service"
sudo systemctl restart NetworkManager
