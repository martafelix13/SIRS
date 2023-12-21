#!/bin/bash

sudo iptables -A INPUT -j DROP
sudo iptables -A OUTPUT -j DROP

sudo iptables -A INPUT -i lo -j ACCEPT
sudo iptables -A OUTPUT -o lo -j ACCEPT

sudo iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
sudo iptables -A OUTPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

sudo iptables -A INPUT -p tcp --dport 3306 -j ACCEPT
sudo iptables -A OUTPUT -p tcp --sport 3306 -j ACCEPT




