#!/bin/bash

EXPECTED_ARGS=2

if [ $# -ne $EXPECTED_ARGS ]
then
    echo "Expecting 2 args : Got $*"
    exit 1
fi

file1=$1
file2=$2

var1=$(cat $file1)
var2=$(cat $file2)

echo $(($var1 + $var2))