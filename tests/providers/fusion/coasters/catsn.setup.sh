#!/bin/bash

cp -v $GROUP/data.txt . || exit 1
ipaddr=$( ifconfig | grep inet | head -1 | cut -d ':' -f 2 | awk '{print $1}' )
export GLOBUS_HOSTNAME=$ipaddr

