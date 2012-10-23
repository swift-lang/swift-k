#!/bin/bash

export CF=swift.properties.restart-iterate
cat $(dirname $(which swift))/../etc/swift.properties | grep --invert-match -E '^lazy.errors=' > $CF
echo lazy.errors=true >> $CF

rm -f *.rlog restart-*.out restart-iterate.kml restart-iterate.xml restart-iterate.*.out restart-iterate.*.out.single-run

rm -rf _concurrent

echo "localhost	helperA	$(pwd)/restart5-helper-success	INSTALLED	INTEL32::LINUX	null" > tmp.restartOK.tc.data
echo "localhost	helperB	$(pwd)/restart5-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartOK.tc.data
echo "localhost	helperC	$(pwd)/restart5-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartOK.tc.data

swift -config $CF -tc.file tmp.restartOK.tc.data restart-iterate.swift

PRECHECKEXIT=$?


if [ "$PRECHECKEXIT" != 0 ]; then
  echo Failed - attempt to run workflow without restart configuration failed
  exit 1
fi

for fn in restart-iterate.*.out; do
mv $fn ${fn}.single-run
done

rm -f *.rlog restart-*.out restart-iterate.kml restart-iterate.xml
rm -rf _concurrent

echo "localhost	helperA	$(pwd)/restart5-helper-success	INSTALLED	INTEL32::LINUX	null" > tmp.restartA.tc.data
echo "localhost	helperB	$(pwd)/restart5-helper-fail	INSTALLED	INTEL32::LINUX	null" >> tmp.restartA.tc.data
echo "localhost	helperC	$(pwd)/restart5-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartA.tc.data

swift -config $CF -tc.file tmp.restartA.tc.data restart-iterate.swift

FIRSTEXIT=$?

# this invocation should fail, with restart-1.out in existence but
# not the others

if [ "$FIRSTEXIT" == 0 ]; then
  echo Failed - workflow was indicated as successfully completed the first time round.
  exit 2
fi

for i in 0 1 2 3 4 ; do
  if [ ! -f restart-iterate.000$i.out ] ; then
    echo Some first-time output files were missing
    exit 4
  fi
done

for i in 05 06 07 08 09 10 ; do
  if [ -f restart-iterate.00$i.out ] ; then
    echo Second-time output file appeared in first-time run
    exit 5
  fi
done

# now make A fail - we should have run it already, and so we want to make
# sure it does not run again; and make B succeed this time round.

echo "localhost	helperA	$(pwd)/restart5-helper-fail	INSTALLED	INTEL32::LINUX	null" > tmp.restartB.tc.data
echo "localhost	helperB	$(pwd)/restart5-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartB.tc.data
echo "localhost	helperC	$(pwd)/restart5-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartB.tc.data

# there should be only a single rlog here, because we deleted them all
# at the start of this script.
swift -config $CF -resume *.rlog -tc.file tmp.restartB.tc.data restart-iterate.swift

SECONDEXIT=$?

if [ "$SECONDEXIT" != "0" ]; then
  echo Failed - second round failed
  exit 3
fi

if [ ! -f restart-iterate.0002.out ] ; then
  echo Second-time output file missing
  exit 5
fi

for i in 1 2 3; do 
   diff -q restart-iterate.000$i.out restart-iterate.000$i.out.single-run
   if [ "$?" -ne "0" ]; then 
     echo restart-based output file $i differs from non-restarted output file
     exit 6
   fi
done

echo restart-iterate success
exit 0

