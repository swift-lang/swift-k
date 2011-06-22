#!/bin/bash
set -x
cat 011-divide-float.out | grep 0.3333333333333333|| exit 1
exit 0
