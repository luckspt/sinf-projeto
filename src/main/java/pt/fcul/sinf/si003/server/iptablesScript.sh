#!/bin/bash

# Flush existing rules and set default policy
# By default, do not accept any incoming traffic
#################################################
# Clean all IP tables existing rules
iptables -F
# Set default chain policies to DROP for incoming network traffic
iptables -P INPUT DROP
# Set default chain policies to DROP for outgoing network traffic originating from the local machine
iptables -P OUTPUT DROP
# Set default chain policies to DROP for forwarding traffic
iptables -P FORWARD DROP



# The packets that are allowed are specified below
# -j flag stands for "jump" and specifies the target of the rule


# Allow loopback device traffic 
#################################
# loopback (lo) interface is virtual network interface that allows communication within the local machine itself
# Accept traffic incoming (-i) on the loopback (lo) interface
iptables -A INPUT -i lo -j ACCEPT
# Accept traffic outgoing (-o) on the loopback (lo) interface ??
iptables -A OUTPUT -o lo -j ACCEPT



# Allow established and related connections
############################################
# -m flag allows to match packets based on their connection state
# --state ESTABLISHED,RELATED condition matches packets that are part of an existing connection or are related to an existing connection
iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
iptables -A OUTPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

# Allow ping from the "gcc" machine
# -s flag allows to specify the source IP address
# -p icmp --icmp-type echo-request specifies the protocol and the type of ICMP echo request packets
# echo-request is the ICMP message type for ping
iptables -A INPUT -s gcc_ip_address -p icmp --icmp-type echo-request -j ACCEPT

# -d flag allows to specify the destination IP address
# -p icmp --icmp-type echo-reply specifies the protocol and the type of ICMP echo reply packets
iptables -A OUTPUT -d gcc_ip_address -p icmp --icmp-type echo-reply -j ACCEPT

# Allow SSH from and to the "gcc" machine
# -p tcp specifies the protocol
# --dport 22 specifies the destination port
iptables -A INPUT -s gcc_ip_address -p tcp --dport 22 -j ACCEPT

# --sport 22 specifies the source port 
iptables -A OUTPUT -d gcc_ip_address -p tcp --sport 22 -j ACCEPT


# Allow connections from any origin to myCloud server
iptables -A OUTPUT -p tcp --sport myCloud_port -j ACCEPT



# Allow ping to machines in the local subnet with mask 255.255.254.0
iptables -A OUTPUT -p icmp --icmp-type echo-request -d 255.255.254.0/23 -j ACCEPT



ipset create allowedMachinesIPs hash:ip

ipset add allowedMachinesIPs 10.121.52.14
ipset add allowedMachinesIPs 10.121.52.15
ipset add allowedMachinesIPs 10.121.52.16
ipset add allowedMachinesIPs 10.121.72.23
ipset add allowedMachinesIPs 10.101.85.138
ipset add allowedMachinesIPs 10.101.85.18
ipset add allowedMachinesIPs 10.101.148.1
ipset add allowedMachinesIPs 10.101.85.137

iptables -A INPUT -m set --match-set allowedMachinesIPs src -j ACCEPT
iptables -A OUTPUT -m set --match-set allowedMachinesIPs src -j ACCEPT

# Save the iptables rules
iptables-save > /etc/iptables/rules.v4
