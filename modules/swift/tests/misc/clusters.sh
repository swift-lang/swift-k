#!/bin/bash

# this will turn on clustering.

# TODO need to check that clustering actually gets used.
#   unsure what the best programmatic way
# to determine that clustering actually happen is. perhaps look at
# the cluster log file and see that there is only one of them or
# run some log analysis to determine the clustering pattern.
#   or, could prepend a touch onto the start of seq.sh for the
# duration of this test, and then check that touch later

export CF=swift.properties.no-retries

cat ../language-behaviour/tc.data | sed 's/null$/GLOBUS::maxwalltime="0:1"/' > clusters.tc.data

cd ../language-behaviour

cat $(dirname $(which swift))/../etc/swift.properties | grep --invert-match -E '^clustering.enabled=' | grep --invert-match -E '^clustering.min.time=' | grep --invert-match -E '^execution.retries=' > $CF

echo clustering.enabled=true >> $CF
echo clustering.min.time=600 >> $CF

# turn of retries, so that if we fail in a cluster, we don't give the
# engine another chance to run potentially outside of.
# (in r2541, although clustering is broken, this cluster test can pass
# because rate limiting eventually takes us down to 1 job at once, which
# then avoids clustering, and succeeds)
echo execution.retries=0 >> $CF

export SWIFT_TEST_PARAMS="-config $CF -tc.file ../misc/clusters.tc.data"

./run 130-fmri


