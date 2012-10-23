#!/bin/bash

# run tests with replication enabled and one site set to queue for a long
# time

cd ../language-behaviour

./generate-tc.data

cat tc.data | grep -e '^localhost' > tmp.tc.data.wonky-twosite

cat tc.data ../sites/tc.data > tmp.tc.data.sites

cat tmp.tc.data.wonky-twosite | sed 's/localhost/wonkyA/' >> tmp.tc.data.sites
cat tmp.tc.data.wonky-twosite | sed 's/localhost/wonkyB/' >> tmp.tc.data.sites

export SWIFT_TEST_PARAMS="-tc.file tmp.tc.data.sites -sites.file ../sites/wonky/slow-queue-fast-queue.xml" 

./run 066-many.swift


