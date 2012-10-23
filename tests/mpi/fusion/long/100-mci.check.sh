#!/bin/bash

set -x

# [ -f 100-output.txt ] || exit 1

for F in transform-*.txt
do
  grep -q header ${F} || exit 1
done

exit 0
