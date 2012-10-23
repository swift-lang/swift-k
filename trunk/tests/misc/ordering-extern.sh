#!/bin/bash

export CF=swift.properties.restart-extern
cat $(dirname $(which swift))/../etc/swift.properties | grep --invert-match -E '^lazy.errors=' > $CF
echo lazy.errors=true >> $CF

rm -f *.rlog restart-*.out restart-extern.kml restart-extern.xml restart-*.out

rm -rf _concurrent

echo "localhost	helperA	$(pwd)/restart5-helper-success	INSTALLED	INTEL32::LINUX	null" > tmp.restartOK.tc.data
echo "localhost	helperB	$(pwd)/restart5-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartOK.tc.data
echo "localhost	helperC	$(pwd)/restart5-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartOK.tc.data

swift -config $CF -tc.file tmp.restartOK.tc.data restart-extern.swift -dir=`pwd`

PRECHECKEXIT=$?


if [ "$PRECHECKEXIT" != 0 ]; then
  echo Failed - attempt to run workflow without ordering configuration failed
  exit 1
fi

rm -f *.rlog restart-*.out restart-extern.kml restart-extern.xml
rm -rf _concurrent

# make A fail and B succeed.
# extern dependency ordering should mean that B does not run.

echo "localhost	helperA	$(pwd)/restart5-helper-fail	INSTALLED	INTEL32::LINUX	null" > tmp.restartB.tc.data
echo "localhost	helperB	$(pwd)/restart5-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartB.tc.data
echo "localhost	helperC	$(pwd)/restart5-helper-success	INSTALLED	INTEL32::LINUX	null" >> tmp.restartB.tc.data

swift -config $CF -tc.file tmp.restartB.tc.data restart-extern.swift -dir=`pwd`

SECONDEXIT=$?

if [ "$SECONDEXIT" = "0" ]; then
  echo Failed - broken apps succeeded 
  exit 2
fi

if [ -f restart-extern.2.out ] || [ -f restart-extern.1.out ]; then
  echo Failed - output files came into existence that indicate external dependency was ignored
  exit 3
fi

echo restart-exterm success
exit 0

