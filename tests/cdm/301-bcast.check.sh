#!/bin/sh

set -x

grep $( uname -m ) 301/301-input.txt || exit 1
grep $( uname -m ) 301-output.txt    || exit 1

exit 0
