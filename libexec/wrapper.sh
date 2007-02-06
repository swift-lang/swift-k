#!/bin/sh

DIR=$1
STDOUT=$2
STDERR=$3
DIRS=$4
LINKS=$5
OUTS=$6
KICKSTART=$7
KICKSTARTREC=$8
WRAPPERLOG=$PWD/wrapper.log

echo "DIR=$DIR">>$WRAPPERLOG
echo "STDOUT=$STDOUT">>$WRAPPERLOG
echo "STDERR=$STDERR">>$WRAPPERLOG
echo "DIRS=$DIRS">>$WRAPPERLOG
echo "LINKS=$LINKS">>$WRAPPERLOG
echo "OUTS=$OUTS">>$WRAPPERLOG

shift 8

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
	$KICKSTART -H -o $STDOUT -e $STDERR "$@" 1>$KICKSTARTREC
	EXITCODE=$?
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