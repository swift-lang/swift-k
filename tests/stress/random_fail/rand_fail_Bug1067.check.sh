#!/bin/bash

if [ ! -f ${0%.check.sh}.stdout ]
then
    echo "${$0%.check.sh}.stdout missing"
    exit -1
fi

grep "Got one name (derr)" ${0%.check.sh}.stdout
if [ "$?" == 0 ]
then
    echo "EXIT : REGRESSION FOUND!" >&2
    exit -1
else
    echo "Test passed"
fi
exit 0