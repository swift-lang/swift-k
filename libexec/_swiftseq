#!/bin/bash

# get the parent directory of the directory containing wrapper.sh, to use
# as the run directory
# this assumes that seq.sh is being executed from the top level of
# the shared directory, and that shared directory is in the top level
# of the workflow run directory
WFDIR=$(dirname $(dirname $0))
cd $WFDIR

SEQID="$1"
shift

LOGPATH="$1"
shift

mkdir -p $PWD/$LOGPATH

WRAPPERLOG=$PWD/$LOGPATH/$SEQID.clusterlog

echo `date +%s` START > $WRAPPERLOG
echo `date +%s` WORKING DIRECTORY IS $PWD >> $WRAPPERLOG

ls >>$WRAPPERLOG

echo `date +%s` POST-LS >> $WRAPPERLOG

EXEC="$1"
shift

# we go round this loop once for each clustered job
while [ "$EXEC" != "" ]; do
	echo `date +%s` LOOP-START >> $WRAPPERLOG
	declare -a ARGS
	INDEX=0
	
	ARG="$1"
	shift
	
	while [ "$ARG" != "|" ]; do
		if [ "$ARG" == "||" ]; then
			ARG="|"
		fi
		echo `date +%s` ARG $ARG >> $WRAPPERLOG
		ARGS[$INDEX]=$ARG
		let INDEX=$INDEX+1
		
		ARG="$1"
		shift
	done
	echo `date +%s` EXECUTING $EXEC ${ARGS[@]} >> $WRAPPERLOG
	"$EXEC" "${ARGS[@]}"
	EXITCODE=$?
	echo `date +%s` EXECUTED $EXITCODE >> $WRAPPERLOG
	
	if [ "$EXITCODE" != "0" ]; then
		echo `date +%s` FAILED >> $WRAPPERLOG
		exit $EXITCODE
	fi
        unset ARGS
	EXEC="$1"
	shift
done
echo `date +%s` DONE >> $WRAPPERLOG
