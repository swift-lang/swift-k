#!/bin/bash

set -x

cat stdout.txt

X=$( grep -c "file: 07511" stdout.txt )
[ $? == 0 ] || exit 1
echo $X
(( $X == 3 )) || exit 1

exit 0
