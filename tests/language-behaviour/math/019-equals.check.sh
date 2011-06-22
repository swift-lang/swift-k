#!/bin/bash
set -x
cat 019-equals.out | grep false|| exit 1
exit 0
