#!/bin/bash
set -x
cat one.count | grep '3 one.txt' || exit 1
cat two.count | grep '3 two.txt' || exit 1
cat three.count | grep '1 three.txt' || exit 1
exit 0
