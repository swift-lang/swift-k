#!/bin/sh

set -x

grep $( uname -m ) 205-input.txt  || exit 1
grep $( uname -m ) /tmp/205-output.txt || exit 1

exit 0
