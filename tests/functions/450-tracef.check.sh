#!/bin/bash

set -x

grep "int:3:3"                 stdout.txt || exit 1
grep "string:4:4"              stdout.txt || exit 1
grep "fraction:3.14"           stdout.txt || exit 1
grep "file:file:.*/test.txt"   stdout.txt || exit 1
grep "array:\[9.0,91.0,19.0\]" stdout.txt || exit 1
grep "pointer:.*Closed"        stdout.txt || exit 1

[[ $( grep -c "WORD" stdout.txt ) == 2 ]] || exit 1
[[ $( grep "WORD" stdout.txt | wc -w ) == 5 ]] || exit 1

exit 0
