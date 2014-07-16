#!/bin/bash

cd ../language-behaviour

./generate-tc.data

cat tc.data ../sites/tc.data > tmp.tc.data.sites

SITE=wonky/wrongdir.xml

echo testing site configuration: $SITE

export CF=swift.properties.wrongdir-relative-fail
cat $(dirname $(which swift))/../etc/swift.properties | grep --invert-match -E '^wrapper.invocation.mode=' > $CF
echo wrapper.invocation.mode=relative >> $CF

export SWIFT_TEST_PARAMS="-sites.file ../sites/${SITE} -tc.file tmp.tc.data.sites -config $CF"

./run 001-echo

if [ "$?" = "0" ] ; then
  echo "wonky-wrongdir-relative-fail test passed when it should have failed"
  exit 1
else
  echo "wonky-wrongdir-relative-fail failed as expected, so test passes"
  exit 0
fi

  
