#!/bin/sh

set -x

grep $( uname -m ) 602/602-input.txt || exit 1
grep $( uname -m ) 602-output.txt    || exit 1

exit 0
