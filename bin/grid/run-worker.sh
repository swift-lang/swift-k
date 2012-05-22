#! /bin/bash

contact=$1
workername=$2
origlogdir=$3
echo OSG_WN_TMP=$OSG_WN_TMP

#if [ _$OSG_WN_TMP = _ ]; then
#  OSG_WN_TMP=/tmp
#fi
mkdir -p $OSG_WN_TMP

logdir=$(mktemp -d $OSG_WN_TMP/${workername}.workerdir.XXXXXX)
export PATH=${OSG_APP}/engage/scec:${OSG_APP}/engage/scec/JBSim3d/bin:${OSG_APP}/engage/scec/SpectralAcceleration/p2utils:${OSG_APP}/extenci/swift/DSSAT/bin:${OSG_APP}/extenci/aashish/terfix/bin:$PATH
#export PATH=${OSG_PATH}:$PATH

echo "=== contact: $contact"
echo "=== name:    $workername Running in dir $(pwd)"
echo "=== cwd:     $(pwd)"
echo "=== logdir:  $logdir"
echo "=== disk space: "
df -h
echo "=== path:    $PATH"
echo "==============================================="

#cat >worker.pl
chmod +x worker.pl

./worker.pl $contact $workername $logdir

exitcode=$?

echo "=== exit: worker.pl exited with code=$exitcode"

echo "=== worker logs:"

echo

for file in $logdir/*
do
   echo $file
   echo -------
   cat $file
done

