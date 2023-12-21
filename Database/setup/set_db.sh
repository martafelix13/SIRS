#!/bin/bash

# Check if the script is run as root
if [ "$(id -u)" -ne 0 ]; then
  echo "This script needs to be run as root (with sudo)."
  exit 1
fi

# Path to the file with the content
INTERFACES_CONTENT_FILE="set_db.txt"

# Path to the /etc/network/interfaces file
INTERFACES_FILE="/etc/network/interfaces"

# Backup of the original file
cp "$INTERFACES_FILE" "$INTERFACES_FILE.bak"

# Remove the existing file before copying
rm -f "$INTERFACES_FILE"

# Add the content to /etc/network/interfaces
cat "$INTERFACES_CONTENT_FILE" | sudo tee "$INTERFACES_FILE" > /dev/null

echo "Configuration applied successfully."

# Add default route via 192.168.3.10
echo "Adding default route via 192.168.3.10"
sudo ip route add default via 192.168.3.10

sudo apt-get update -y
sleep 1
sudo apt-get install sqlite3 -y
sleep 1
sudo apt install maven -y
sleep 1

sudo route del defaul

# Restart the NetworkManager service
echo "Restarting NetworkManager service"
sudo systemctl restart NetworkManager

# Check if the correct number of arguments has been provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <database_name>"
    exit 1
fi

# Input parameter
db_file="$1"

# Check if the SQL file exists
if [ ! -f "$db_file" ]; then
    echo "Error: The SQL file '$db_file' does not exist."
    exit 1
fi

# Remove the SQL file extension to get the SQLite database name
db_name=$(echo "$db_file" | cut -f 1 -d '.')

# Execute the SQL script using sqlite3
sqlite3 "$db_name.db" < "$db_file"

# Check if the execution was successful
if [ $? -eq 0 ]; then
    echo "SQLite database '$db_name.db' created and populated successfully from the file '$db_file'."
else
    echo "Error: Failed to create and populate the database from the file '$db_file'."
fi

