#! /bin/sh
 
BIN=$(cd $(dirname $0); pwd)
BASE=$(cd $BIN/..; pwd)
LOC=$(cd $BASE/..; pwd)

echo executing $0 from BIN=$BIN
echo making tutorial package of BASE=$BASE
echo placing tutorial package in LOC=$LOC

(
  cd $LOC
  tar zcf swift-cray-tutorial.tgz  --exclude-vcs swift-cray-tutorial
  scp swift-cray-tutorial.tgz p01532@raven.cray.com:
)
