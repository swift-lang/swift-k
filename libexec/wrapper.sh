#!/bin/sh

infosection() {
	echo >>"$INFO"
	echo "_____________________________________________________________________________" >>"$INFO"
	echo >>"$INFO"
	echo "        $1" >>"$INFO" 
	echo "_____________________________________________________________________________" >>"$INFO"
	echo >>"$INFO"
}

info() {
	infosection "uname -a"
	uname -a 2>&1 >>"$INFO"
	infosection "id"
	id 2>&1 >>"$INFO"
	infosection "env"
	env 2>&1 >>"$INFO"
	infosection "df"
	df 2>&1 >>"$INFO"
	infosection "/proc/cpuinfo"
	cat /proc/cpuinfo 2>&1 >>"$INFO"
	infosection "/proc/meminfo"
	cat /proc/meminfo 2>&1 >>"$INFO"
}

logstate() {
	echo "Progress " `date +"%Y-%m-%e %H:%M:%S%z"` " $@" >>"$INFO"
}

log() {
	echo "$@" >>"$INFO"
}

fail() {
	EC=$1
	shift
	echo $@ >"$WFDIR/status/${ID}-error"
	log $@
	info
	#exit $EC
	#let vdl-int.k handle the issues
	exit 0
}

checkError() {
	if [ "$?" != "0" ]; then
		fail $@
	fi
}

checkEmpty() {
	if [ "$1" == "" ]; then
		shift
		fail 254 $@
	fi
}

getarg() {
	NAME=$1
	shift
	VALUE=""
	SHIFTCOUNT=0
	if [ "$1" == "$NAME" ]; then
		shift
		let "SHIFTCOUNT=$SHIFTCOUNT+1"
		while [ "${1:0:1}" != "-" ] && [ "$#" != "0" ]; do
			VALUE="$VALUE $1"
			shift
			let "SHIFTCOUNT=$SHIFTCOUNT+1"
		done
	else
		fail 254 "Missing $NAME argument"
	fi
	VALUE="${VALUE:1}"
}

WFDIR=$PWD
INFO="wrapper.log"
ID=$1
checkEmpty "$ID" "Missing job ID"
INFO=$WFDIR/info/${ID}-info
rm -f "$INFO"
logstate "START"
infosection "Wrapper"
DIR=$ID

logstate "OPTION_PROCESSING"
log "Options: $@"
shift

getarg "-e" "$@"
EXEC=$VALUE
shift $SHIFTCOUNT

getarg "-out" "$@"
STDOUT=$VALUE
shift $SHIFTCOUNT

getarg "-err" "$@"
STDERR=$VALUE
shift $SHIFTCOUNT

getarg "-i" "$@"
STDIN=$VALUE
shift $SHIFTCOUNT

getarg "-d" "$@"
DIRS=$VALUE
shift $SHIFTCOUNT

getarg "-if" "$@"
INF=$VALUE
shift $SHIFTCOUNT

getarg "-of" "$@"
OUTF=$VALUE
shift $SHIFTCOUNT

getarg "-k" "$@"
KICKSTART=$VALUE
shift $SHIFTCOUNT

if [ "$1" == "-a" ]; then
	shift
else
	fail 254 "Missing arguments (-a option)"
fi

PATH=$PATH:/bin:/usr/bin

log "DIR=$DIR"
log "EXEC=$EXEC"
log "STDIN=$STDIN"
log "STDOUT=$STDOUT"
log "STDERR=$STDERR"
log "DIRS=$DIRS"
log "INF=$INF"
log "OUTF=$OUTF"
log "KICKSTART=$KICKSTART"
log "ARGS=$@"

IFS="|"

logstate "CREATE_JOBDIR"
mkdir -p $DIR
checkError 254 "Failed to create job directory $DIR"
log "Created job directory: $DIR"

logstate "CREATE_INPUTDIR"
for D in $DIRS ; do
	mkdir -p "$DIR/$D" 2>&1 >>"$INFO"
	checkError 254 "Failed to create input directory $D"
	log "Created output directory: $DIR/$D"
done

logstate "LINK_INPUTS"
for L in $INF ; do
	ln -s "$PWD/shared/$L" "$DIR/$L" 2>&1 >>"$INFO"
	checkError 254 "Failed to link input file $L"
	log "Linked input: $PWD/shared/$L to $DIR/$L"
done

logstate "EXECUTE"
cd $DIR
#ls >>$WRAPPERLOG
if [ ! -f "$EXEC" ]; then
	fail 254 "The executable $EXEC does not exist"
fi
if [ ! -x "$EXEC" ]; then
	fail 254 "The executable $EXEC does not have the executable bit set"
fi
if [ "$KICKSTART" == "" ]; then
	if [ "$STDIN" == "" ]; then
		"$EXEC" "$@" 1>"$STDOUT" 2>"$STDERR"
	else
		"$EXEC" "$@" 1>"$STDOUT" 2>"$STDERR" <"$STDIN"
	fi
	checkError $? "Exit code $?"
else
	if [ ! -f "$KICKSTART" ]; then
		log "Kickstart executable ($KICKSTART) not found"
		fail 254 "The Kickstart executable ($KICKSTART) was not found"		
	elif [ ! -x "$KICKSTART" ]; then
		log "Kickstart executable ($KICKSTART) is not executable"
		fail 254 "The Kickstart executable ($KICKSTART) does not have the executable bit set"
	else
		mkdir -p ../kickstart
		log "Using Kickstart ($KICKSTART)"
		if [ "$STDIN" == "" ]; then
			"$KICKSTART" -H -o "$STDOUT" -e "$STDERR" "$EXEC" "$@" 1>kickstart.xml 2>"$STDERR"
		else
			"$KICKSTART" -H -o "$STDOUT" -i "$STDIN" -e "$STDERR" "$EXEC" "$@" 1>kickstart.xml 2>"$STDERR"
		fi
		export APPEXIT=$?
		mv -f kickstart.xml "../kickstart/$ID-kickstart.xml" 2>&1 >>"$INFO"
		checkError 254 "Failed to copy Kickstart record to shared directory"
		if [ "$APPEXIT" != "0" ]; then
			fail $APPEXIT "Exit code $APPEXIT"
		fi
	fi
fi
cd ..

logstate "EXECUTE_DONE"
log "Job ran successfully"

MISSING=
for O in $OUTF ; do
	if [ ! -f "$DIR/$O" ]; then
		if [ "$MISSING" == "" ]; then
			MISSING=$O
		else
			MISSING="$MISSING, $O"
		fi
	fi
done
if [ "$MISSING" != "" ]; then
	fail 254 "The following output files were not created by the application: $MISSING"
fi

logstate "COPYING_OUTPUTS"
for O in $OUTF ; do
	cp "$DIR/$O" "shared/$O" 2>&1 >>"$INFO"
	checkError 254 "Failed to copy output file $O to shared directory"
done

logstate "RM_JOBDIR"
rm -rf "$DIR" 2>&1 >>"$INFO"
checkError 254 "Failed to remove job directory $DIR" 

logstate "TOUCH_SUCCESS"
touch status/${ID}-success
logstate "END"

