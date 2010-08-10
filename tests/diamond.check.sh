#!/bin/sh

set -x

COUNT=( $( ls f*.txt ) )
(( ${#COUNT[@]} == 4 )) || exit 1

LINES=$( wc -l < fd.txt )
(( ${LINES} == 4 )) || exit 1

exit 0
