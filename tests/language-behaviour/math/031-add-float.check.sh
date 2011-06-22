#!/bin/bash
set -x
cat 031-add-float.out | grep 135.3|| exit 1
exit 0
