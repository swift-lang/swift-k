#!/bin/bash

set -x

grep I_IS_4 105-assert-loop.stdout || exit 1

#
# Assert fails the entire run whenever the assertion
# fails, and not when the loop in which the assert statement
# exists completes. It is therefore incorrect to assume anything
# about the number of parallel iterations that are
# completed when assert aborts everything.
#

#LINES=$( grep -c "i:" 105-assert-loop.stdout )
#[[ ${LINES} == 5 ]] || exit 1

exit 0
