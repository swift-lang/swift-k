#!/bin/sh

set -x

LINES=$( cat file-*.txt | grep -c HOWDY )
[[ ${LINES} == 7 ]] || exit 1

exit 0
