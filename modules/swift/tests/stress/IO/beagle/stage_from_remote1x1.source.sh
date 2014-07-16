#!/bin/bash

export COG_OPTS=-Dtcp.channel.log.io.performance=true

if [ "midway001" == "midway001" ]
then
   export GLOBUS_HOSTNAME=swift.rcc.uchicago.edu
   export GLOBUS_TCP_PORT_RANGE=50000,51000
fi;

