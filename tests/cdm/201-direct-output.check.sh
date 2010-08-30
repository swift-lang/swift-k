#!/bin/sh

set -x

grep $( uname -m ) 201-input.txt  || exit 1
grep $( uname -m ) 201/201-output.txt || exit 1

exit 0
