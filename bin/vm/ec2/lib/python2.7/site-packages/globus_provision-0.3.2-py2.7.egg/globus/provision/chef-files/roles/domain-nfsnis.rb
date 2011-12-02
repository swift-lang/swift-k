name "domain-nfsnis"
description "An domain's NFS/NIS server"
run_list "recipe[provision::gp_node]", "recipe[provision::nis_server]", "recipe[provision::nfs_server]", "recipe[provision::domain_users]"

