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

cat $(dirname $(which swift))/../etc/swift.properties | \
 grep --invert-match -E '^replication.enabled=' | \
 grep --invert-match -E '^tc.file=' | \
 grep --invert-match -E '^sites.file=' \
 > $CF
echo replication.enabled=true >> $CF
echo tc.file=tmp.tc.data.sites >> $CF
echo sites.file=../sites/wonky/slow-queue-fast-queue.xml >> $CF

export SWIFT_TEST_PARAMS="-config $CF" 

./run 0651-several-delay.swift 066-many.swift


