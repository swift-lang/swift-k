#!/bin/bash

set -x

grep I_IS_4 105-assert-loop.stdout || exit 1

LINES=$( grep -c "i:" 105-assert-loop.stdout )
[[ ${LINES} == 5 ]] || exit 1

exit 0
