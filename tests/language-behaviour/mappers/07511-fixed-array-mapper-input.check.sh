#!/bin/bash
set -x
cat $TEST_LOG | grep "file: 07511-fixed-array-mapper-input.first.in" || exit 1
cat $TEST_LOG | grep "file: 07511-fixed-array-mapper-input.second.in" || exit 1
cat $TEST_LOG | grep "file: 07511-fixed-array-mapper-input.third.in" || exit 1
exit 0
