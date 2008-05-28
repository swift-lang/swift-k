#!/bin/bash

export CF=`pwd`/swift.properties.restart4
cat $(dirname $(which swift))/../etc/swift.properties | grep --invert-match -E '^lazy.errors=' > $CF
echo lazy.errors=true >> $CF

rm -f *.rlog restart-*.out

echo "localhost	helperA	$(pwd)/restart-helper-success	INSTALLED	INTEL32::LINUX	null" > tmp.restart.tc.data
echo "localhost	helperB	$(pwd)/restart-helper-fail	INSTALLED	INTEL32::LINUX	null" >> tmp.restart.tc.data
echo "localhost	helperC	$(pwd)/restart-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restart.tc.data

swift -config $CF  -tc.file tmp.restart.tc.data restart4.swift

FIRSTEXIT=$?

# this invocation should fail, with restart-1.out in existence but
# not the others

if [ "$FIRSTEXIT" == 0 ]; then
  echo Failed - workflow was indicated as successfully completed the first time round.
  exit 1
fi

# now make A fail - we should have run it already, and so we want to make
# sure it does not run again; and make B succeed this time round.

echo "localhost	helperA	$(pwd)/restart-helper-fail	INSTALLED	INTEL32::LINUX	null" > tmp.restart.tc.data
echo "localhost	helperB	$(pwd)/restart-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restart.tc.data
echo "localhost	helperC	$(pwd)/restart-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restart.tc.data

# there should be only a single rlog here, because we deleted them all
# at the start of this script.
swift -config $CF  -resume *.rlog -tc.file tmp.restart.tc.data restart4.swift

SECONDEXIT=$?

if [ "$SECONDEXIT" != "0" ]; then
  echo Failed - second round did not exit with success
  exit 4
fi

echo success
exit 0

