#!/bin/bash

# Sketch of script that is called on remote test site

GROUPLISTFILE=$1 # E.g., groups/group-all-local.sh

# Enter test directory
cd $( dirname $0 ) || exit 1

svn up || exit 1

# Work within, e.g., /home/wozniak/nightly/topdir
rm -rf topdir
./nightly.sh -g -o topdir $GROUPLISTFILE
[ $? != 0 ] && exit 1

exit 0
