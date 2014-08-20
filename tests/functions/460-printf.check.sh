#!/bin/bash

set -x

grep "int:2:2"		       460-printf.stdout || exit 1
grep "int:3:3"                 460-printf.stdout || exit 1
grep "string:4:4"              460-printf.stdout || exit 1
grep "fraction:3.14"           460-printf.stdout || exit 1
grep "array:\[9,91,19\]"       460-printf.stdout || exit 1
grep "pointer:.*Closed"        460-printf.stdout || exit 1

[[ $( grep -c "WORD" 460-printf.stdout ) == 2 ]] || exit 1
[[ $( grep -o '\<WORD\>' 460-printf.stdout | wc -l | sed 's/^[ \t]*//' ) == 4 ]] || exit 1

exit 0
