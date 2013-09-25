#!/bin/bash

numtests=$1

if [ -z "$numtests" ]; then
   echo "Usage: $0 <numtests>"
   exit 1
fi

origdir=$PWD
basedir=$( cd ../../../../..; echo $PWD; cd $origdir )
testdir=$( cd ..; echo $PWD; cd $origdir )
cd $testdir

for i in $( seq -w 001 $numtests ); do
   ./suite.sh -t -o $basedir groups/group-all-local.sh
   outputdir=$( ls -1rtd $basedir/run-????-??-?? | tail -1 )
   mv $outputdir $outputdir.loop.$i
done

cd $origdir
./report.pl $basedir > $basedir/release.html
echo Results can be found at $basedir/release.html
