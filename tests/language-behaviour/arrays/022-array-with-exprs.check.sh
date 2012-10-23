#!/bin/bash
set -x
cat 022-array-with-exprs.out | grep 22 || exit 1
exit 0
