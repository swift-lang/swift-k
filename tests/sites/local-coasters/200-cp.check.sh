#!/bin/sh

set -x

grep $( uname -m ) 200-input.txt  || exit 1
grep $( uname -m ) 200-output.txt || exit 1

exit 0
