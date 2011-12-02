name "domain-gridftp-default"
description "A domain's GridFTP machine"
run_list "role[globus]", "recipe[globus::gridftp-default]", "recipe[provision::gridmap]"

