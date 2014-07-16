#!/bin/bash

set -x

COUNT=( $( ls f[a-d].txt ) )
(( ${#COUNT[@]} == 4 )) || exit 1

LINES=$( wc -l < fd.txt )
(( ${LINES} == 4 )) || exit 1

exit 0
