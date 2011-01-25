#!/bin/bash

# example file for setting up environment and running nightly.sh
# given a groupslistfile, will run nightly.sh on that site

GROUPSLISTFILE=$1 # E.g., groups/group-all-local.sh

# user-specific variables

export WORK=/ci/projects/cnari/swift_work/skenny
export QUEUE=short
export PROJECT=CI-IBN000039

# branch testing

export COG_VERSION=branches/4.1.8
export SWIFT_VERSION=branches/release-0.92

/home/skenny/swift_runs/tests/nightly.sh -g $GROUPSLISTFILE

exit 0
