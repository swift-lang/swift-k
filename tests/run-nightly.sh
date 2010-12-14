#!/bin/bash

# Sketch of script that is called on remote test site

GROUPLISTFILE=$1 # E.g., groups/group-all-local.sh

# Enter test directory
cd $( dirname $0 ) || exit 1

svn up || exit 1

./nightly.sh -g -o /scratch/wozniak/nightly $GROUPLISTFILE
[ $? != 0 ] && exit 1

exit 0
