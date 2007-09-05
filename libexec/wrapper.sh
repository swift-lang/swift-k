#!/bin/sh

info() {
	echo "*** uname -a:" >>$WFDIR/info/${ID}-info
	uname -a 2>&1 >>$WFDIR/info/${ID}-info
	echo "*** id:" >>$WFDIR/info/${ID}-info
	id 2>&1 >>$WFDIR/info/${ID}-info
	echo "*** env:" >>$WFDIR/info/${ID}-info
	env 2>&1 >>$WFDIR/info/${ID}-info
	echo "*** df:" >>$WFDIR/info/${ID}-info
	df 2>&1 >>$WFDIR/info/${ID}-info
	echo "*** /proc/cpuinfo:" >>$WFDIR/info/${ID}-info
	cat /proc/cpuinfo 2>&1 >>$WFDIR/info/${ID}-info
	echo "*** /proc/meminfo:" >>$WFDIR/info/${ID}-info
	cat /proc/meminfo 2>&1 >>$WFDIR/info/${ID}-info
}

fail() {
	EC=$1
	shift
	echo $@ >$WFDIR/status/${ID}-error
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

ID=$1
DIR=$ID
STDOUT=$2
STDERR=$3
STDIN=$4
DIRS=$5
LINKS=$6
OUTS=$7
KICKSTART=$8
WRAPPERLOG=$PWD/wrapper.log

PATH=$PATH:/bin:/usr/bin

echo "DIR=$DIR">>$WRAPPERLOG
echo "STDOUT=$STDOUT">>$WRAPPERLOG
echo "STDERR=$STDERR">>$WRAPPERLOG
echo "DIRS=$DIRS">>$WRAPPERLOG
echo "LINKS=$LINKS">>$WRAPPERLOG
echo "OUTS=$OUTS">>$WRAPPERLOG

shift 8

IFS=" "
WFDIR=$PWD

mkdir -p $DIR
checkError 254 "Failed to create job directory $DIR"

for D in $DIRS ; do
	mkdir -p $DIR/$D >>$WRAPPERLOG 2>&1
	checkError 254 "Failed to create input directory $D"
done

for L in $LINKS ; do
	ln -s $PWD/shared/$L $DIR/$L >>$WRAPPERLOG 2>&1
	checkError 254 "Failed to link input file $L"
done

cd $DIR
ls >>$WRAPPERLOG
if [ "$KICKSTART" == "" ]; then
	if [ ! -f "$1" ]; then
		fail 254 "The executable $1 does not exist"
	fi
	if [ ! -x "$1" ]; then
		fail 254 "The executable $1 does not have the executable bit set"
	fi
	if [ "$STDIN" == "" ]; then
		"$@" 1>$STDOUT 2>$STDERR
	else
		"$@" 1>$STDOUT 2>$STDERR <$STDIN
	fi
	checkError $? "Exit code $?"
else
	if [ ! -f $KICKSTART ]; then
		echo "Kickstart executable ($KICKSTART) not found" >>$WRAPPERLOG
		echo "Kickstart executable ($KICKSTART) not found" >>$STDERR
		checkError 254 "The Kickstart executable ($KICKSTART) was not found"		
	elif [ ! -x $KICKSTART ]; then
		echo "Kickstart executable ($KICKSTART) is not executable" >>$WRAPPERLOG
		echo "Kickstart executable ($KICKSTART) is not executable" >>$STDERR
		checkError 254 "The Kickstart executable ($KICKSTART) does not have the executable bit set"
	else
		mkdir -p ../kickstart
		echo "Using Kickstart ($KICKSTART)" >>$WRAPPERLOG
		if [ "$STDIN" == "" ]; then
			$KICKSTART -H -o $STDOUT -e $STDERR "$@" 1>kickstart.xml 2>$STDERR
		else
			$KICKSTART -H -o $STDOUT -e $STDERR "$@" 1>kickstart.xml 2>$STDERR <$STDIN
		fi
		checkError $? "Exit code $?"
		mv -f kickstart.xml ../kickstart/$ID-kickstart.xml >>$WRAPPERLOG 2>&1
		checkError 254 "Failed to copy Kickstart record to shared directory"
	fi
fi
cd ..

echo "Exit code was $EXITCODE" >>$WRAPPERLOG

MISSING=
for O in $OUTS ; do
	if [ ! -f $DIR/$O ]; then
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

for O in $OUTS ; do
	cp $DIR/$O shared/$O >>$WRAPPERLOG 2>&1
	checkError 254 "Failed to copy output file $O to shared directory"
done

rm -rf $DIR >>$WRAPPERLOG 2>&1
checkError 254 "Failed to remove job directory $DIR" 

touch status/"${ID}-success"
