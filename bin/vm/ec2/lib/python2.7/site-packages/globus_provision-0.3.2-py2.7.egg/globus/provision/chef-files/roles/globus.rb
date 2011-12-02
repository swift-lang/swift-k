name "globus"
description "A machine running Globus"
run_list "recipe[globus::client-tools]", "recipe[provision::ca]", "recipe[provision::hostcert]"

