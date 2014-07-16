#!/bin/bash

# run tests with no retries
# this should expose, for example, the problem i'm seeing with
# 130-fmri failing once on my mac

export CF=swift.properties.no-retries

cd ../language-behaviour

cat $(dirname $(which swift))/../etc/swift.properties | grep --invert-match -E '^execution.retries=' > $CF
echo execution.retries=0 >> $CF

export SWIFT_TEST_PARAMS="-config $CF -tc.file ./tc.data"

./run


