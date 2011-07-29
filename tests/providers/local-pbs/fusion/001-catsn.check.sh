#!/bin/bash

set -x

LINES=$( ls catsn*.out | wc -l )
[[ ${?} == 0 ]] || exit 1
[[ ${LINES} == 10 ]] || exit 1

exit 0
