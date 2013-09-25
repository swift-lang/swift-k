#!/bin/bash
set -x
cat 053-pass-int.out | grep 7 || exit 1
exit 0
