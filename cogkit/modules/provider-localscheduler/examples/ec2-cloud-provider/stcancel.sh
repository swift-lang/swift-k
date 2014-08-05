#!/bin/bash

LOGGING=1 # Either 1 or 0

RUNDIRS=$(echo run[0-9][0-9][0-9])
RUNDIR=${RUNDIRS##*\ }
LOG=$RUNDIR/scripts/log
[[ "$LOGGING" == "1" ]] && mkdir -p $(dirname $LOG)

CLOUD_PY=$SWIFT_HOME/libexec/ec2-cloud-provider/cloud.py

log()
{
    [[ "$LOGGING" == "1" ]] && echo $* >> $LOG
}

JOBID=$1
CONF=/tmp/$JOBID
EXITCODE=0

log "Received cancel directive for $JOBID"

# Check for the conf file for the job at /tmp/<jobid>
if [ ! -f "$CONF" ]
then
    echo "ERROR: Could not find conf script at /tmp/$1" 1>&2
    exit -1
fi

if [[ "$1" != "" ]]
then
    log "Cancelling $JOBID " $(date +"%T")
    python $CLOUD_PY --logfile $LOG --cancel $CONF --jobid $JOBID
    EXITCODE=$?
    if [[ "$EXITCODE" == "0" ]]
    then
        log "Done cancelling $JOBID" $(date +"%T")
        rm $CONF
    else
        log "Failed to cancel $JOBID: returned exitcode:$EXITCODE" $(date +"%T")
    fi
fi

exit $EXITCODE
