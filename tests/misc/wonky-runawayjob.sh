#!/bin/bash

cd ../language-behaviour

cat ../sites/tc.data > tmp.tc.data.sites

SITE=wonky/wonky-runawayjob.xml

echo testing site configuration: $SITE

export SWIFT_TEST_PARAMS="-sites.file ../sites/${SITE} -tc.file tmp.tc.data.sites"

./run 001-echo

