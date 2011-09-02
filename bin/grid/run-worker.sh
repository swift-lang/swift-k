#! /bin/bash

contact=$1
workername=$2
origlogdir=$3
echo OSG_WN_TMP=$OSG_WN_TMP
if [ _$OSG_WN_TMP = _ ]; then
  OSG_WN_TMP=/tmp
fi
mkdir -p $OSG_WN_TMP
logdir=$(mktemp -d $OSG_WN_TMP/${workername}.workerdir.XXXXXX)
nlines=1000

PATH=$OSG_APP:$OSG_APP/scec:$OSG_APP/extenci/aashish/terfix/bin:$PATH

echo "=== contact: $contact"
echo "=== name:    $workername Running in dir $(pwd)"
echo "=== cwd:     $(pwd)"
echo "=== logdir:  $logdir"
echo "==============================================="

cat >worker.pl
chmod +x worker.pl

./worker.pl $contact $workername $logdir

exitcode=$?

echo "=== exit: worker.pl exited with code=$exitcode"

echo "=== worker log - last $nlines lines:"

echo

tail -v -n $nlines $logdir/*

