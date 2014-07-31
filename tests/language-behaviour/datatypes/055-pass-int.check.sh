#!/bin/bash
set -x
cat 055-pass-int.out | grep 99 || exit 1
exit 0
