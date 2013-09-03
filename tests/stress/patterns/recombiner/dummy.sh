#!/bin/bash

tmp="TMP.$RANDOM"
cat $* > $tmp
awk '{ sum += $1 } END { print sum }' $tmp
rm $tmp