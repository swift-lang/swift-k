#! /bin/bash

SERVICEURL=$1
NWORKERS=$2
LOGLEVEL=$3

# WORKER_LOGGING_LEVEL=$LOGLEVEL ./worker.pl http://128.135.125.17:$PORT swork01 ./workerlogs 

LOGDIR=/tmp/$USER/workerlogs
mkdir -p $LOGDIR
cd $LOGDIR

for worker in $(seq -w 0 $(($NWORKERS-1))); do
  WORKER_LOGGING_LEVEL=$LOGLEVEL $HOME/swift_gridtools/worker.pl $SERVICEURL swork${worker} $LOGDIR >& /dev/null &
done
wait
ls -lt $LOGDIR/
tail $LOGDIR/*
