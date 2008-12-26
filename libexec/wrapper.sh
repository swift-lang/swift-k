# this script must be invoked inside of bash, not plain sh

infosection() {
	echo >& "$INFO"
	echo "_____________________________________________________________________________" >& "$INFO"
	echo >& "$INFO"
	echo "        $1" >& "$INFO" 
	echo "_____________________________________________________________________________" >& "$INFO"
	echo >& "$INFO"
}

info() {
	infosection "uname -a"
	uname -a 2>&1 >& "$INFO"
	infosection "id"
	id 2>&1 >& "$INFO"
	infosection "env"
	env 2>&1 >& "$INFO"
	infosection "df"
	df 2>&1 >& "$INFO"
	infosection "/proc/cpuinfo"
	cat /proc/cpuinfo 2>&1 >& "$INFO"
	infosection "/proc/meminfo"
	cat /proc/meminfo 2>&1 >& "$INFO"
	infosection "command line"
	echo $COMMANDLINE 2>&1 >& "$INFO"
}

logstate() {
	echo "Progress " `date +"%Y-%m-%d %H:%M:%S.%N%z"` " $@" >& "$INFO"
}

log() {
	echo "$@" >& "$INFO"
}

fail() {
	EC=$1
	shift
	echo $@ >"$WFDIR/status/$JOBDIR/${ID}-error"
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

openinfo() {
	exec 3<> $1
	INFO=3
}

closeinfo() {
	exec 3>&-
}

COMMANDLINE=$@

# get the parent directory of the directory containing wrapper.sh, to use
# as the run directory
# this assumes that wrapper.sh is being executed from the top level of
# the shared directory, and that shared directory is in the top level
# of the workflow run directory
WFDIR=$(dirname $(dirname $0))

cd $WFDIR
openinfo "wrapper.log"
ID=$1
checkEmpty "$ID" "Missing job ID"

shift

getarg "-jobdir" "$@"
JOBDIR=$VALUE
shift $SHIFTCOUNT

checkEmpty "$JOBDIR" "Missing job directory prefix"
mkdir -p $WFDIR/info/$JOBDIR
closeinfo
rm -f "$WFDIR/info/$JOBDIR/${ID}-info"
openinfo "$WFDIR/info/$JOBDIR/${ID}-info"

logstate "LOG_START"
infosection "Wrapper"

mkdir -p $WFDIR/status/$JOBDIR

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

if [ "X$SWIFT_JOBDIR_PATH" != "X" ]; then
  log "Job directory mode is: local copy"
  DIR=${SWIFT_JOBDIR_PATH}/$JOBDIR/$ID
  COPYNOTLINK=1
else
  log "Job directory mode is: link on shared filesystem"
  DIR=jobs/$JOBDIR/$ID
  COPYNOTLINK=0
fi

PATH=$PATH:/bin:/usr/bin

if [ "$PATHPREFIX" != "" ]; then
export PATH=$PATHPREFIX:$PATH
fi

if [ "X${EXEC:0:1}" != "X/" ] ; then
export ORIGEXEC=$EXEC
export EXEC=$(which $EXEC)
if [ "X$EXEC" = "X" ] ; then
fail 254 "Cannot find executable $ORIGEXEC on site system path"
fi
fi

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
log "ARGC=$#"

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
	if [ $COPYNOTLINK = 1 ]; then
		cp "$PWD/shared/$L" "$DIR/$L" 2>&1 >& $INFO
		checkError 254 "Failed to copy input file $L"
		log "Copied input: $PWD/shared/$L to $DIR/$L"
	else
		ln -s "$PWD/shared/$L" "$DIR/$L" 2>&1 >& $INFO
		checkError 254 "Failed to link input file $L"
		log "Linked input: $PWD/shared/$L to $DIR/$L"
	fi
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
		mkdir -p $WFDIR/kickstart/$JOBDIR
		log "Using Kickstart ($KICKSTART)"
		if [ "$STDIN" == "" ]; then
			"$KICKSTART" -H -o "$STDOUT" -e "$STDERR" "$EXEC" "$@" 1>kickstart.xml 2>"$STDERR"
		else
			"$KICKSTART" -H -o "$STDOUT" -i "$STDIN" -e "$STDERR" "$EXEC" "$@" 1>kickstart.xml 2>"$STDERR"
		fi
		export APPEXIT=$?
		mv -f kickstart.xml "$WFDIR/kickstart/$JOBDIR/$ID-kickstart.xml" 2>&1 >& "$INFO"
		checkError 254 "Failed to copy Kickstart record to shared directory"
		if [ "$APPEXIT" != "0" ]; then
			fail $APPEXIT "Exit code $APPEXIT"
		fi
	fi
fi

cd $WFDIR

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
	cp "$DIR/$O" "shared/$O" 2>&1 >& "$INFO"
	checkError 254 "Failed to copy output file $O to shared directory"
done

logstate "RM_JOBDIR"
rm -rf "$DIR" 2>&1 >& "$INFO"
checkError 254 "Failed to remove job directory $DIR" 

logstate "TOUCH_SUCCESS"
touch status/${JOBDIR}/${ID}-success
logstate "END"

closeinfo
