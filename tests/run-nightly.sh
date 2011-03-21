#!/bin/bash

# sets up environment and runs nightly.sh
# given a groupslistfile, will run nightly.sh on that site

GROUPSLISTFILE=$1 # E.g., groups/group-all-local.sh

# user-specific variables

export WORK=$2
export QUEUE=$3
export PROJECT=$4

# branch testing

export COG_VERSION=$5
export SWIFT_VERSION=$6

#$2/nightly.sh -g $GROUPSLISTFILE

$2/nightly.sh -g $GROUPSLISTFILE

exit 0
