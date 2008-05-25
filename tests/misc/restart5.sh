#!/bin/bash

export CF=swift.properties.restart5

cat $(dirname $(which swift))/../etc/swift.properties | grep --invert-match -E '^lazy.errors=' > $CF
echo lazy.errors=true >> $CF

rm -f *.rlog restart-*.out restart5.kml restart5.xml
rm -rf _concurrent

echo "localhost	helperA	$(pwd)/restart-helper-success	INSTALLED	INTEL32::LINUX	null" > tmp.restartOK.tc.data
echo "localhost	helperB	$(pwd)/restart-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartOK.tc.data
echo "localhost	helperC	$(pwd)/restart-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartOK.tc.data

swift -config $CF -tc.file tmp.restartOK.tc.data restart5.swift

PRECHECKEXIT=$?


if [ "$PRECHECKEXIT" != 0 ]; then
  echo Failed - attempt to run workflow without restart configuration failed
  exit 1
fi

rm -f *.rlog restart-*.out restart5.kml restart5.xml
rm -rf _concurrent

echo "localhost	helperA	$(pwd)/restart-helper-success	INSTALLED	INTEL32::LINUX	null" > tmp.restartA.tc.data
echo "localhost	helperB	$(pwd)/restart-helper-fail	INSTALLED	INTEL32::LINUX	null" >> tmp.restartA.tc.data
echo "localhost	helperC	$(pwd)/restart-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartA.tc.data

swift -config $CF -tc.file tmp.restartA.tc.data restart5.swift

FIRSTEXIT=$?

# this invocation should fail, with restart-1.out in existence but
# not the others

if [ "$FIRSTEXIT" == 0 ]; then
  echo Failed - workflow was indicated as successfully completed the first time round.
  exit 1
fi

# now make A fail - we should have run it already, and so we want to make
# sure it does not run again; and make B succeed this time round.

echo "localhost	helperA	$(pwd)/restart-helper-fail	INSTALLED	INTEL32::LINUX	null" > tmp.restartB.tc.data
echo "localhost	helperB	$(pwd)/restart-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartB.tc.data
echo "localhost	helperC	$(pwd)/restart-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartB.tc.data

# there should be only a single rlog here, because we deleted them all
# at the start of this script.
swift -config $CF -resume *.rlog -tc.file tmp.restartB.tc.data restart5.swift

SECONDEXIT=$?

if [ "$SECONDEXIT" != "0" ]; then
  echo Failed - second round failed
  exit 4
fi

echo success
exit 0

