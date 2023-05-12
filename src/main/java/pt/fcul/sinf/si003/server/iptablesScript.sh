#!/bin/bash

# Flush existing rules and set default policy
# By default, do not accept any incoming traffic
#################################################
# Clean all IP tables existing rules
# sudo iptables -F INPUT
# sudo iptables -F OUTPUT

sudo iptables -A INPUT -s 10.121.52.14 -j ACCEPT
sudo iptables -A INPUT -s 10.121.52.15 -j ACCEPT
sudo iptables -A INPUT -s 10.121.52.16 -j ACCEPT
sudo iptables -A INPUT -s 10.121.72.23 -j ACCEPT
sudo iptables -A INPUT -s 10.101.85.138 -j ACCEPT
sudo iptables -A INPUT -s 10.101.85.18 -j ACCEPT
sudo iptables -A INPUT -s 10.101.148.1 -j ACCEPT
sudo iptables -A INPUT -s 10.101.85.137 -j ACCEPT

sudo iptables -A OUTPUT -d 10.121.52.14 -j ACCEPT
sudo iptables -A OUTPUT -d 10.121.52.15 -j ACCEPT
sudo iptables -A OUTPUT -d 10.121.52.16 -j ACCEPT
sudo iptables -A OUTPUT -d 10.121.72.23 -j ACCEPT
sudo iptables -A OUTPUT -d 10.101.85.138 -j ACCEPT
sudo iptables -A OUTPUT -d 10.101.85.18 -j ACCEPT
sudo iptables -A OUTPUT -d 10.101.148.1 -j ACCEPT
sudo iptables -A OUTPUT -d 10.101.85.137 -j ACCEPT



# The packets that are allowed are specified below
# -j flag stands for "jump" and specifies the target of the rule


# Allow loopback device traffic 
#################################
# loopback (lo) interface is virtual network interface that allows communication within the local machine itself
# Accept traffic incoming (-i) on the loopback (lo) interface
sudo iptables -A INPUT -i lo -j ACCEPT
# Accept traffic outgoing (-o) on the loopback (lo) interface ??
sudo iptables -A OUTPUT -o lo -j ACCEPT



# Allow established and related connections
############################################
# -m flag allows to match packets based on their connection state
# --state ESTABLISHED,RELATED condition matches packets that are part of an existing connection or are related to an existing connection
sudo iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
sudo iptables -A OUTPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

# Allow ping from the "gcc" machine
# -s flag allows to specify the source IP address
# -p icmp --icmp-type echo-request specifies the protocol and the type of ICMP echo request packets
# echo-request is the ICMP message type for ping
sudo iptables -A INPUT -s 10.101.151.5 -p icmp --icmp-type echo-request -j ACCEPT

# -d flag allows to specify the destination IP address
# -p icmp --icmp-type echo-reply specifies the protocol and the type of ICMP echo reply packets
sudo iptables -A OUTPUT -d 10.101.151.5 -p icmp --icmp-type echo-reply -j ACCEPT

# Allow SSH from and to the "gcc" machine
# -p tcp specifies the protocol
# --dport 22 specifies the destination port
sudo iptables -A INPUT -s 10.101.151.5 -p tcp --dport 22 -j ACCEPT

# --sport 22 specifies the source port 
sudo iptables -A OUTPUT -d 10.101.151.5 -p tcp --sport 22 -j ACCEPT


# Allow connections from any origin to myCloud server
sudo iptables -A INPUT -p tcp --dport 3000 -j ACCEPT


# Allow ping to machines in the local subnet with mask 255.255.254.0
sudo iptables -A OUTPUT -p icmp --icmp-type 8 -d 10.101.148.0/22 -j ACCEPT



# Set default chain policies to DROP for incoming network traffic
sudo iptables -A INPUT -j DROP
# Set default chain policies to DROP for outgoing network traffic originating from the local machine
sudo iptables -A OUTPUT -j DROP
# Set default chain policies to DROP for forwarding traffic
fc56897@imagem-2223:~/Desktop/sinf-projeto-main/sinf-projeto-main/src/main/java/pt/fcul/sinf/si003/server$ 
fc56897@imagem-2223:~/Desktop/sinf-projeto-main/sinf-projeto-main/src/main/java/pt/fcul/sinf/si003/server$ cat iptablesScript.sh 
#!/bin/bash

