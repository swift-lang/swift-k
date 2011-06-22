#!/bin/bash
set -x
cat 030-mix-float-int.out | grep 54.3|| exit 1
exit 0
