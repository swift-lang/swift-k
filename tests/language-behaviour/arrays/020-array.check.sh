#!/bin/bash
set -x
cat 020-array.out | grep 4 || exit 1
exit 0
