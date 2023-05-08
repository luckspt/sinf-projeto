preprocessor frag3_global
preprocessor frag3_engine

# threshold:type limit means that the rule will fire when the number of events in the specified time period exceeds the specified threshold


# track by_dst means that the threshold will be applied to the number of events per destination IP address
# count 5 threshold will be applied when the number of events per destination IP address exceeds 5
# seconds 120 means that the threshold will be applied in a 2 minute period
# dport:<2048 threshold for ports lower than 2048
alert tcp any any -> any $port (msg:"Ligações em menos de 2 min. para portos inferiores a 2048"; threshold:type limit, track by_dst, count 5, seconds 120; dport:<2048; sid:20220510; rev:0;)

# track by_src means that the threshold will be applied to the number of events per source IP address
# Using detection_filter to filter out the events that are not relevant
alert tcp any any -> any $port (msg:"Estão a tentar descobrir uma password de acesso ao serviço"; threshold:type limit, track by_src, count 3, seconds 20; sid:20220511; rev:0;)



