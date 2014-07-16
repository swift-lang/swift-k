#!/bin/sh

set -x

grep $( uname -m ) /tmp/206-input.txt || exit 1
grep $( uname -m ) 206-output.txt     || exit 1

exit 0
