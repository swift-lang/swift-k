#!/bin/bash

# run tests with replication enabled and one site set to queue for a long
# time

export CF=swift.properties.enable-replication

cd ../language-behaviour

./generate-tc.data

cat tc.data | grep -e '^localhost' > tmp.tc.data.wonky-twosite

cat tc.data ../sites/tc.data > tmp.tc.data.sites

cat tmp.tc.data.wonky-twosite | sed 's/localhost/wonkyA/' >> tmp.tc.data.sites
cat tmp.tc.data.wonky-twosite | sed 's/localhost/wonkyB/' >> tmp.tc.data.sites

cat $(dirname $(which swift))/../etc/swift.properties | grep --invert-match -E '^replication.enabled=' > $CF
echo replication.enabled=true >> $CF

export SWIFT_TEST_PARAMS="-config $CF -tc.file tmp.tc.data.sites -sites.file ../sites/wonky/slow-queue-fast-queue.xml -debug" 

./run 066-many.swift


