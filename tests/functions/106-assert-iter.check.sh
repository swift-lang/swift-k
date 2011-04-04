#!/bin/bash

set -x

grep I_IS_4 stdout.txt || exit 1

LINES=$( grep -c "i:" stdout.txt )
[[ ${LINES} == 5 ]] || exit 1

exit 0
