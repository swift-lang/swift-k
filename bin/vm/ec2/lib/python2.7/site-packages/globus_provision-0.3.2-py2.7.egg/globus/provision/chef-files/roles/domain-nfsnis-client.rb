name "domain-nfsnis-client"
description "A domain's client machine (any machine that is not the NFS/NIS server)"
run_list "recipe[provision::gp_node]", "recipe[provision::nis_client]", "recipe[provision::nfs_client]"

