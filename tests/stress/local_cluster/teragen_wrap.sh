#!/bin/bash

# By default with ARG1:100 and SLICESIZE=10000, this script will generate
# 10^6 records.
ARG1=1
[ ! -z $1 ] && ARG1=$1

FILE="input_$RANDOM.txt"
LOWERLIMIT=0
UPPERLIMIT=1000000 # 10^9
SLICESIZE=10000     # 10^4 records padded to 100B would result in 1MB file
#SLICESIZE=1000     # 10^3  If padded to 100B would result

shuf -i $LOWERLIMIT-$UPPERLIMIT -n $(($SLICESIZE*$ARG1)) | awk '{printf "%-99s\n", $0}'
exit 0
