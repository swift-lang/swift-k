#!/bin/bash

FILES=10
rm input/* -rf
for (( i=1; i <= 20; i=$(($i+1)) ))
do
    top=$(($i*5120000))
    ./gendata.pl $top > input/file_$i.inp &
done;