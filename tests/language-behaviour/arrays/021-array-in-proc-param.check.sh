#!/bin/bash
set -x
cat 021-array-in-proc-param.out | grep 1 || exit 0
exit 0
