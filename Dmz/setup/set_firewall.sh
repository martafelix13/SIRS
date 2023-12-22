#!/bin/bash

sysctl net.ipv4.ip_forward=1

sysctl net.ipv4.conf.all.forwarding

sudo iptables -F

sudo iptables -P FORWARD ACCEPT
sudo iptables -F FORWARD 

sudo iptables -t nat -F  
                                                                                            
sudo iptables -t nat -A POSTROUTING  -o eth3 -j MASQUERADE

sudo iptables -t nat -A POSTROUTING -p tcp --dport 443 -j SNAT --to-source 192.168.0.10:443
sudo iptables -t nat -A PREROUTING -p tcp --dport 443 -j DNAT --to-destination 192.168.0.100:443


sudo iptables -A INPUT -j DROP
sudo iptables -A OUTPUT -j DROP

sudo iptables -A INPUT -p tcp --dport 443 -m state --state NEW,ESTABLISHED -j ACCEPT
sudo iptables -A OUTPUT -p tcp --sport 443 -m state --state ESTABLISHED -j ACCEPT


