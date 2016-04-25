
LOGGING=1 # Either 1 or 0

RUNDIRS=$(echo run[0-9][0-9][0-9])
RUNDIR=${RUNDIRS##*\ }
LOG="$RUNDIR/scripts/log"
[[ "$LOGGING" == "1" ]] && mkdir -p $(dirname $LOG)


log()
{
    [[ "$LOGGING" == "1" ]] && echo $* >> "$LOG"
}

AIMES_CLIENT="$SWIFT_HOME/libexec/aimes-emanager-provider/aimes-swift-client.py"
ENDPOINT='http://localhost:8080'
CANCEL_SESSION='/emgr/sessions/'
START_SESSION='/emgr/sessions/'
SUBMIT_TASK='/emgr/sessions/'

