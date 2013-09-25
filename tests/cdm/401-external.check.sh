#!/bin/sh

set -x

cat   external.out || exit 1
rm -v external.out || exit 1

grep $( uname -m ) 210-input.txt  || exit 1
grep $( uname -m ) 210-output.txt || exit 1

exit 0
