#!/bin/sh

set -x

grep $( uname -m ) 601/601-input.txt || exit 1
grep $( uname -m ) 601-output.txt    || exit 1

exit 0