# Flush existing rules and set default policy
# By default, do not accept any incoming traffic
#################################################
# Clean all IP tables existing rules
# sudo iptables -F INPUT
# sudo iptables -F OUTPUT

sudo iptables -A INPUT -s 10.121.52.14 -j ACCEPT
sudo iptables -A INPUT -s 10.121.52.15 -j ACCEPT
sudo iptables -A INPUT -s 10.121.52.16 -j ACCEPT
sudo iptables -A INPUT -s 10.121.72.23 -j ACCEPT
sudo iptables -A INPUT -s 10.101.85.138 -j ACCEPT
sudo iptables -A INPUT -s 10.101.85.18 -j ACCEPT
sudo iptables -A INPUT -s 10.101.148.1 -j ACCEPT
sudo iptables -A INPUT -s 10.101.85.137 -j ACCEPT

sudo iptables -A OUTPUT -d 10.121.52.14 -j ACCEPT
sudo iptables -A OUTPUT -d 10.121.52.15 -j ACCEPT
sudo iptables -A OUTPUT -d 10.121.52.16 -j ACCEPT
sudo iptables -A OUTPUT -d 10.121.72.23 -j ACCEPT
sudo iptables -A OUTPUT -d 10.101.85.138 -j ACCEPT
sudo iptables -A OUTPUT -d 10.101.85.18 -j ACCEPT
sudo iptables -A OUTPUT -d 10.101.148.1 -j ACCEPT
sudo iptables -A OUTPUT -d 10.101.85.137 -j ACCEPT



# The packets that are allowed are specified below
# -j flag stands for "jump" and specifies the target of the rule


# Allow loopback device traffic 
#################################
# loopback (lo) interface is virtual network interface that allows communication within the local machine itself
# Accept traffic incoming (-i) on the loopback (lo) interface
sudo iptables -A INPUT -i lo -j ACCEPT
# Accept traffic outgoing (-o) on the loopback (lo) interface ??
sudo iptables -A OUTPUT -o lo -j ACCEPT



# Allow established and related connections
############################################
# -m flag allows to match packets based on their connection state
# --state ESTABLISHED,RELATED condition matches packets that are part of an existing connection or are related to an existing connection
sudo iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
sudo iptables -A OUTPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

# Allow ping from the "gcc" machine
# -s flag allows to specify the source IP address
# -p icmp --icmp-type echo-request specifies the protocol and the type of ICMP echo request packets
# echo-request is the ICMP message type for ping
sudo iptables -A INPUT -s 10.101.151.5 -p icmp --icmp-type echo-request -j ACCEPT

# -d flag allows to specify the destination IP address
# -p icmp --icmp-type echo-reply specifies the protocol and the type of ICMP echo reply packets
sudo iptables -A OUTPUT -d 10.101.151.5 -p icmp --icmp-type echo-reply -j ACCEPT

# Allow SSH from and to the "gcc" machine
# -p tcp specifies the protocol
# --dport 22 specifies the destination port
sudo iptables -A INPUT -s 10.101.151.5 -p tcp --dport 22 -j ACCEPT

# --sport 22 specifies the source port 
sudo iptables -A OUTPUT -d 10.101.151.5 -p tcp --sport 22 -j ACCEPT


# Allow connections from any origin to myCloud server
sudo iptables -A INPUT -p tcp --dport 3000 -j ACCEPT


# Allow ping to machines in the local subnet with mask 255.255.254.0
sudo iptables -A OUTPUT -p icmp --icmp-type 8 -d 10.101.148.0/22 -j ACCEPT



# Set default chain policies to DROP for incoming network traffic
sudo iptables -A INPUT -j DROP
# Set default chain policies to DROP for outgoing network traffic originating from the local machine
sudo iptables -A OUTPUT -j DROP
# Set default chain policies to DROP for forwarding traffic
#sudo iptables -P FORWARD DROP
