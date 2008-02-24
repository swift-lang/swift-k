#!/bin/bash

# this will turn on clustering. not sure what the best programmatic way
# to determine that clustering actually happen is. perhaps look at
# the cluster log file and see that there is only one of them or
# run some log analysis to determine the clustering pattern.

export CF=swift.properties.no-retries

cd ../language-behaviour

cat $(dirname $(which swift))/../etc/swift.properties | grep --invert-match -E '^clustering.enabled=' > $CF
echo clustering.enabled=true >> $CF

export SWIFT_TEST_PARAMS="-config $CF -tc.file ../misc/clusters.tc.data"

./run 130-fmri


