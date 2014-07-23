#!/bin/bash

LOG=/home/yadu/src/swift-trunk/cog/modules/provider-localscheduler/examples/ec2-cloud-provider/log
LOGGING=1 # Either 1 or 0
log()
{
    [[ "$LOGGING" == "1" ]] && echo $* >> $LOG
}

JOBID=$1
CONF=/tmp/$JOBID

# Check for the conf file for the job at /tmp/<jobid>
if [ ! -f "$CONF" ]
then
    echo "ERROR: Could not find conf script at /tmp/$1" 1>&2
    exit -1
fi

if [[ "$1" != "" ]]
then
	log "Stat'ing for $1 PWD: $PWD"
    python /home/yadu/src/swift-trunk/cog/modules/provider-localscheduler/examples/ec2-cloud-provider/cloud.py --status $CONF --jobid $JOBID | tee -a $LOG
fi
