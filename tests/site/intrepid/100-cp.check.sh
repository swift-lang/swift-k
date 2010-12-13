#!/bin/sh

set -x 

grep $( uname -m )  100-cp-output.txt || exit 1

exit 0
