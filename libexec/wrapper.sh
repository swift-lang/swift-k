#!/bin/sh

DIR=$1
STDOUT=$2
STDERR=$3
DIRS=$4
LINKS=$5
OUTS=$6
KICKSTART=$7
WRAPPERLOG=$PWD/wrapper.log

PATH=$PATH:/bin:/usr/bin

echo "DIR=$DIR">>$WRAPPERLOG
echo "STDOUT=$STDOUT">>$WRAPPERLOG
echo "STDERR=$STDERR">>$WRAPPERLOG
echo "DIRS=$DIRS">>$WRAPPERLOG
echo "LINKS=$LINKS">>$WRAPPERLOG
echo "OUTS=$OUTS">>$WRAPPERLOG

shift 7

IFS=" "

for D in $DIRS ; do
	mkdir -p $DIR/$D >>$WRAPPERLOG 2>&1
done

for L in $LINKS ; do
	ln -s $PWD/shared/$L $DIR/$L >>$WRAPPERLOG 2>&1
done

cd $DIR
if [ "$KICKSTART" == "" ]; then
	"$@" 1>$STDOUT 2>$STDERR
	EXITCODE=$?
else
	if [ ! -f $KICKSTART ]; then
		echo "Kickstart executable ($KICKSTART) not found" >>$WRAPPERLOG
		echo "Kickstart executable ($KICKSTART) not found" >>$STDERR
		#surely, we can use any numbers here
		EXITCODE=1024
	elif [ ! -x $KICKSTART ]; then
		echo "Kickstart executable ($KICKSTART) is not executable" >>$WRAPPERLOG
		echo "Kickstart executable ($KICKSTART) is not executable" >>$STDERR
		EXITCODE=1025
	else
		mkdir -p ../kickstart
		echo "Using Kickstart ($KICKSTART)" >>$WRAPPERLOG
		$KICKSTART -H -o $STDOUT -e $STDERR "$@" 1>kickstart.xml 2>$STDERR
		EXITCODE=$?
		mv -f kickstart.xml ../kickstart/$DIR-kickstart.xml >>$WRAPPERLOG 2>&1
	fi
fi
cd ..


echo "Exit code was $EXITCODE" >>$WRAPPERLOG

if [ "$EXITCODE" != "0" ]; then
	echo $EXITCODE > $DIR/exitcode
	echo "Job failed with exit code $EXITCODE" >>$WRAPPERLOG 2>&1
else
	ECP="n"
	for O in $OUTS ; do
		cp $DIR/$O shared/$O >>$WRAPPERLOG 2>&1
		if [ "$?" != "0" ]; then
			ECP="y"
		fi
	done
	if [ "$ECP" == "y" ]; then
		echo "Errors encountered while copying output files; keeping job directory" >>$WRAPPERLOG
		echo "Failed to copy output files to shared directory" >>$DIR/exitcode
		$EXITCODE=128
	else
		rm -rf $DIR >>$WRAPPERLOG 2>&1
	fi
fi

exit $EXITCODE
