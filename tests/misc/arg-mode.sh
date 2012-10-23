#!/bin/bash

INITDIR=$(pwd)

cd ../language-behaviour

./generate-tc.data

cat tc.data ../sites/tc.data > tmp.tc.data.sites

SITE=local-wrapper-args-file.xml

echo testing site configuration: $SITE

export SWIFT_TEST_PARAMS="-sites.file ../sites/${SITE} -tc.file tmp.tc.data.sites"

./run 001-echo 066-many 141-space-in-filename 142-space-and-quotes 1421-space-and-quotes


