#!/bin/bash

ARGS_FILE=script.args
case $STRESS in
    "S1")
        FILES=10
        LOOPS=10
        ;;
    "S2")
        FILES=100
        LOOPS=10
        ;;
    "S3")
        FILES=200
        LOOPS=20
        ;;
    "S4")
        FILES=300
        LOOPS=10
        ;;
    *)
        FILES=10
        LOOPS=10
        ;;
esac

dd if=/dev/zero of=dummy bs=1024 count=0 seek=$((1024*FILES))
echo "-loops=$LOOPS" > $ARGS_FILE