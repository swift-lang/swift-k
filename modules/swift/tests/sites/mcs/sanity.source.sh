#!/bin/bash
if [ "$HOSTNAME" == "midway001" ]
then
   export GLOBUS_HOSTNAME=swift.rcc.uchicago.edu
   export GLOBUS_TCP_PORT_RANGE=50000,51000
fi;
