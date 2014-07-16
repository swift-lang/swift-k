#!/bin/bash
set -x
cat 054-pass-int.out | grep 99 || exit 1
exit 0
