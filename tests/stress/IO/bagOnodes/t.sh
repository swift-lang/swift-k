#!/bin/bash

arr=(crank chrun crush grind steamroller stomp thrash thwomp trounce vanquish);

for item in ${arr[*]}
do
    echo "Host $item.mcs.anl.gov"
    echo "    Hostname $item.mcs.anl.gov"
    echo "    User yadunand"
    echo "    ProxyCommand ssh -A yadunand@login.mcs.anl.gov nc %h %p 2> /dev/null"
    echo "    ForwardAgent yes"
    echo
done