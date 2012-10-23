#!/bin/bash

rm -rf wn-local.d/ wn-local-timestamp

mkdir wn-local.d
sleep 5s
touch wn-local-timestamp
sleep 5s

export SWIFT_JOBDIR_PATH=`pwd`/wn-local.d/

swift ../language-behaviour/001-echo.swift

if [ wn-local.d -nt wn-local-timestamp ]; then
 echo something happened in the worker node directory - PASS
 exit 0
else
 echo nothing happened in the worker node directory - FAIL
 exit 1
fi
