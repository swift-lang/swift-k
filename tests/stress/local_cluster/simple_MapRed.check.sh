#!/bin/bash

BASE=${0%.check.sh}
ARGS=`cat $BASE.args | sed 's/-loops=//'`

EXPECTED=$(($ARGS * 10000))

if [ -f "final_result" ];then
    RESULT=($(tail -n 1 final_result))
    echo "RESULT line : ${RESULT[*]}"
    echo "EXPECTED = $EXPECTED"
    echo "ACTUAL   = ${RESULT[1]}"
fi

if [[ "${RESULT[1]}" == "$EXPECTED" ]]
then
    echo "Result matched"
else
    echo "Result does not match expectation" >&2
    exit 1
fi
