#!/bin/bash

set -x

FILES=( $( ls 400-fprintf-*.out ) )
(( ${#FILES[@]} == 3 )) || exit 1

grep hello 400-fprintf-1.out || exit 1

LINES=$( wc -l 400-fprintf-2.out | sed 's/^[ \t]*//' | cut -d ' ' -f 1 )
(( ${LINES} == 3 )) || exit 1

grep "hello: 32" 400-fprintf-3.out || exit 1

exit 0
