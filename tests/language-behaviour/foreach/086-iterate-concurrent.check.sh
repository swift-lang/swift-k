#!/bin/sh

set -x

LINES=$( cat _concurrent/outfile-* | grep -c hello )
[[ ${LINES} == 12 ]] || exit 1

exit 0
