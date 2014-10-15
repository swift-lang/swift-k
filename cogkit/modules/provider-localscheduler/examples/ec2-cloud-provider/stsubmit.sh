#!/bin/bash

rm -f /tmp/stsubmit
EXECUTABLE=
DIR=
ARGS=
STDOUTLOC=
STDOUTPATH=
STDERRLOC=
STDERRPATH=
STDINLOC=
STDINPATH=
STDIN=
STDOUT=
STDERR=

LOGGING=1 # Either 1 or 0
RUNDIRS=$(echo run[0-9][0-9][0-9])
RUNDIR=${RUNDIRS##*\ }
LOG=$RUNDIR/scripts/log
[[ "$LOGGING" == "1" ]] && mkdir -p $(dirname $LOG)

log()
{
    [[ "$LOGGING" == "1" ]] && echo $* >> $LOG
}

CLOUD_PY=$SWIFT_HOME/libexec/ec2-cloud-provider/cloud.py
SUBMIT_SCRIPT=$(mktemp)
touch $SUBMIT_SCRIPT

while read LINE; do
	echo $LINE >>/tmp/stsubmit
	case $LINE in
		executable=*)
			EXECUTABLE=${LINE#executable=}
			;;
		directory=*)
			DIR=${LINE#directory=}
			;;
		arg=*)
            if [[ ${LINE#arg=} == *cscript*pl ]]
            then
                # Replacing temporary worker script with a specific remote worker script
                ARGS="$ARGS /usr/local/bin/swift-trunk/bin/worker.pl"
            else
                ARGS="$ARGS ${LINE#arg=}"
            fi
			;;
		attr.*)
            LINE2=${LINE#attr.}
            if [[ $LINE2 == ec2* ]]
            then
			    echo $LINE2 >> $SUBMIT_SCRIPT
            fi
			;;
		stdin.location=*)
			STDINLOC=${LINE#stdin.location=}
			;;
		stdin.path=*)
			STDINPATH=${LINE#stdin.path=}
			;;
		stdout.location=*)
			STDOUTLOC=${LINE#stdout.location=}
			;;
		stdout.path=*)
			STDOUTPATH=${LINE#stdout.path=}
			;;
		stderr.location=*)
			STDERRLOC=${LINE#stderr.location=}
			;;
		stderr.path=*)
			STDERRPATH=${LINE#stderr.path=}
			;;
		env.*)
			LINE2=${LINE#env.}
			# split on '='
			ELS=(${LINE2//=/})
			NAME=${ELS[0]}
			VALUE=${ELS[1]}
			export $NAME=$VALUE
			;;
		*)
			echo "Don't know how to interpret line: $LINE" >&2
			exit 2
	esac
done < /dev/stdin

if [ "$STDOUTLOC" == "tmp" ]; then
	STDOUTPATH=$(mktemp)
	echo "stdout.path=$STDOUTPATH"
fi
if [ "$STDOUTPATH" != "" ]; then
	STDOUT="1> $STDOUTPATH"
fi

if [ "$STDERRLOC" == "tmp" ]; then
	STDERRPATH=$(mktemp)
	echo "stderr.path=$STDERRPATH"
fi
if [ "$STDERRPATH" != "" ]; then
	STDERR="2> $STDERRPATH"
fi

if [ "$STDINLOC" != "" ]; then
	STDIN="< $STDINLOC"
fi

CMD="$EXECUTABLE $ARGS $STDIN $STDOUT $STDERR"
log "CMD   : $CMD"

DIR=/tmp/

cat<<EOF >> $SUBMIT_SCRIPT
CMD_STRING="mkdir $DIR; cd $DIR; $CMD"
EOF

cat $SUBMIT_SCRIPT >> $LOG

log "$PWD"
log "python $CLOUD_PY --logfile $LOG --submit $SUBMIT_SCRIPT"

JOBINFO=$(python $CLOUD_PY --logfile $LOG --submit $SUBMIT_SCRIPT) 2> $LOG
echo $JOBINFO
retcode="$?"
log "JOBINFO : $JOBINFO"
log "RETCODE : $retcode"

[[ "$retcode" != "0" ]] && exit retcode

if [[ $JOBINFO == jobid\=* ]]
then
    cat $SUBMIT_SCRIPT > /tmp/${JOBINFO#jobid=}
fi

exit 0
