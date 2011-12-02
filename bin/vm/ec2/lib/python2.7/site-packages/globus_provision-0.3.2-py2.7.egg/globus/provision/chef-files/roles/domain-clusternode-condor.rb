name "domain-clusternode-condor"
description "A domain's Condor worker node"
run_list "recipe[condor::condor_worker]"

