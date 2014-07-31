#!/bin/bash
set -x
cat 025-array-passing.out | grep two || exit 1
exit 0
