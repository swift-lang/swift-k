#!/bin/bash

set -x

grep I_IS_4 106-assert-iter.stdout || exit 1

LINES=$( grep -c "i:" 106-assert-iter.stdout )
[[ ${LINES} == 5 ]] || exit 1

exit 0
