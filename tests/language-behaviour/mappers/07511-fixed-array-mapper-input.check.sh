#!/bin/bash
set -x
cat 07511-fixed-array-mapper-input.stdout | grep "file: 07511-fixed-array-mapper-input.first.in" || exit 1
cat 07511-fixed-array-mapper-input.stdout | grep "file: 07511-fixed-array-mapper-input.second.in" || exit 1
cat 07511-fixed-array-mapper-input.stdout | grep "file: 07511-fixed-array-mapper-input.third.in" || exit 1
exit 0
