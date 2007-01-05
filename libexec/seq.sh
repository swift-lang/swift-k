#!/bin/sh

WRAPPERLOG=$PWD/wrapper.log
ls >>$WRAPPERLOG

EXEC="$1"
shift

while [ "$EXEC" != "" ]; do
	ARGS=
	INDEX=0
	
	ARG="$1"
	shift
	
	while [ "$ARG" != "|" ]; do
		if [ "$ARG" == "||" ]; then
			ARG="|"
		fi
		echo "SEQ: ARG=$ARG">>$WRAPPERLOG
		ARGS[$INDEX]=$ARG
		let INDEX=$INDEX+1
		
		ARG="$1"
		shift
	done
	echo "SEQ: Executing $EXEC $ARGS" >>$WRAPPERLOG
	"$EXEC" "${ARGS[@]}"
	
	EXITCODE=$?
	if [ "$EXITCODE" != "0" ]; then
		echo "SEQ: Failing ($EXITCODE)" >>$WRAPPERLOG
		exit $EXITCODE
	fi

	EXEC="$1"
	shift
done
echo "SEQ: No more stuff" >>$WRAPPERLOG
