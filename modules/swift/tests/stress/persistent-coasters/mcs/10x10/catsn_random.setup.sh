#!/bin/bash

NUMFILES=10
FILESIZE=10
FILESIZE=$(($FILESIZE*1000000))

if [ -n "$GROUP" ]; then
   cp $RUNDIR/* .
fi

mkdir -p data
for count in `seq 1 $NUMFILES` 
do
   FILENAME=`mktemp -p data`
   ./gendata.pl $FILESIZE > $FILENAME
done

start-coaster-service
