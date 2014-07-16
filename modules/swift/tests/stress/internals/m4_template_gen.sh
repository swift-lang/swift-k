#!/bin/bash

for i in $(ls *swift)
do
    NAME=${i%.swift};
    ARGS="$NAME.args.m4"
    if [ -f $ARGS ]; then
        rm $ARGS;
    fi;
    grep "#NIGHTLY" $i >> $ARGS
    grep "#WEEKLY" $i  >> $ARGS
    cat test.args.m4   >> $ARGS
done