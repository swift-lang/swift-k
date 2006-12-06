#!/bin/sh

DIR=$1
STDOUT=$2
STDERR=$3
DIRS=$4
LINKS=$5
OUTS=$6
WRAPPERLOG=$PWD/wrapper.log

shift 6

IFS=" "

for D in $DIRS ; do
	mkdir -p $DIR/$D >>$WRAPPERLOG 2>&1
done

for L in $LINKS ; do
	ln -s $PWD/shared/$L $DIR/$L >>$WRAPPERLOG 2>&1
done

cd $DIR
"$@" 1>$STDOUT 2>$STDERR
EXITCODE=$?
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
	else
		rm -rf $DIR >>$WRAPPERLOG 2>&1
	fi
fi
