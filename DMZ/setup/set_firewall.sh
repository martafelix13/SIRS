#!/bin/bash

sudo iptables -A INPUT -j DROP
sudo iptables -A OUTPUT -j DROP

sudo iptables -A INPUT -p tcp --dport 443 -m state --state NEW,ESTABLISHED -j ACCEPT
sudo iptables -A OUTPUT -p tcp --sport 443 -m state --state ESTABLISHED -j ACCEPT


