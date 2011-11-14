#!/bin/sh

set -x

grep $( uname -m ) 211-input.txt  || exit 1
grep $( uname -m ) /tmp/211-output.txt || exit 1

exit 0
