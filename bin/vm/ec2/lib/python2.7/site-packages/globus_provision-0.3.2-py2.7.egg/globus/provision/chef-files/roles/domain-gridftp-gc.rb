name "domain-gridftp-gc"
description "A domain's GridFTP machine (using a Globus Connect certificate)"
run_list "role[globus]", "recipe[globus::gridftp-gc]", "recipe[provision::gridmap]"

