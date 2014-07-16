#!/bin/sh

set -x

grep $( uname -m ) 202/202-input-1.txt || exit 1
grep $( hostname ) 202/202-input-1.txt || exit 1

grep $( uname -m ) 202/202-output.txt  || exit 1
grep $( hostname ) 202/202-output.txt || exit 1

exit 0
