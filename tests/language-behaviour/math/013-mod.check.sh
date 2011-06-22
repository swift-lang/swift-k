#!/bin/bash
set -x
cat 013-mod.out | grep 19|| exit 1
exit 0
