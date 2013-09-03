#!/bin/bash

ARG1=100

[ ! -z $1 ] && ARG1=$1

SCALE=1000000

for i in `seq 1 $ARG1`
do
    X=$(($(od -vAn -N4 -tu4 < /dev/urandom) % $SCALE ))
    Y=$(($(od -vAn -N4 -tu4 < /dev/urandom) % $SCALE ))
    echo -n "$X $Y "
    DIST=$(echo "scale=5;sqrt(($X^2) + ($Y^2))" | bc)
    if [ $(echo "$DIST <= $SCALE" | bc ) -ne 0 ]; then
        echo "$DIST YES"
    else
        echo "$DIST NO"
    fi
done;

exit 0