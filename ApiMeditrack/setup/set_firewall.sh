#!/bin/bash

# Add default route via 192.168.0.10
echo "Adding default route via 192.168.0.10"
sudo ip route add default via 192.168.0.10

sudo iptables -F

sudo iptables -P INPUT DROP
sudo iptables -P OUTPUT DROP

sudo iptables -A INPUT -p tcp --dport 443 -j ACCEPT
sudo iptables -A OUTPUT -p tcp --sport 443 -j ACCEPT

#sudo iptables -A INPUT -p tcp --dport 3306 -j ACCEPT
#sudo iptables -A OUTPUT -p tcp --sport 3306 -j ACCEPT

#sudo iptables -A OUTPUT -p tcp -d 192.168.0.10 --dport 443 -m state --state NEW,ESTABLISHED -j ACCEPT
#sudo iptables -A INPUT -p tcp -s 192.168.0.10 --sport 443 -m state --state ESTABLISHED -j ACCEPT

#sudo iptables -A OUTPUT -p tcp -d 192.168.1.101 --dport 3306 -m state --state NEW,ESTABLISHED -j ACCEPT
#sudo iptables -A INPUT -p tcp -s 192.168.1.101 --sport 3306 -m state --state ESTABLISHED -j ACCEPT
