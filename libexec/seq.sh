#!/bin/bash

SEQID="$1"
shift

LOGPATH="$1"
shift

mkdir -p $PWD/$LOGPATH

WRAPPERLOG=$PWD/$LOGPATH/$SEQID.clusterlog

echo `date +%s` START > $WRAPPERLOG

ls >>$WRAPPERLOG

echo `date +%s` POST-LS >> $WRAPPERLOG

EXEC="$1"
shift

while [ "$EXEC" != "" ]; do
	echo `date +%s` LOOP-START >> $WRAPPERLOG
	ARGS=
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
	echo `date +%s` EXECUTING $EXEC $ARG >> $WRAPPERLOG
	"$EXEC" "${ARGS[@]}"
	EXITCODE=$?
	echo `date +%s` EXECUTED $EXITCODE >> $WRAPPERLOG
	
	if [ "$EXITCODE" != "0" ]; then
		echo `date +%s` FAILED >> $WRAPPERLOG
		exit $EXITCODE
	fi

	EXEC="$1"
	shift
done
echo `date +%s` DONE >> $WRAPPERLOG
