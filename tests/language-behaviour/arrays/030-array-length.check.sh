#!/bin/bash
set -x
cat 030-array-length.out | grep "SwiftScript trace: 3" || exit 1
exit 0
