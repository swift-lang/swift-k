#!/bin/sh

DEST=$1
SRC=$2
DURATION=$3

sleep $DURATION
cp -v $SRC $DEST

