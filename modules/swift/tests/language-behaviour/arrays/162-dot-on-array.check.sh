#!/bin/bash
set -x
cat 162-dot-on-array.out | grep two-C || exit 1
exit 0
