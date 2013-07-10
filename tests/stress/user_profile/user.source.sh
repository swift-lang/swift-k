#!/bin/bash

# Profile for Yadu
export BEAGLE_USERNAME="yadunandb"
export MIDWAY_USERNAME="yadunand"
export MCS_USERNAME="yadunand"
export UC3_USERNAME="yadunand"

if [ "$HOSTNAME" == "midway001" ]
then
   export GLOBUS_HOSTNAME=swift.rcc.uchicago.edu
   export GLOBUS_TCP_PORT_RANGE=50000,51000
fi;