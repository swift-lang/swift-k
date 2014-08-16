#!/bin/bash

FILES=$*
SUM=0
COUNT=0

for file in $*
do
    RES=($(awk '{ sum += $1 } END { print sum,NR }' $file))
    echo "${RES[0]} ${RES[1]}"
    SUM=$(($SUM+${RES[0]}))
    COUNT=$(($COUNT+${RES[1]}))
done
echo "SUM  : $SUM"
echo "COUNT: $COUNT"
exit 0
