#!/bin/bash

echo "FILES TO MERGE : $*" 1>&2
cat $* | sort -k2 | awk -F\  '{a[$2]+=$1} END {for (i in a) print i"  "a[i]}' | sort
