#!/bin/bash

echo "From filemaker.sh $1 $2 on Host:$HOSTNAME"
MAXSIZE=$1
OUT=$2
dd if=/dev/zero of=$OUT bs=1024 count=0 seek=$((1024*MAXSIZE))

