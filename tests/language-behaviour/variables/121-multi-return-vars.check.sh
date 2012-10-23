#!/bin/bash
set -x
cat 121-multi-return-vars.first.out | grep hi|| exit 1
[ -e 121-multi-return-vars.second.out ] || exit 1
exit 0
