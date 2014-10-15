#!/bin/bash

LOGGING=1 # Either 1 or 0
RUNDIRS=$(echo run[0-9][0-9][0-9])
RUNDIR=${RUNDIRS##*\ }
LOG=$RUNDIR/scripts/log
[[ "$LOGGING" == "1" ]] && mkdir -p $(dirname $LOG)

CLOUD_PY=$SWIFT_HOME/libexec/gce-cloud-provider/cloud.py

log()
{
    [[ "$LOGGING" == "1" ]] && echo $(date +"%H:%M:%S") $* >> $LOG
}

JOBID=$1
CONF=/tmp/$JOBID
EXITCODE=0

# Check for the conf file for the job at /tmp/<jobid>
if [ ! -f "$CONF" ]
then
    echo "ERROR: Could not find conf script at /tmp/$1" 1>&2
    exit -1
fi

if [[ "$1" != "" ]]
then
    log "Cancelling $JOBID"
    python $CLOUD_PY --logfile $LOG --cancel $CONF --jobid $JOBID | tee -a $LOG
    EXITCODE=$?
    if [[ "$EXITCODE" == "0" ]]
    then
        log "Done cancelling $JOBID"
        rm $CONF
    else
        log "Failed to cancel $JOBID: returned exitcode:$EXITCODE"
    fi
fi

exit $EXITCODE
